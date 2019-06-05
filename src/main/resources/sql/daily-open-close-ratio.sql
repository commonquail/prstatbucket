WITH
pr_opened_by_date AS (
    SELECT
        count(*) AS num_prs,
        created_ts::DATE AS the_date
    FROM pull_request
    GROUP BY the_date
),
pr_opened_stats as (
    SELECT
        extract(isodow FROM the_date) AS day_of_week,
        percentile_cont(0.50) WITHIN GROUP (ORDER BY num_prs) AS median_opened,
        percentile_cont(0.95) WITHIN GROUP (ORDER BY num_prs) AS p95_opened,
        avg(num_prs) AS mean_opened,
        sum(num_prs) AS total_opened
    FROM pr_opened_by_date
    GROUP BY day_of_week
),
pr_closed_by_date AS (
    SELECT
        count(*) AS num_prs,
        closed_ts::DATE AS the_date
    FROM pull_request
    GROUP BY the_date
),
pr_closed_stats as (
    SELECT
        extract(isodow FROM the_date) AS day_of_week,
        percentile_cont(0.50) WITHIN GROUP (ORDER BY num_prs) AS median_closed,
        percentile_cont(0.95) WITHIN GROUP (ORDER BY num_prs) AS p95_closed,
        avg(num_prs) AS mean_closed,
        sum(num_prs) AS total_closed
    FROM pr_closed_by_date
    GROUP BY day_of_week
)
SELECT
    pr_opened_stats.day_of_week,
    median_opened,
    p95_opened,
    mean_opened,
    total_opened,
    median_closed,
    p95_closed,
    mean_closed,
    total_closed
FROM pr_opened_stats
LEFT JOIN pr_closed_stats ON pr_closed_stats.day_of_week = pr_opened_stats.day_of_week
ORDER BY day_of_week;
