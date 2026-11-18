package com.fourthon.backend.show.config;

import com.fourthon.backend.common.client.RequestThrottle;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
@EnableConfigurationProperties({RatingProperties.class, ShowAnalysisProperties.class})
public class ShowConfig {

    @Bean
    public RequestThrottle showAnalysisThrottle(ShowAnalysisProperties showAnalysisProperties) {
        return new RequestThrottle(Duration.ofMillis(showAnalysisProperties.requestIntervalMillis()));
    }
}
