package com.fourthon.backend.ai.service;

import com.fourthon.backend.ai.domain.ReviewSource;
import com.fourthon.backend.ticket.domain.TicketReview;

import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ReviewTextBuilder {

    private static final Map<TicketReview.ReviewType, String> TYPE_LABELS = Map.of(
            TicketReview.ReviewType.PERFORMANCE, "공연 후기",
            TicketReview.ReviewType.ACTOR, "배우 후기",
            TicketReview.ReviewType.PAIR, "페어 후기"
    );

    public String build(ReviewSource source) {
        StringBuilder text = new StringBuilder()
                .append("공연: ").append(source.performanceTitle()).append('\n')
                .append("후기 종류: ").append(TYPE_LABELS.get(source.type())).append('\n');
        if (!source.actorNames().isEmpty()) {
            text.append("배우: ").append(String.join(", ", source.actorNames())).append('\n');
        }
        return text.append("별점: ").append(source.rating()).append('\n')
                .append("후기: ").append(source.content().strip())
                .toString();
    }
}
