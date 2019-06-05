SELECT
    extract(isodow FROM created_ts) day_of_week,
    avg(closed_ts - created_ts) AS cycletime
FROM pull_request
WHERE closed_ts - created_ts < '11d'::interval
GROUP BY day_of_week
ORDER BY day_of_week;