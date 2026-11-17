package com.fourthon.backend.ai.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "review_analysis_failures",
        indexes = @Index(name = "idx_review_analysis_failures_ticket_id", columnList = "ticket_id")
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReviewAnalysisFailure {

    private static final int MAX_ERROR_LENGTH = 500;

    @Id
    @Column(name = "review_id")
    private Long reviewId;

    @Column(name = "ticket_id", nullable = false)
    private Long ticketId;

    @Column(nullable = false)
    private int failCount;

    @Column(length = MAX_ERROR_LENGTH)
    private String lastError;

    @Column(nullable = false)
    private LocalDateTime failedAt;

    public ReviewAnalysisFailure(Long reviewId, Long ticketId) {
        this.reviewId = reviewId;
        this.ticketId = ticketId;
    }

    public void record(String error, LocalDateTime failedAt) {
        this.failCount++;
        this.lastError = error == null || error.length() <= MAX_ERROR_LENGTH
                ? error
                : error.substring(0, MAX_ERROR_LENGTH);
        this.failedAt = failedAt;
    }
}
