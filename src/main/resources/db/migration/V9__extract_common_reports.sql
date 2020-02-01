CREATE OR REPLACE VIEW report_pr_ts AS
SELECT
    pr_url,
    state,
    created_ts AS pr_created_ts,
    closed_ts  AS pr_closed_ts,
    closed_ts - created_ts AS cycle_time
FROM pull_request;

COMMENT ON VIEW report_pr_ts IS
'Reports commonly used pull request time metrics.';

CREATE OR REPLACE VIEW report_pr_comment_ts_extremum AS
SELECT
    pr_url,
    min(created_ts) AS first_ts,
    max(created_ts) AS last_ts
FROM pull_request_comment
GROUP BY pr_url;

COMMENT ON VIEW report_pr_comment_ts_extremum IS
'Reports earliest and latest timestamp of comments by pull request.';

CREATE OR REPLACE VIEW report_pr_approval_ts_extremum AS
SELECT
    pr_url,
    min(approval_ts) AS first_ts,
    max(approval_ts) AS last_ts
FROM pull_request_approval
GROUP BY pr_url;

COMMENT ON VIEW report_pr_approval_ts_extremum IS
'Reports earliest and latest timestamp of approvals by pull request.';

CREATE OR REPLACE VIEW report_pr_feedback_ts_extremum AS
SELECT
    pr_url,
    min(first_ts) AS first_ts
FROM (
    SELECT pr_url, first_ts FROM report_pr_comment_ts_extremum
    UNION ALL
    SELECT pr_url, first_ts FROM report_pr_approval_ts_extremum
) x
GROUP BY pr_url;

COMMENT ON VIEW report_pr_feedback_ts_extremum IS
'Reports earliest and latest timestamp of feedback by pull request.

"Feedback" includes comments and approvals.

There is no `last_ts`. Whether "last feedback" should mean
"latest first-feedback" or "latest last-feedback" is unclear, and the latter
seems of little use.';

CREATE OR REPLACE VIEW report_pr_comment_tt AS
SELECT
    pr_url,
    first_ts - pr_created_ts AS ttfc,
    last_ts  - pr_created_ts AS ttlc
FROM report_pr_ts
INNER JOIN report_pr_comment_ts_extremum USING (pr_url);

COMMENT ON VIEW report_pr_comment_tt IS
'Reports aggregates for TTFC and TTLC reports.';

CREATE OR REPLACE VIEW report_pr_approval_tt AS
SELECT
    pr_url,
    first_ts - pr_created_ts AS ttfa,
    last_ts  - pr_created_ts AS ttla
FROM report_pr_ts
INNER JOIN report_pr_approval_ts_extremum USING (pr_url);

COMMENT ON VIEW report_pr_approval_tt IS
'Reports aggregates for TTFA and TTLA reports.';

CREATE OR REPLACE VIEW report_pr_feedback_tt AS
SELECT
    pr_url,
    first_ts - pr_created_ts AS ttff
FROM report_pr_ts
INNER JOIN report_pr_feedback_ts_extremum USING (pr_url);

COMMENT ON VIEW report_pr_feedback_tt IS
'Reports aggregates for TTFF report.

There is no TTLF. Whether "last feedback" should mean "latest first-feedback"
or "latest last-feedback" is unclear, and the latter seems of little use.';

CREATE OR REPLACE FUNCTION pr_report_cycle_time_hist(
    bucket_count integer,
    time_limit interval,
    states text[] DEFAULT ARRAY['MERGED', 'DECLINED']
)
RETURNS TABLE(bucket integer, num_hours int4range, freq bigint, perc float, bar text)
AS $func$
SELECT bucket, bucket_range, freq, perc, bar FROM discrete_histogram(
    format(
        $$
            (
                SELECT
                    extract(epoch FROM cycle_time) / 3600 AS cycle_time
                FROM report_pr_ts
                WHERE state = any(%L)
                AND cycle_time <= %L::interval
            )
        $$,
        states,
        time_limit
    ),
    'cycle_time',
    bucket_count
)
$func$
LANGUAGE SQL;

CREATE OR REPLACE FUNCTION pr_report_ttfc_hist(
    bucket_count integer,
    time_limit interval
)
RETURNS TABLE(bucket integer, ttfc_hours int4range, freq bigint, perc float, bar text)
AS $func$
SELECT bucket, bucket_range, freq, perc, bar FROM discrete_histogram(
    format(
        $$
            (
                SELECT
                    cast(extract(epoch FROM ttfc) / 3600 AS integer) AS ttfc_hours
                FROM report_pr_comment_tt
                WHERE ttfc <= %L::interval
            )
        $$,
        time_limit
    ),
    'ttfc_hours',
    bucket_count
)
$func$
LANGUAGE SQL;

CREATE OR REPLACE FUNCTION pr_report_ttlc_hist(
    bucket_count integer,
    time_limit interval
)
RETURNS TABLE(bucket integer, ttlc_hours int4range, freq bigint, perc float, bar text)
AS $func$
SELECT bucket, bucket_range, freq, perc, bar FROM discrete_histogram(
    format(
        $$
            (
                SELECT
                    cast(extract(epoch FROM last_ts - pr_created_ts) / 3600 AS integer) AS ttlc_hours
                FROM report_pr_ts
                INNER JOIN report_pr_comment_ts_extremum USING (pr_url)
                WHERE last_ts - pr_created_ts <= %L::interval
            )
        $$,
        time_limit
    ),
    'ttlc_hours',
    bucket_count
)
$func$
LANGUAGE SQL;

CREATE OR REPLACE FUNCTION pr_report_ttfa_hist(
    bucket_count integer,
    time_limit interval
)
RETURNS TABLE(bucket integer, ttfa_hours int4range, freq bigint, perc float, bar text)
AS $func$
SELECT bucket, bucket_range, freq, perc, bar FROM discrete_histogram(
    format(
        $$
            (
                SELECT
                    cast(extract(epoch FROM ttfa) / 3600 AS integer) AS ttfa_hours
                FROM report_pr_approval_tt
                WHERE ttfa <= %L::interval
            )
        $$,
        time_limit
    ),
    'ttfa_hours',
    bucket_count
)
$func$
LANGUAGE SQL;

CREATE OR REPLACE FUNCTION pr_report_ttff_hist(
    bucket_count integer,
    time_limit interval
)
RETURNS TABLE(bucket integer, ttff_hours int4range, freq bigint, perc float, bar text)
AS $func$
SELECT bucket, bucket_range, freq, perc, bar FROM discrete_histogram(
    format(
        $$
            (
                SELECT
                    cast(extract(epoch FROM ttff) / 3600 AS integer) AS ttff_hours
                FROM report_pr_feedback_tt
                WHERE ttff <= %L::interval
            )
        $$,
        time_limit
    ),
    'ttff_hours',
    bucket_count
)
$func$
LANGUAGE SQL;
