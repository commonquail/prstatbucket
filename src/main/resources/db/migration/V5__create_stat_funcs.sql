DROP FUNCTION IF EXISTS discrete_histogram;
CREATE FUNCTION discrete_histogram(
    table_name_or_subquery text,
    column_name text,
    bucket_count integer default 20
)
RETURNS TABLE(bucket integer, bucket_range int4range, freq bigint, perc float, bar text)
AS $func$
BEGIN
RETURN QUERY EXECUTE format(
    $$
        WITH
        source AS (
            SELECT * FROM %s AS _x
        ),
        min_max AS (
            SELECT min(%I) AS min, max(%I) AS max FROM source
        ),
        histogram AS (
            SELECT
                width_bucket(%I, min_max.min, min_max.max, %L) AS bucket,
                int4range(
                    min(%I)::integer,
                    max(%I)::integer,
                    '[]') AS bucket_range,
                count(%I) AS freq
          FROM source, min_max
          WHERE %I IS NOT NULL
          GROUP BY bucket
          ORDER BY bucket
        )
        SELECT
            bucket,
            bucket_range,
            freq::bigint,
            (freq::float / (sum(freq) over()) * 100) as perc,
            repeat('■', (freq::float / (max(freq) over() + 1) * 30)::integer) AS bar
        FROM histogram
    $$,
    table_name_or_subquery,
    column_name,
    column_name,
    column_name,
    bucket_count,
    column_name,
    column_name,
    column_name,
    column_name
    );
END
$func$ LANGUAGE plpgsql;

COMMENT ON FUNCTION discrete_histogram IS
'Generates a histogram of the discrete numbers in the named column of the
supplied table or subquery. If subquery, it _must_ be wrapped in parentheses.';


DROP FUNCTION IF EXISTS pr_report_total_comment_hist;
CREATE FUNCTION pr_report_total_comment_hist(bucket_count integer)
RETURNS TABLE(bucket integer, num_comments int4range, freq bigint, perc float, bar text)
AS $func$
SELECT bucket, bucket_range, freq, perc, bar FROM discrete_histogram(
    $$
        (
            SELECT
                count(*) AS num_comments
            FROM pull_request
            INNER JOIN pull_request_comment USING (pr_url)
            GROUP BY pr_url
        )
    $$,
    'num_comments',
    bucket_count
)
$func$
LANGUAGE SQL;

COMMENT ON FUNCTION pr_report_total_comment_hist IS
'Generates a histogram of the total number of comments per pull request. Higher
numbers imply technical or procedural misalignment.';


DROP FUNCTION IF EXISTS pr_report_cycle_time_hist;
CREATE FUNCTION pr_report_cycle_time_hist(
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
                    extract(epoch FROM (closed_ts - created_ts)) / 3600 AS cycle_time
                FROM pull_request
                WHERE state = any(%L)
                AND closed_ts - created_ts <= %L::interval
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

COMMENT ON FUNCTION pr_report_cycle_time_hist IS
'Generates a histogram of the _cycle time_ of closed pull requests whose cycle
time is not greater than the specified limit.

A pull request''s cycle time is how much time passes from the pull request being
created until it is closed.';


DROP FUNCTION IF EXISTS pr_report_ttfc_hist;
CREATE FUNCTION pr_report_ttfc_hist(
    bucket_count integer,
    time_limit interval
)
RETURNS TABLE(bucket integer, ttfc_hours int4range, freq bigint, perc float, bar text)
AS $func$
SELECT bucket, bucket_range, freq, perc, bar FROM discrete_histogram(
    format(
        $$
            (
                WITH
                prtimes AS (
                    SELECT
                        pr_url,
                        created_ts AS pr_ts
                    FROM pull_request
                ),
                ctimes AS (
                    SELECT
                        pr_url,
                        min(created_ts) AS c_ts
                    FROM pull_request_comment
                    GROUP BY pr_url
                )
                    SELECT
                        cast(extract(epoch FROM c_ts - pr_ts) / 3600 AS integer) AS ttfc_hours
                    FROM prtimes
                    INNER JOIN ctimes USING (pr_url)
                    WHERE c_ts - pr_ts <= %L::interval
            )
        $$,
        time_limit
    ),
    'ttfc_hours',
    bucket_count
)
$func$
LANGUAGE SQL;

COMMENT ON FUNCTION pr_report_ttfc_hist IS
'Generates a histogram of the _TTFC_ of pull requests whose TTFC is not greater
than the specified time limit.

_TTFC_ is "Time To First Comment": how soon after a pull request''s creation is
the first comment made? Lower numbers imply speedier resolution.

Pull requests with no comments are ignored.';


DROP FUNCTION IF EXISTS pr_report_ttlc_hist;
CREATE FUNCTION pr_report_ttlc_hist(
    bucket_count integer,
    time_limit interval
)
RETURNS TABLE(bucket integer, ttlc_hours int4range, freq bigint, perc float, bar text)
AS $func$
SELECT bucket, bucket_range, freq, perc, bar FROM discrete_histogram(
    format(
        $$
            (
                WITH
                prtimes AS (
                    SELECT
                        pr_url,
                        created_ts AS pr_ts
                    FROM pull_request
                ),
                ctimes AS (
                    SELECT
                        pr_url,
                        max(created_ts) AS c_ts
                    FROM pull_request_comment
                    GROUP BY pr_url
                )
                    SELECT
                        cast(extract(epoch FROM c_ts - pr_ts) / 3600 AS integer) AS ttlc_hours
                    FROM prtimes
                    INNER JOIN ctimes USING (pr_url)
                    WHERE c_ts - pr_ts <= %L::interval
            )
        $$,
        time_limit
    ),
    'ttlc_hours',
    bucket_count
)
$func$
LANGUAGE SQL;

COMMENT ON FUNCTION pr_report_ttlc_hist IS
'Generates a histogram of the _TTLC_ of pull requests whose TTLC is not greater
than the specified time limit.

_TTLC_ is "Time To Last Comment": how long after a pull request''s creation is
the last comment made? Higher numbers imply drawn-out resolution.

Pull requests with no comments are ignored.';
