package com.fourthon.backend.recommendation.config;

import com.fourthon.backend.common.client.RequestThrottle;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
@EnableConfigurationProperties(RecommendationProperties.class)
public class RecommendationConfig {

    @Bean
    public RequestThrottle reasonThrottle(RecommendationProperties recommendationProperties) {
        return new RequestThrottle(Duration.ofMillis(recommendationProperties.reason().requestIntervalMillis()));
    }
}
