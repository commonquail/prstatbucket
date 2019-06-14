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

        return new UnresolvedReview(
                url, destination, title, age, prettyAge(age));
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
            str.deleteCharAt(str.length() - 1);
        }

        return str.toString();
    }
}
