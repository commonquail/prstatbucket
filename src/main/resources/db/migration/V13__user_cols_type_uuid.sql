-- Since we store user column values as UUIDs it would be nice if we could take
-- advantage of Postgres' built-in support for UUID types. Fortunately, we can:
-- we know that every value stored by the application up until this point is
-- either a UUID (now), which uses 128 bits, or an MD5 hash (previously), which
-- also happens to be 128 bits. This means we can quite safely convert all saved
-- values to UUIDs using just the built-in text-to-UUID casting. Many of those
-- values will be absolute bogus but that doesn't really matter, the
-- relationships remain unchanged and running the ingestion again will simply
-- update the stored values.

-- Make views dependent on user columns into no-ops so we can free the columns
-- for structural changes.

CREATE OR REPLACE VIEW report_pr_comment_ts_extremum AS
SELECT '' pr_url, localtimestamp AS first_ts, localtimestamp AS last_ts;

CREATE OR REPLACE VIEW report_pr_approval_ts_extremum AS
SELECT '' pr_url, localtimestamp AS first_ts, localtimestamp AS last_ts;

-- Now change the user columns from bare text to UUID. This can only fail if a
-- human operator manipulated the ingestion pipeline or stored data directly, in
-- which case all bets are off.

ALTER TABLE pull_request
    ALTER COLUMN author
    SET DATA TYPE uuid USING author::uuid;

ALTER TABLE pull_request_comment
    ALTER COLUMN author
    SET DATA TYPE uuid USING author::uuid;

ALTER TABLE pull_request_approval
    ALTER COLUMN approver
    SET DATA TYPE uuid USING approver::uuid;

-- Finally restore the dependent views. Their definition and behaviour is
-- unchanged, it's just a necessary step.

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
