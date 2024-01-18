SELECT attname, n_distinct, most_common_vals::text::text[] as most_common_vals, most_common_freqs, histogram_bounds::text::text[] as histogram_bounds
FROM pg_stats
WHERE tablename='l_p1'