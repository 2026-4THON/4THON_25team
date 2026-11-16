package com.fourthon.backend.kopis.config;

import com.fourthon.backend.kopis.client.RequestThrottle;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Configuration
@EnableScheduling
@EnableConfigurationProperties(KopisProperties.class)
public class KopisConfig {

    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(5);
    private static final Duration READ_TIMEOUT = Duration.ofSeconds(30);

    @Bean
    public RestClient kopisRestClient(KopisProperties kopisProperties) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(CONNECT_TIMEOUT);
        requestFactory.setReadTimeout(READ_TIMEOUT);
        return RestClient.builder()
                .baseUrl(kopisProperties.baseUrl())
                .requestFactory(requestFactory)
                .build();
    }

    @Bean
    public RequestThrottle kopisThrottle(KopisProperties kopisProperties) {
        return new RequestThrottle(Duration.ofMillis(kopisProperties.requestIntervalMillis()));
    }
}
