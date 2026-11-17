package com.fourthon.backend.show.service;

import com.fourthon.backend.ai.event.ReviewTagScoresChangedEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReviewTagScoreListener {

    private final ShowTagScoreService showTagScoreService;

    @EventListener
    public void onReviewTagScoresChanged(ReviewTagScoresChangedEvent event) {
        try {
            showTagScoreService.refreshReviewScores(event.performanceIds());
        } catch (RuntimeException e) {
            log.error("공연 태그 점수 갱신 실패: {}", event.performanceIds(), e);
        }
    }
}
