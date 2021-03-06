package io.gitlab.mkjeldsen.prstatbucket;

import static io.gitlab.mkjeldsen.prstatbucket.PullRequestHandleConsumer.ensureUsers;
import static io.gitlab.mkjeldsen.prstatbucket.PullRequestHandleConsumer.executeBatch;

import io.gitlab.mkjeldsen.prstatbucket.apimodel.PullRequestActivity;
import io.gitlab.mkjeldsen.prstatbucket.apimodel.User;
import java.util.HashSet;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.HandleConsumer;

public final class ActivityHandleConsumer
        implements HandleConsumer<RuntimeException> {

    private static final String SQL_UPSERT_PR_COMMENT =
            ""
                    + "INSERT INTO"
                    + " pull_request_comment (c_url, pr_url, author, content, is_deleted, created_ts)"
                    + " VALUES (:c_url, :pr_url, :author, :content, :is_deleted, :created_ts)"
                    + " ON CONFLICT (c_url) DO UPDATE SET"
                    + " pr_url = excluded.pr_url,"
                    + " author = excluded.author,"
                    + " content = excluded.content,"
                    + " is_deleted = excluded.is_deleted,"
                    + " created_ts = excluded.created_ts";

    private static final String SQL_UPSERT_PR_APPROVAL =
            ""
                    + "INSERT INTO"
                    + " pull_request_approval (pr_url, approver, approval_ts)"
                    + " VALUES (:pr_url, :approver, :approval_ts)"
                    + " ON CONFLICT (pr_url, approver, approval_ts) DO NOTHING";

    private static final String SQL_SET_PR_CLOSED_TS =
            ""
                    + "UPDATE"
                    + " pull_request"
                    + " SET closed_ts = :closed_ts"
                    + " WHERE pr_url = :pr_url"
                    + " AND (closed_ts IS NULL OR :closed_ts < closed_ts)";

    private final PullRequestActivity pullRequestActivity;

    public ActivityHandleConsumer(PullRequestActivity pullRequestActivity) {
        this.pullRequestActivity = pullRequestActivity;
    }

    @Override
    public void useHandle(Handle handle) throws RuntimeException {

        if (pullRequestActivity.closedTs != null) {
            /*
            This is racy with PullRequestHandleConsumer: a pull request may not
            have been persisted yet. In practice, that's okay. We're
            usually delayed by HTTP I/O to retrieve the activity, which is
            slow, and if we fail it will work the next time we synchronize.
            */
            handle.createUpdate(SQL_SET_PR_CLOSED_TS)
                    .bind("closed_ts", pullRequestActivity.closedTs)
                    .bind("pr_url", pullRequestActivity.url)
                    .execute();
        }

        if (!pullRequestActivity.comments.isEmpty()) {
            final var batch = handle.prepareBatch(SQL_UPSERT_PR_COMMENT);

            final var users = new HashSet<User>();
            for (final var c : pullRequestActivity.comments) {
                batch.bind("c_url", c.url)
                        .bind("pr_url", pullRequestActivity.url)
                        .bind("author", c.author.uuid)
                        .bind("content", c.content)
                        .bind("is_deleted", c.deleted)
                        .bind("created_ts", c.createdOn)
                        .add();

                users.add(c.author);
            }
            ensureUsers(handle, users);

            executeBatch(batch);
        }

        if (!pullRequestActivity.approvals.isEmpty()) {
            final var batch = handle.prepareBatch(SQL_UPSERT_PR_APPROVAL);

            final var users = new HashSet<User>();
            for (final var a : pullRequestActivity.approvals) {
                batch.bind("pr_url", pullRequestActivity.url);
                batch.bind("approver", a.approver.uuid);
                batch.bind("approval_ts", a.date);
                batch.add();

                users.add(a.approver);
            }
            ensureUsers(handle, users);

            executeBatch(batch);
        }
    }

    @Override
    public String toString() {
        return "ActivityHandleConsumer{"
                + "pullRequestActivity="
                + pullRequestActivity
                + '}';
    }
}
