CREATE TABLE pull_request_approval(
    a_id SERIAL NOT NULL,
    pr_url TEXT NOT NULL REFERENCES pull_request (pr_url),
    approver TEXT NOT NULL,
    approval_ts TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    PRIMARY KEY (a_id),
    UNIQUE (pr_url, approver, approval_ts)
);

CREATE INDEX ON pull_request_approval (approval_ts);

COMMENT ON TABLE pull_request_approval IS
'Represents every approval of a pull request identified by
`pull_request.pr_url`.

`a_id` is a surrogate key.';

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
                WITH
                prtimes AS (
                    SELECT
                        pr_url,
                        created_ts AS pr_ts
                    FROM pull_request
                ),
                atimes AS (
                    SELECT
                        pr_url,
                        min(approval_ts) AS a_ts
                    FROM pull_request_approval
                    GROUP BY pr_url
                )
                    SELECT
                        cast(extract(epoch FROM a_ts - pr_ts) / 3600 AS integer) AS ttfa_hours
                    FROM prtimes
                    INNER JOIN atimes USING (pr_url)
                    WHERE a_ts - pr_ts <= %L::interval
            )
        $$,
        time_limit
    ),
    'ttfa_hours',
    bucket_count
)
$func$
LANGUAGE SQL;

COMMENT ON FUNCTION pr_report_ttfa_hist IS
'Generates a histogram of the _TTFA_ of pull requests whose TTFA is not greater
than the specified time limit.

_TTFA_ is "Time To First Approval": how soon after a pull request''s creation is
it approved for the first time? Lower numbers imply speedier resolution.

Pull requests with no approvals are ignored. Reversed approvals ("unapprovals")
are ignored.';


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
                ),
                atimes AS (
                    SELECT
                        pr_url,
                        min(approval_ts) AS a_ts
                    FROM pull_request_approval
                    GROUP BY pr_url
                ),
                ftimes AS (
                    SELECT pr_url, min(f_ts) AS f_ts FROM (
                        SELECT
                            pr_url,
                            min(c_ts) AS f_ts
                        FROM ctimes
                        GROUP BY pr_url
                        UNION ALL
                        SELECT
                            pr_url,
                            min(a_ts) AS f_ts
                        FROM atimes
                        GROUP BY pr_url
                    ) x GROUP BY pr_url
                )
                    SELECT
                        cast(extract(epoch FROM f_ts - pr_ts) / 3600 AS integer) AS ttff_hours
                    FROM prtimes
                    INNER JOIN ftimes USING (pr_url)
                    WHERE f_ts - pr_ts <= %L::interval
            )
        $$,
        time_limit
    ),
    'ttff_hours',
    bucket_count
)
$func$
LANGUAGE SQL;

COMMENT ON FUNCTION pr_report_ttff_hist IS
'Generates a histogram of the _TTFF_ of pull requests whose TTFF is not greater
than the specified time limit.

_TTFF_ is "Time To First Feedback": how soon after a pull request''s creation
does the first reviewer feedback arrive? TTFF aggregates the lower ("better")
value of TTFC and TTFA as the first piece of information that an author could
possibly react to. Consequently, it has the same limitations as TTFC and TTFA.';
