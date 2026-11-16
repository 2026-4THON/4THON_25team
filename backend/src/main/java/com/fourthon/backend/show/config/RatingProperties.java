package com.fourthon.backend.show.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "rating")
public record RatingProperties(
        @DefaultValue("3") int minPriorCount,
        @DefaultValue("3.5") double defaultMean
) {
}
