package io.gitlab.mkjeldsen.prstatbucket.unresolved;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

public final class UnresolvedReviewRowMapper
        implements RowMapper<UnresolvedReview> {

    private final Instant now;

    public UnresolvedReviewRowMapper(Instant now) {
        this.now = now;
    }

    @Override
    public UnresolvedReview map(ResultSet rs, StatementContext ctx)
            throws SQLException {
        final var url = rs.getString("pr_url");
        final var destination = rs.getString("destination");
        final var title = rs.getString("title");

        // SQL's INTERVAL is cumbersome in integration layers so calculate the
        // age in application layer.
        final var createdTs = rs.getTimestamp("created_ts").toInstant();
        final var age = Duration.between(createdTs, now);

        final var commentCount = rs.getInt("comment_count");
        final var taskCount = rs.getInt("task_count");

        return new UnresolvedReview(
                url,
                destination,
                title,
                age,
                prettyAge(age),
                commentCount,
                taskCount);
    }

    private static String prettyAge(Duration age) {
        final StringBuilder str = new StringBuilder();

        long days = age.toDaysPart();
        if (days > 0) {
            str.append(days).append('d').append(' ');
        }

        int hours = age.toHoursPart();
        if (hours > 0) {
            str.append(hours).append('h').append(' ');
        }

        int minutes = age.toMinutesPart();
        if (minutes > 0) {
            str.append(minutes).append('m');
        } else {
            // str may end with a space, which we don't want. Although this can
            // happen in the wild iff 0s <= age < 60s, in tests running on a
            // dirty database it happens frequently: pull requests created after
            // test code is eventually created "in the future", such that
            // age.isNegative() == true. In that case this trim would trigger
            // out-of-bounds without the guard.

            final int end;
            if ((end = str.length() - 1) > 0) {
                str.deleteCharAt(end);
            }
        }

        return str.toString();
    }
}
