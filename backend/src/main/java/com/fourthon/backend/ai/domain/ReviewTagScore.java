package com.fourthon.backend.ai.domain;

import com.fourthon.backend.common.domain.TasteTag;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "review_tag_scores",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_review_tag_scores_review_tag",
                columnNames = {"review_id", "tag"}
        ),
        indexes = {
                @Index(name = "idx_review_tag_scores_ticket_id", columnList = "ticket_id"),
                @Index(name = "idx_review_tag_scores_performance_id", columnList = "performance_id")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReviewTagScore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "review_id", nullable = false)
    private Long reviewId;

    @Column(name = "ticket_id", nullable = false)
    private Long ticketId;

    @Column(name = "performance_id", nullable = false)
    private Long performanceId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TasteTag tag;

    @Column(nullable = false)
    private double score;

    public ReviewTagScore(ReviewSource source, TasteTag tag, double score) {
        this.reviewId = source.reviewId();
        this.ticketId = source.ticketId();
        this.performanceId = source.performanceId();
        this.tag = tag;
        this.score = score;
    }
}
