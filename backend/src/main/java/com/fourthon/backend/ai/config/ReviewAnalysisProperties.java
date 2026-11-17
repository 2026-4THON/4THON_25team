package com.fourthon.backend.ai.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "review-analysis")
public record ReviewAnalysisProperties(
        @DefaultValue("2") int threads,
        @DefaultValue("1000") long requestIntervalMillis,
        @DefaultValue("3") int maxFailCount,
        @DefaultValue("10") int missingLimit,
        @DefaultValue("50") int batchPageSize
) {
}
