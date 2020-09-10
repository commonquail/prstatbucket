package io.gitlab.mkjeldsen.prstatbucket;

import io.gitlab.mkjeldsen.prstatbucket.apimodel.PullRequests;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.HandleConsumer;
import org.jdbi.v3.core.statement.PreparedBatch;

public final class PullRequestHandleConsumer
        implements HandleConsumer<RuntimeException> {

    private static final String SQL =
            ""
                    + "INSERT INTO"
                    + " pull_request (pr_url, destination, title, author, state, created_ts, task_count)"
                    + " VALUES (:pr_url, :destination, :title, :author, :state, :created_ts, :task_count)"
                    + " ON CONFLICT (pr_url) DO UPDATE SET"
                    + " destination = excluded.destination,"
                    + " author = excluded.author,"
                    + " state = excluded.state,"
                    + " task_count = excluded.task_count,"
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
                    .bind("author", pr.author.uuid)
                    .bind("created_ts", pr.createdOn)
                    .bind("task_count", pr.taskCount)
                    .add();
        }

        executeBatch(batch);
    }

    @Override
    public String toString() {
        return "PullRequestHandleConsumer{"
                + "pullRequests="
                + pullRequests
                + '}';
    }

    /**
     * Safely executes the provided batch.
     *
     * <p>{@link PreparedBatch#execute() Executing} an empty prepared batch
     * yields a runtime error. This usually happens when a batch is populated by
     * a loop over a collection that in some rare cases can be empty. If the
     * provided batch is empty, this method does nothing.
     *
     * @param batch the prepared batch to execute
     */
    static void executeBatch(final PreparedBatch batch) {
        if (batch.size() > 0) {
            batch.execute();
        }
    }
}
