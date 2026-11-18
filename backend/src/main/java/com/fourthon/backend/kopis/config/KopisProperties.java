package com.fourthon.backend.kopis.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;

@ConfigurationProperties(prefix = "kopis")
public record KopisProperties(
        String apiKey,
        @DefaultValue("http://www.kopis.or.kr/openApi/restful") String baseUrl,
        @DefaultValue("7000") long requestIntervalMillis,
        @DefaultValue({"30s", "60s", "120s"}) List<Duration> blockedRetryDelays,
        @DefaultValue Sync sync
) {

    public record Sync(
            @DefaultValue("false") boolean enabled,
            @DefaultValue("2025-01-01") LocalDate startDate,
            @DefaultValue("3") int futureMonths,
            @DefaultValue("1111") String districtCode,
            @DefaultValue("UNI") String boxOfficeArea
    ) {
    }
}
