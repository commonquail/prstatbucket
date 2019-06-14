package io.gitlab.mkjeldsen.prstatbucket;

import io.gitlab.mkjeldsen.prstatbucket.apimodel.PullRequests;
import java.nio.charset.StandardCharsets;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.HandleConsumer;
import org.springframework.util.DigestUtils;

public final class PullRequestHandleConsumer
        implements HandleConsumer<RuntimeException> {

    private static final String SQL =
            ""
                    + "INSERT INTO"
                    + " pull_request (pr_url, destination, title, author, state, created_ts)"
                    + " VALUES (:pr_url, :destination, :title, :author, :state, :created_ts)"
                    + " ON CONFLICT (pr_url) DO UPDATE SET"
                    + " destination = excluded.destination,"
                    + " state = excluded.state,"
                    + " title = excluded.title";

    private final PullRequests pullRequests;

    public PullRequestHandleConsumer(PullRequests pullRequests) {
        this.pullRequests = pullRequests;
    }

    @Override
    public void useHandle(Handle handle) throws RuntimeException {
        final var batch = handle.prepareBatch(SQL);

        for (final var pr : pullRequests.values) {
            batch.bind("pr_url", pr.links.html.href)
                    .bind("destination", pr.destination.repository.fullName)
                    .bind("title", pr.title)
                    .bind("state", pr.state)
                    .bind("author", hash(pr.author.uuid.toString()))
                    .bind("created_ts", pr.createdOn)
                    .add();
        }

        batch.execute();
    }

    @Override
    public String toString() {
        return "PullRequestHandleConsumer{"
                + "pullRequests="
                + pullRequests
                + '}';
    }

    static String hash(String author) {
        var authorBytes = author.getBytes(StandardCharsets.UTF_8);
        return DigestUtils.md5DigestAsHex(authorBytes);
    }
}
