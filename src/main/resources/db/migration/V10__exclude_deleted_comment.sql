CREATE OR REPLACE VIEW report_pr_comment_ts_extremum AS
SELECT
    pr_url,
    min(created_ts) AS first_ts,
    max(created_ts) AS last_ts
FROM pull_request_comment
WHERE
    NOT is_deleted
GROUP BY pr_url;
