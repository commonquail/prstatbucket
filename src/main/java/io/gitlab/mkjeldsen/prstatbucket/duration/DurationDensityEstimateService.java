package io.gitlab.mkjeldsen.prstatbucket.duration;

import java.util.List;

public interface DurationDensityEstimateService {
    List<StartEndRecord> dataFor(final DurationDensityReport report);
}
