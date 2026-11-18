package com.fourthon.backend.recommendation.domain;

import com.fourthon.backend.ai.domain.VectorCodec;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "user_taste_embeddings",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_user_taste_embeddings_user_performance",
                columnNames = {"user_id", "performance_id"}
        )
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserTasteEmbedding {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "performance_id", nullable = false)
    private Long performanceId;

    @Column(nullable = false, length = 50)
    private String model;

    @Column(nullable = false, columnDefinition = "BLOB")
    private byte[] vector;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public UserTasteEmbedding(Long userId, Long performanceId, String model, float[] values, LocalDateTime createdAt) {
        this.userId = userId;
        this.performanceId = performanceId;
        this.model = model;
        this.vector = VectorCodec.toBytes(values);
        this.createdAt = createdAt;
    }

    public float[] values() {
        return VectorCodec.fromBytes(vector);
    }
}
