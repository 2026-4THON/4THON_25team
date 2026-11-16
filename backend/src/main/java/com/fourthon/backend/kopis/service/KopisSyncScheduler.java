package com.fourthon.backend.kopis.service;

import com.fourthon.backend.kopis.config.KopisProperties;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KopisSyncScheduler {

    private final KopisSyncService kopisSyncService;
    private final KopisProperties kopisProperties;

    @Scheduled(cron = "${kopis.sync.cron:0 37 4 * * *}", zone = "Asia/Seoul")
    public void runDaily() {
        if (kopisProperties.sync().enabled()) {
            kopisSyncService.runDaily();
        }
    }
}
