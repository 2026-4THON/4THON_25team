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
        @DefaultValue("20000000") int maxDownloadBytes,
        @DefaultValue("1000") int imageMaxWidth,
        @DefaultValue("1600") int imageSliceHeight,
        @DefaultValue("10") int maxImageSlices,
        @DefaultValue("4000") long imageRequestIntervalMillis
) {
}
