package com.fourthon.backend.taste.service;

import com.fourthon.backend.internal.event.TicketChangedEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class TasteRatingListener {

    private final TasteService tasteService;

    @TransactionalEventListener(fallbackExecution = true)
    public void onTicketChanged(TicketChangedEvent event) {
        try {
            tasteService.recalculateScores(event.userId());
        } catch (RuntimeException e) {
            log.error("취향 점수 재계산 실패: userId={}", event.userId(), e);
        }
    }
}
