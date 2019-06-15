package io.gitlab.mkjeldsen.prstatbucket.unresolved;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import io.gitlab.mkjeldsen.prstatbucket.BackgroundIngester;
import io.gitlab.mkjeldsen.prstatbucket.JsonSupplier;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.EnabledIf;

@EnabledIf("${smoke.tests.enabled:false}")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
final class UnresolvedReviewIntTest {

    @Autowired
    private Jdbi jdbi;

    private Savepoint savepoint;

    @BeforeEach
    void beginTransaction() {
        savepoint = jdbi.withHandle(Savepoint::create);
    }

    @AfterEach
    void rollBack() {
        savepoint.restore(jdbi);
    }

    @Test
    void ingests_and_extracts_open_pull_requests() {

        final var executor = new ForkJoinPool();
        final var ingester =
                new BackgroundIngester(
                        new TestResourceJsonSupplier(), jdbi, executor);

        ingester.ingestAll(
                List.of("/pullrequests.json", "/more-pullrequests.json"));
        if (!executor.awaitQuiescence(2, TimeUnit.SECONDS)) {
            fail("tardy executor");
        }

        final Clock someClock =
                Clock.fixed(
                        Instant.parse("2019-06-10T07:18:02.711Z"),
                        ZoneOffset.UTC);

        final var dao = new UnresolvedReviewDao(jdbi, someClock);
        final var openPullRequests = dao.getOpenPullRequests();
        assertThat(openPullRequests)
                .isNotEmpty()
                .isSorted()
                .anySatisfy(
                        pullRequest -> {
                            assertThat(pullRequest.getTitle())
                                    .describedAs("title")
                                    .isEqualTo("Some open PR");
                            assertThat(pullRequest.getAge())
                                    .describedAs("age")
                                    .isEqualTo("95d 11h 57m");
                        })
                .anySatisfy(
                        pullRequest -> {
                            assertThat(pullRequest.getTitle())
                                    .describedAs("title")
                                    .isEqualTo("Some open PR in another repo");
                            assertThat(pullRequest.getAge())
                                    .describedAs("age")
                                    .isEqualTo("7d 18h");
                        });
    }

    @Test
    void bug_open_to_closed_pr() {

        // If a PR is ingested while open, then again after closing, its state
        // must accurately reflect this change.
        //
        // Ingest an open PR, then ingest it again with some closed state.
        // Expect it to no longer be open.

        final var executor = new ForkJoinPool();
        final var ingester =
                new BackgroundIngester(
                        new TestResourceJsonSupplier(), jdbi, executor);

        ingester.ingest("/bug/open-to-closed/pullrequests-open.json");
        if (!executor.awaitQuiescence(1, TimeUnit.SECONDS)) {
            fail("tardy executor");
        }

        final var someClock = Clock.systemUTC();
        final var dao = new UnresolvedReviewDao(jdbi, someClock);
        final var beforeClosed = dao.getOpenPullRequests();

        assertThat(beforeClosed)
                .isNotEmpty()
                .extracting(UnresolvedReview::getTitle)
                .describedAs("title")
                .contains("Some open, then closed PR");

        ingester.ingest("/bug/open-to-closed/pullrequests-closed.json");
        if (!executor.awaitQuiescence(1, TimeUnit.SECONDS)) {
            fail("tardy executor");
        }

        final var afterClosed = dao.getOpenPullRequests();
        assertThat(afterClosed)
                .extracting(UnresolvedReview::getTitle)
                .describedAs("title")
                .doesNotContain("Some open, then closed PR");
    }

    private static final class TestResourceJsonSupplier
            implements JsonSupplier {
        @Override
        public String getJson(String url) throws IOException {
            if (url.contains("?ctx=")) {
                url = "/activity-empty.json";
            } else if (url.endsWith("/activity")) {
                url = "/activity.json";
            }
            try (var json = getClass().getResourceAsStream(url)) {
                assert json != null : url;
                byte[] bytes = json.readAllBytes();
                return new String(bytes, StandardCharsets.UTF_8);
            }
        }

        @Override
        public void close() throws Exception {}
    }
}
