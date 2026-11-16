package com.fourthon.backend.recommendation.service;

import com.fourthon.backend.show.event.ShowAnalysisCompletedEvent;
import com.fourthon.backend.taste.event.TasteReportSavedEvent;

import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class ReasonTriggerListener {

    private final RecommendationReasonService recommendationReasonService;

    @TransactionalEventListener(fallbackExecution = true)
    public void onTasteReportSaved(TasteReportSavedEvent event) {
        recommendationReasonService.regenerateInBackground(event.userId(), event.updated());
    }

    @EventListener
    public void onShowAnalysisCompleted(ShowAnalysisCompletedEvent event) {
        recommendationReasonService.runBatchInBackground();
    }
}
