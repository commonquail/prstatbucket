package io.gitlab.mkjeldsen.prstatbucket;

import static io.gitlab.mkjeldsen.prstatbucket.BackgroundIngester.objectMapper;
import static org.assertj.core.api.Assertions.assertThat;

import io.gitlab.mkjeldsen.prstatbucket.apimodel.Approval;
import io.gitlab.mkjeldsen.prstatbucket.apimodel.Comment;
import io.gitlab.mkjeldsen.prstatbucket.apimodel.Link;
import io.gitlab.mkjeldsen.prstatbucket.apimodel.ProfileLinks;
import io.gitlab.mkjeldsen.prstatbucket.apimodel.PullRequest;
import io.gitlab.mkjeldsen.prstatbucket.apimodel.PullRequestActivity;
import io.gitlab.mkjeldsen.prstatbucket.apimodel.PullRequests;
import io.gitlab.mkjeldsen.prstatbucket.apimodel.User;
import java.io.IOException;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.UUID;
import org.junit.jupiter.api.Test;

final class ObjectMapperTest {

    @Test
    void pullrequests() throws IOException {
        var pullRequests = readJsonAs("/pullrequests.json", PullRequests.class);

        var merged = pullRequests.values.get(0);
        var ctx = "merged";

        assertThat(merged.destination.repository.fullName)
                .as("[%s] repository name", ctx)
                .isEqualTo("repoowner1/repo1");
        assertThat(merged.title)
                .as("[%s] pull request title", ctx)
                .isEqualTo("Some merged PR");
        assertThat(merged.author.nickname)
                .as("[%s] author nickname", ctx)
                .isEqualTo("Repo Owner 1");
        assertThat(merged.author.uuid)
                .as("[%s] author uuid", ctx)
                .isEqualTo(
                        UUID.fromString(
                                "b1ec23cd-9465-4ed3-afc2-a4cc612757b4"));
        assertThat(merged.state)
                .as("[%s] state", ctx)
                .isSameAs(PullRequest.State.MERGED);
        assertThat(merged.taskCount).as("[%s] tasks", ctx).isEqualTo(0);
        assertThat(merged.links.activity.href)
                .as("[%s] activity href", ctx)
                .isEqualTo(
                        "https://api.bitbucket.org/2.0/repositories/repoowner1/repo1/pullrequests/3/activity");
        assertThat(merged.createdOn)
                .as("[%s] created ts", ctx)
                .isEqualTo(utc("2019-05-15T08:35:07.724888+00:00"));

        var open = pullRequests.values.get(1);
        ctx = "open";

        assertThat(open.destination.repository.fullName)
                .as("[%s] repository name", ctx)
                .isEqualTo("repoowner1/repo1");
        assertThat(open.title)
                .as("[%s] pull request title", ctx)
                .isEqualTo("Some open PR");
        assertThat(open.author.nickname)
                .as("[%s] author nickname", ctx)
                .isEqualTo("Repo Owner 1");
        assertThat(open.author.uuid)
                .as("[%s] author uuid", ctx)
                .isEqualTo(
                        UUID.fromString(
                                "b1ec23cd-9465-4ed3-afc2-a4cc612757b4"));
        assertThat(open.state)
                .as("[%s] state", ctx)
                .isSameAs(PullRequest.State.OPEN);
        assertThat(open.taskCount).as("[%s] tasks", ctx).isEqualTo(2);
        assertThat(open.links.activity.href)
                .as("[%s] activity href", ctx)
                .isEqualTo(
                        "https://api.bitbucket.org/2.0/repositories/repoowner1/repo1/pullrequests/2/activity");
        assertThat(open.createdOn)
                .as("[%s] created ts", ctx)
                .isEqualTo(utc("2019-03-06T19:20:32.157622+00:00"));

        var declined = pullRequests.values.get(2);
        ctx = "declined";

        assertThat(declined.destination.repository.fullName)
                .as("[%s] repository name", ctx)
                .isEqualTo("repoowner1/repo1");
        assertThat(declined.title)
                .as("[%s] pull request title", ctx)
                .isEqualTo("Some declined PR");
        assertThat(declined.author.nickname)
                .as("[%s] author nickname", ctx)
                .isEqualTo("Repo Owner 1");
        assertThat(declined.author.uuid)
                .as("[%s] author uuid", ctx)
                .isEqualTo(
                        UUID.fromString(
                                "b1ec23cd-9465-4ed3-afc2-a4cc612757b4"));
        assertThat(declined.state)
                .as("[%s] state", ctx)
                .isSameAs(PullRequest.State.DECLINED);
        assertThat(declined.taskCount).as("[%s] tasks", ctx).isEqualTo(0);
        assertThat(declined.links.activity.href)
                .as("[%s] activity href", ctx)
                .isEqualTo(
                        "https://api.bitbucket.org/2.0/repositories/repoowner1/repo1/pullrequests/1/activity");
        assertThat(declined.createdOn)
                .as("[%s] created ts", ctx)
                .isEqualTo(utc("2019-03-06T19:19:02.576925+00:00"));
    }

