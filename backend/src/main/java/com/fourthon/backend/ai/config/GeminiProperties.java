package com.fourthon.backend.ai.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "gemini")
public record GeminiProperties(
        String apiKey,
        @DefaultValue("https://generativelanguage.googleapis.com/v1beta") String baseUrl,
        @DefaultValue("gemini-3.6-flash") String model,
        @DefaultValue("0.3") double temperature,
        @DefaultValue("180") int readTimeoutSeconds,
        @DefaultValue("gemini-embedding-2") String embeddingModel,
        @DefaultValue("768") int embeddingDimension
) {
}
