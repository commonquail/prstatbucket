package io.gitlab.mkjeldsen.prstatbucket.unresolved;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import io.gitlab.mkjeldsen.prstatbucket.BackgroundIngester;
import io.gitlab.mkjeldsen.prstatbucket.PullRequestStateFilter;
import io.gitlab.mkjeldsen.prstatbucket.testhelper.TestResourceJsonSupplier;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Predicate;
import org.assertj.core.api.ObjectAssert;
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

    private static final Function<String, String> urlOverride =
            url -> {
                if (url.contains("?ctx=")) {
                    return "/activity-empty.json";
                }
                if (url.endsWith("/activity")) {
                    return "/activity.json";
                }
                return url;
            };

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
                        PullRequestStateFilter.all(),
                        new TestResourceJsonSupplier(urlOverride),
                        jdbi,
                        executor);

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
                            assertThat(pullRequest.getTaskCount()).isEqualTo(2);
                        })
                .anySatisfy(
                        pullRequest -> {
                            assertThat(pullRequest.getTitle())
                                    .describedAs("title")
                                    .isEqualTo("Some open PR in another repo");
                            assertThat(pullRequest.getAge())
                                    .describedAs("age")
                                    .isEqualTo("7d 18h");
                            assertThat(pullRequest.getTaskCount()).isEqualTo(0);
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
                        PullRequestStateFilter.all(),
                        new TestResourceJsonSupplier(urlOverride),
                        jdbi,
                        executor);

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

    @Test
    void bug_mismatched_comment_count() {

        // The comment_count recorded on a PR does not always match the total
        // comments recorded in the activity or comments endpoints. Differences
        // cannot be explained by comment deletion, description vs. diff
        // comments, or PR state.
        //
        // Ingest a PR reporting zero comments, then ingest its activity feed
        // containing two comments, one of which is deleted. Expect
        // comment_count == 1.

        final var executor = new ForkJoinPool();
        final var ingester =
                new BackgroundIngester(
                        PullRequestStateFilter.all(),
                        new TestResourceJsonSupplier(urlOverride),
                        jdbi,
                        executor);

        ingester.ingest(
                "/bug/mismatched-comment-count/pullrequests-no-comments.json");
        if (!executor.awaitQuiescence(1, TimeUnit.SECONDS)) {
            fail("tardy executor");
        }

        final var someClock = Clock.systemUTC();
        final var dao = new UnresolvedReviewDao(jdbi, someClock);
        final var beforeActivity = dao.getOpenPullRequests();

        final var prTitle = "Some open PR reporting zero comments";
        assertThatPrByTitle(beforeActivity, prTitle)
                .extracting(UnresolvedReview::getCommentCount)
                .describedAs("comment count")
                .isEqualTo(0);

        ingester.ingest("/bug/mismatched-comment-count/activity-comments.json");
        if (!executor.awaitQuiescence(1, TimeUnit.SECONDS)) {
            fail("tardy executor");
        }

        final var afterActivity = dao.getOpenPullRequests();
        assertThatPrByTitle(afterActivity, prTitle)
                .extracting(UnresolvedReview::getCommentCount)
                .describedAs("comment count")
                .isEqualTo(1);
    }

    private static ObjectAssert<UnresolvedReview> assertThatPrByTitle(
            List<UnresolvedReview> prs, String title) {
        return assertThat(prs)
                .describedAs("by title: %s", title)
                .filteredOn(hasTitle(title))
                .first();
    }

    private static Predicate<UnresolvedReview> hasTitle(String title) {
        return pr -> title.equals(pr.getTitle());
    }
}