    @Test
    void pullrequests_activity() throws IOException {
        var activity = readJsonAs("/activity.json", PullRequestActivity.class);

        assertThat(activity.getNext())
                .contains(
                        "https://api.bitbucket.org/2.0/repositories/repoowner1/repo1/pullrequests/2/activity?ctx=b8f6tM");

        assertThat(activity.url)
                .isEqualTo(
                        "https://bitbucket.org/repoowner1/repo1/pull-requests/2");

        assertThat(activity.closedTs)
                .isEqualTo(utc("2019-05-08T18:15:59.514007+00:00"));

        assertThat(activity.comments)
                .containsExactlyInAnyOrder(
                        new Comment(
                                "https://bitbucket.org/repoowner1/repo1/pull-requests/2/_/diff#comment-101358731",
                                "Sample comment",
                                utc("2019-05-08T18:15:54.204435+00:00"),
                                false,
                                User.DELETED),
                        new Comment(
                                "https://bitbucket.org/repoowner1/repo1/pull-requests/2/_/diff#comment-94963965",
                                "Deleted comment",
                                utc("2019-03-14T07:47:30.666498+00:00"),
                                true,
                                author1()),
                        new Comment(
                                "https://bitbucket.org/repoowner1/repo1/pull-requests/2/_/diff#comment-94891166",
                                "Comment with parent, different author",
                                utc("2019-03-13T16:45:48.364789+00:00"),
                                false,
                                author2()));

        assertThat(activity.approvals)
                .containsOnly(
                        new Approval(
                                utc("2019-05-15T09:25:58.674877+00:00"),
                                repoowner()));
    }

    private <T> T readJsonAs(String filename, Class<T> type)
            throws IOException {
        var json = getClass().getResource(filename);
        return objectMapper().readerFor(type).readValue(json);
    }

    private static Instant utc(String s) {
        // Instant::parse can't parse the timestamp format used throughout.
        return ZonedDateTime.parse(s).toInstant();
    }

    private static User repoowner() {
        return new User(
                "repoowner1",
                "Repo Owner 1",
                "557057:44a78471-963c-4b63-8a5f-994c2d75613d",
                new ProfileLinks(
                        new Link(
                                "https://api.bitbucket.org/2.0/users/%7Bdc0d7577-9404-482a-85af-9a8a379ab471%7D"),
                        new Link(
                                "https://bitbucket.org/%7Bdc0d7577-9404-482a-85af-9a8a379ab471%7D/"),
                        new Link("https://imgsrc.example")),
                "repoowner1",
                "user",
                "{dc0d7577-9404-482a-85af-9a8a379ab471}");
    }

    private static User author1() {
        return new User(
                "author1",
                "Author 1",
                "637029:cdf29ef8-cd86-48a3-997e-f55afebd70eb",
                new ProfileLinks(
                        new Link(
                                "https://api.bitbucket.org/2.0/users/%7B58dbd2a5-66ef-4367-b5ea-f2301e374449%7D"),
                        new Link(
                                "https://bitbucket.org/%7B58dbd2a5-66ef-4367-b5ea-f2301e374449%7D/"),
                        new Link("https://imgsrc.example")),
                "Author 1",
                "user",
                "{58dbd2a5-66ef-4367-b5ea-f2301e374449}");
    }

    private static User author2() {
        return new User(
                "author2",
                "Author 2",
                "637029:f1dcf0dd-43b2-46ff-be64-f51ef8a46db9",
                new ProfileLinks(
                        new Link(
                                "https://api.bitbucket.org/2.0/users/%7B25a9e1a0-65d4-497a-a9cd-968822cb427e%7D"),
                        new Link(
                                "https://bitbucket.org/%7B25a9e1a0-65d4-497a-a9cd-968822cb427e%7D/"),
                        new Link("https://imgsrc.example")),
                "Author 2",
                "user",
                "{25a9e1a0-65d4-497a-a9cd-968822cb427e}");
    }
}
