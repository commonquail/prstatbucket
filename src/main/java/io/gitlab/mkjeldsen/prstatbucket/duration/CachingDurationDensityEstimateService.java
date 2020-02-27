package io.gitlab.mkjeldsen.prstatbucket.duration;

import com.github.benmanes.caffeine.cache.LoadingCache;
import java.util.List;

public final class CachingDurationDensityEstimateService
        implements DurationDensityEstimateService {

    private final LoadingCache<DurationDensityReport, List<StartEndRecord>>
            cache;

    public CachingDurationDensityEstimateService(
            final LoadingCache<DurationDensityReport, List<StartEndRecord>>
                    cache) {
        this.cache = cache;
    }

    @Override
    public List<StartEndRecord> dataFor(final DurationDensityReport report) {
        return cache.get(report);
    }
}
