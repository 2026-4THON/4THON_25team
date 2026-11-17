package com.fourthon.backend.recommendation.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Table(
        name = "pair_recommendations",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_pair_recommendations_user_performance",
                columnNames = {"user_id", "performance_id"}
        )
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PairRecommendation {

    private static final String DELIMITER = ",";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "performance_id", nullable = false)
    private Long performanceId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PairStatus status;

    @Column(nullable = false, length = 200)
    private String actorIds;

    @Column(length = 500)
    private String citationIds;

    @Column(length = 200)
    private String reason;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public PairRecommendation(
            Long userId,
            Long performanceId,
            PairStatus status,
            List<Long> actorIds,
            List<Long> citationIds,
            String reason,
            LocalDateTime createdAt) {

        this.userId = userId;
        this.performanceId = performanceId;
        this.status = status;
        this.actorIds = join(actorIds);
        this.citationIds = join(citationIds);
        this.reason = reason;
        this.createdAt = createdAt;
    }

    public List<Long> actorIdList() {
        if (actorIds == null || actorIds.isBlank()) {
            return List.of();
        }
        return Arrays.stream(actorIds.split(DELIMITER)).map(Long::valueOf).toList();
    }

    private static String join(List<Long> ids) {
        return ids.stream().map(String::valueOf).collect(Collectors.joining(DELIMITER));
    }
}
