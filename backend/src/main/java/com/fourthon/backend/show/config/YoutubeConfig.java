package com.fourthon.backend.show.config;

import com.fourthon.backend.common.client.RequestThrottle;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class YoutubeConfig {

    @Bean
    public RequestThrottle youtubeThrottle(YoutubeProperties youtubeProperties) {
        return new RequestThrottle(Duration.ofMillis(youtubeProperties.requestIntervalMillis()));
    }
}
