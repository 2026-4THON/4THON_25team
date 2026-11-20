package com.fourthon.backend.show.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "youtube")
public record YoutubeProperties(
        String apiKey,
        @DefaultValue("https://www.googleapis.com/youtube/v3") String baseUrl,
        @DefaultValue("10") int candidateCount,
        @DefaultValue("2") int maxVideos,
        @DefaultValue("3") int minScore,
        @DefaultValue("90") int dailyLimit,
        @DefaultValue("1000") long requestIntervalMillis,
        @DefaultValue("true") boolean enabled
) {
}
