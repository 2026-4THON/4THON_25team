package com.fourthon.backend.show.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "youtube")
public record YoutubeProperties(
        @DefaultValue("2") int maxVideos,
        @DefaultValue("3") int minScore
) {
}
