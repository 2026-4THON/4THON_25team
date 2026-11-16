package com.fourthon.backend.show.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(RatingProperties.class)
public class ShowConfig {
}
