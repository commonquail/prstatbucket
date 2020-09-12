package io.gitlab.mkjeldsen.prstatbucket;

import static org.assertj.core.api.Assertions.assertThat;

import io.gitlab.mkjeldsen.prstatbucket.testhelper.TestIngester;
import io.gitlab.mkjeldsen.prstatbucket.unresolved.Savepoint;
import java.util.UUID;
import java.util.function.Function;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.EnabledIf;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@EnabledIf("${smoke.tests.enabled:false}")
final class BitbucketUserIntTest {

    @Autowired
    private Jdbi jdbi;

    private Savepoint savepoint;

    private static final Function<String, String> urlOverride =
            url -> {
                if (url.contains("/activity")) {
                    return "/activity-empty.json";
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
    void upserts_nickname() {
        final var ingester = new TestIngester(jdbi, urlOverride);

        final var userUuid = "58113816-08a2-4975-8b2d-2659a5a409df";

        ingester.ingest("/bitbucket-user/initial-name.json");

        final var initialName = selectNicknameFor(userUuid);
        assertThat(initialName).isEqualTo("Some Initial Nickname");

        ingester.ingest("/bitbucket-user/rename.json");

        final var laterName = selectNicknameFor(userUuid);

        assertThat(laterName).isEqualTo("Some Changed Nickname");
    }

    @Test
    void shortens_too_long_nickname() {
        final var ingester = new TestIngester(jdbi, urlOverride);

        ingester.ingest("/bitbucket-user/too-long-name.json");

        final var nickname =
                selectNicknameFor("e7e20150-93ca-4ef8-8504-8ec9a9cde131");

        assertThat(nickname).hasSize(50).endsWith("y");
    }

    private String selectNicknameFor(final String uuid) {
        return jdbi.withHandle(
                h -> {
                    final var sql =
                            "SELECT nickname FROM bitbucket_user WHERE user_uuid = ?";
                    final var select = h.select(sql, UUID.fromString(uuid));
                    return select.mapTo(String.class).one();
                });
    }
}
