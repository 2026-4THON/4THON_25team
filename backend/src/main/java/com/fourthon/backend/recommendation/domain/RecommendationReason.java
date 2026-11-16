package com.fourthon.backend.recommendation.domain;

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
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Table(
        name = "recommendation_reasons",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_recommendation_reasons_user_performance",
                columnNames = {"user_id", "performance_id"}
        )
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RecommendationReason {

    private static final String DELIMITER = ",";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "performance_id", nullable = false)
    private Long performanceId;

    @Column(nullable = false, length = 50)
    private String reason;

    @Column(length = 200)
    private String citationIds;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public RecommendationReason(
            Long userId,
            Long performanceId,
            String reason,
            List<Long> citationIds,
            LocalDateTime createdAt) {

        this.userId = userId;
        this.performanceId = performanceId;
        this.reason = reason;
        this.citationIds = citationIds.stream().map(String::valueOf).collect(Collectors.joining(DELIMITER));
        this.createdAt = createdAt;
    }
}
