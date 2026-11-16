package com.fourthon.backend.ai.domain;

import com.fourthon.backend.ticket.domain.TicketReview;

import java.util.List;

public record ReviewSource(
        Long reviewId,
        Long ticketId,
        Long performanceId,
        String performanceTitle,
        TicketReview.ReviewType type,
        List<String> actorNames,
        double rating,
        String content
) {

    public boolean isPerformanceReview() {
        return type == TicketReview.ReviewType.PERFORMANCE;
    }
}
