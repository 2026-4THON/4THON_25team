package com.fourthon.backend.ai.dto;

import com.fourthon.backend.common.domain.TasteTag;

import java.time.LocalDateTime;
import java.util.Map;

public final class ReviewAnalysisResult {

    private ReviewAnalysisResult() {
    }

    public record TagSummary(
            long reviewCount,
            Map<TasteTag, Double> averages
    ) {
    }

    public record Failure(
            Long reviewId,
            Long ticketId,
            int failCount,
            String lastError,
            LocalDateTime failedAt
    ) {
    }

    public record Batch(
            int analyzed,
            int failed,
            boolean stopped
    ) {
    }
}
