package com.fourthon.backend.ai.service;

import com.fourthon.backend.internal.event.TicketChangedEvent;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class ReviewChangeListener {

    private final ReviewAnalysisService reviewAnalysisService;

    @TransactionalEventListener(fallbackExecution = true)
    public void onTicketChanged(TicketChangedEvent event) {
        reviewAnalysisService.handleInBackground(event);
    }
}
