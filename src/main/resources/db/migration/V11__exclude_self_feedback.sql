CREATE OR REPLACE VIEW report_pr_comment_ts_extremum AS
SELECT
    c.pr_url,
    min(c.created_ts) AS first_ts,
    max(c.created_ts) AS last_ts
FROM pull_request_comment AS c
INNER JOIN pull_request AS p ON p.pr_url = c.pr_url
WHERE
    NOT is_deleted
    AND p.author != c.author
GROUP BY c.pr_url;

CREATE OR REPLACE VIEW report_pr_approval_ts_extremum AS
SELECT
    a.pr_url,
    min(a.approval_ts) AS first_ts,
    max(a.approval_ts) AS last_ts
FROM pull_request_approval AS a
INNER JOIN pull_request AS p ON p.pr_url = a.pr_url
WHERE p.author != a.approver
GROUP BY a.pr_url;
