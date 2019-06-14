package io.gitlab.mkjeldsen.prstatbucket.unresolved;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.Query;

public final class UnresolvedReviewDao implements UnresolvedReviewService {

    private static final String SQL_OPEN_PRS =
            "SELECT pr_url, destination, title, created_ts FROM pull_request WHERE state = 'OPEN' ORDER BY created_ts ASC, title";

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