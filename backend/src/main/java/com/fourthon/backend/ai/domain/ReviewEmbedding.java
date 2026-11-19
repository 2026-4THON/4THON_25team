package com.fourthon.backend.ai.domain;

import com.fourthon.backend.ticket.domain.TicketReview;

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

import java.time.LocalDateTime;

@Entity
@Table(
        name = "review_embeddings",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_review_embeddings_review_id",
                columnNames = "review_id"
        ),
        indexes = {
                @Index(name = "idx_review_embeddings_ticket_id", columnList = "ticket_id"),
                @Index(name = "idx_review_embeddings_performance_id", columnList = "performance_id")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReviewEmbedding {

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
    private TicketReview.ReviewType reviewType;

    @Column(nullable = false, length = 50)
    private String model;

    @Column(nullable = false)
    private int dimension;

    @Column(name = "`vector`", nullable = false, columnDefinition = "BLOB")
    private byte[] vector;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public ReviewEmbedding(ReviewSource source, String model, float[] values, LocalDateTime createdAt) {
        this.reviewId = source.reviewId();
        this.ticketId = source.ticketId();
        this.performanceId = source.performanceId();
        this.reviewType = source.type();
        this.model = model;
        this.dimension = values.length;
        this.vector = VectorCodec.toBytes(values);
        this.createdAt = createdAt;
    }

    public float[] values() {
        return VectorCodec.fromBytes(vector);
    }
}
