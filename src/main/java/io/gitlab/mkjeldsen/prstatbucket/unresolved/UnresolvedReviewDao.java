package io.gitlab.mkjeldsen.prstatbucket.unresolved;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.Query;

public final class UnresolvedReviewDao implements UnresolvedReviewService {

    private static final String SQL_OPEN_PRS =
            ""
                    + "WITH comment_count AS"
                    + " (SELECT pr_url, count(*) AS comment_count FROM pull_request_comment"
                    + " WHERE NOT is_deleted GROUP BY pr_url),"
                    + " approval_count AS ("
                    + "     SELECT pr_url, count(*) AS approval_count"
                    + "     FROM pull_request_approval"
                    + "     GROUP BY pr_url)"
                    + " SELECT"
                    + " pr_url,"
                    + " destination,"
                    + " title,"
                    + " coalesce(comment_count.comment_count, 0) AS comment_count,"
                    + " task_count,"
                    + " coalesce(approval_count.approval_count, 0) AS approval_count,"
                    + " created_ts"
                    + " FROM pull_request"
                    + " LEFT OUTER JOIN comment_count USING (pr_url)"
                    + " LEFT OUTER JOIN approval_count USING (pr_url)"
                    + " WHERE state = 'OPEN' ORDER BY created_ts ASC, title";

    private final Clock clock;

    private final Jdbi jdbi;

    public UnresolvedReviewDao(Jdbi jdbi, Clock clock) {
        this.jdbi = jdbi;
        this.clock = clock;
    }

    @Override
    public List<UnresolvedReview> getOpenPullRequests() {
        return jdbi.withHandle(this::fetchOpenPrs);
    }

    private List<UnresolvedReview> fetchOpenPrs(Handle handle) {
        final Query select = handle.select(SQL_OPEN_PRS);
        final Instant now = clock.instant();
        return select.map(new UnresolvedReviewRowMapper(now)).list();
    }
}
