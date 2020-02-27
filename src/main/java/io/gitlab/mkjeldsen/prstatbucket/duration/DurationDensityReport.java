package io.gitlab.mkjeldsen.prstatbucket.duration;

public enum DurationDensityReport {
    ttfa("first", "report_pr_approval_ts_extremum"),
    ttla("last", "report_pr_approval_ts_extremum"),
    ttfc("first", "report_pr_comment_ts_extremum"),
    ttlc("last", "report_pr_comment_ts_extremum"),
    ttff("first", "report_pr_feedback_ts_extremum"),
    cycle_time(
            "SELECT "
                    + tsToEpochMs("created_ts")
                    + " AS start, "
                    + tsToEpochMs("closed_ts")
                    + " AS end"
                    + " FROM pull_request"
                    + " WHERE closed_ts IS NOT NULL");

    private final String sql;

    DurationDensityReport(final String sql) {
        this.sql = sql;
    }

    DurationDensityReport(final String firstOrLast, final String bakedReport) {
        final var endColumn = firstOrLast + "_ts";
        this.sql =
                "SELECT "
                        + tsToEpochMs("pr_created_ts")
                        + " AS start, "
                        + tsToEpochMs(endColumn)
                        + " AS end"
                        + " FROM report_pr_ts"
                        + " INNER JOIN "
                        + bakedReport
                        + " USING (pr_url)"
                        + " WHERE "
                        + endColumn
                        + " IS NOT NULL";
    }

    public String select() {
        return sql;
    };

    private static String tsToEpochMs(final String column) {
        return "CAST(EXTRACT(EPOCH FROM " + column + ") * 1000 AS bigint)";
    }
}
