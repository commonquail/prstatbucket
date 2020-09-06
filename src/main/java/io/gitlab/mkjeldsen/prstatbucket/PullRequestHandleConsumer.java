package io.gitlab.mkjeldsen.prstatbucket;

import io.gitlab.mkjeldsen.prstatbucket.apimodel.PullRequests;
import io.gitlab.mkjeldsen.prstatbucket.apimodel.User;
import java.util.HashSet;
import java.util.Set;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.HandleConsumer;
import org.jdbi.v3.core.statement.PreparedBatch;

public final class PullRequestHandleConsumer
        implements HandleConsumer<RuntimeException> {

    private static final Object BITBUCKET_USER_MUTEX = new Object();

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

        final var users = new HashSet<User>();
        for (final var pr : pullRequests.values) {
            batch.bind("pr_url", pr.links.html.href)
                    .bind("destination", pr.destination.repository.fullName)
                    .bind("title", pr.title)
                    .bind("state", pr.state)
                    .bind("author", pr.author.uuid)
                    .bind("created_ts", pr.createdOn)
                    .bind("task_count", pr.taskCount)
                    .add();

            users.add(pr.author);
        }
        ensureUsers(handle, users);

        executeBatch(batch);
    }

    @Override
    public String toString() {
        return "PullRequestHandleConsumer{"
                + "pullRequests="
                + pullRequests
                + '}';
    }

    static void ensureUsers(final Handle handle, final Set<User> users) {
        final var sql =
                ""
                        + "INSERT INTO bitbucket_user AS b (user_uuid, nickname)"
                        + " VALUES (:user_uuid, :nickname)"
                        + " ON CONFLICT (user_uuid) DO UPDATE SET"
                        + " nickname = excluded.nickname"
                        + " WHERE b.nickname != excluded.nickname";

        final var batch = handle.prepareBatch(sql);
        for (final var user : users) {
            var uuid = user.uuid;
            var nickname = user.nickname;
            if (nickname.length() > 50) {
                nickname = nickname.substring(0, 50);
            }

            batch.bind("user_uuid", uuid);
            batch.bind("nickname", nickname);
            batch.add();
        }

        // I can't figure out how to UPSERT concurrently. I can't acquire keys
        // in consistent order, so when 2 threads try to update the same user
        // they deadlock. This may have something to do with keys not being
        // visible across non-serializable transactions. I also can't find any
        // explicit examples for concurrent UPSERTs. Splitting into separate
        // INSERT ... DO NOTHING followed by UPDATE ... works completely, though
        // that reduces the deadlock to only the UPDATE statements and fails
        // much less. Not even explicit SERIALIZABLE isolation level works.
        //
        // Obviously this is all just because the concurrency model is FUBAR.
        // I'm certainly not blaming Postgres for any errors here. But how can
        // this not already be a copypasta-solved problem?!
        //
        // This silly shared lock easily mitigates all deadlocks by forcefully
        // obstructing every thread, and the slowest request remains an order of
        // magnitude faster than the slowest (broken) split-statement version.
        synchronized (BITBUCKET_USER_MUTEX) {
            executeBatch(batch);
        }
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
