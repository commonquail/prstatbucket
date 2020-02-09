package io.gitlab.mkjeldsen.prstatbucket.duration;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.StatementContext;

public final class DurationDensityEstimateDao
        implements DurationDensityEstimateService {

    private final Jdbi jdbi;

    public DurationDensityEstimateDao(final Jdbi jdbi) {
        this.jdbi = jdbi;
    }

    @Override
    public List<StartEndRecord> dataFor(final DurationDensityReport report) {
        return jdbi.withHandle(
                handle ->
                        handle.select(report.select())
                                .map(DurationDensityEstimateDao::toRecord)
                                .list());
    }

    private static StartEndRecord toRecord(
            final ResultSet rs, final StatementContext ctx)
            throws SQLException {
        // Query guarantees not-null.
        final long start = rs.getLong("start");
        final long end = rs.getLong("end");
        return new StartEndRecord(start, end);
    }
}
