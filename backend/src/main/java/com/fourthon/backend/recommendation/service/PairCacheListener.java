package com.fourthon.backend.recommendation.service;

import com.fourthon.backend.internal.event.TicketChangedEvent;
import com.fourthon.backend.taste.event.TasteReportSavedEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class PairCacheListener {

    private final PairRecommendationService pairRecommendationService;

    @TransactionalEventListener(fallbackExecution = true)
    public void onTasteReportSaved(TasteReportSavedEvent event) {
        clear(event.userId());
    }

    @TransactionalEventListener(fallbackExecution = true)
    public void onTicketChanged(TicketChangedEvent event) {
        clear(event.userId());
    }

    private void clear(Long userId) {
        try {
            pairRecommendationService.clear(userId);
        } catch (RuntimeException e) {
            log.error("페어 추천 저장값 삭제 실패: userId={}", userId, e);
        }
    }
}
