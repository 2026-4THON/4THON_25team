package com.fourthon.backend.show.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "show-analysis")
public record ShowAnalysisProperties(
        @DefaultValue("400") int batchSize,
        @DefaultValue("4000") long requestIntervalMillis,
        @DefaultValue("3") int maxFailCount,
        @DefaultValue("20") int minSynopsisLength,
        @DefaultValue("200") int extractionBatchSize,
        @DefaultValue("4") int maxIntroImages,
        @DefaultValue("3000000") int maxImageBytes,
        @DefaultValue("4000") long imageRequestIntervalMillis
) {
}
