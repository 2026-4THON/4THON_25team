package com.fourthon.backend.show.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "show-analysis")
public record ShowAnalysisProperties(
        @DefaultValue("400") int batchSize,
        @DefaultValue("4000") long requestIntervalMillis,
        @DefaultValue("3") int maxFailCount,
        @DefaultValue("20") int minSynopsisLength
) {
}
