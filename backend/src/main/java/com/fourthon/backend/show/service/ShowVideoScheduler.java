package com.fourthon.backend.show.service;

import com.fourthon.backend.show.config.YoutubeProperties;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ShowVideoScheduler {

    private final ShowVideoService showVideoService;
    private final YoutubeProperties youtubeProperties;

    @Scheduled(cron = "${youtube.cron:0 23 17 * * *}", zone = "Asia/Seoul")
    public void runDaily() {
        if (youtubeProperties.enabled()) {
            showVideoService.run();
        }
    }
}
