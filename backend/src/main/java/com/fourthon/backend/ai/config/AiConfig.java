package com.fourthon.backend.ai.config;

import com.fourthon.backend.common.client.RequestThrottle;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
@EnableConfigurationProperties({GeminiProperties.class, ReviewAnalysisProperties.class})
public class AiConfig {

    @Bean
    public RequestThrottle reviewAnalysisThrottle(ReviewAnalysisProperties reviewAnalysisProperties) {
        return new RequestThrottle(Duration.ofMillis(reviewAnalysisProperties.requestIntervalMillis()));
    }
}
