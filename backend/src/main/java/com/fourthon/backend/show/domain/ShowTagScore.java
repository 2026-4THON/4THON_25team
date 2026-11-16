package com.fourthon.backend.show.domain;

import com.fourthon.backend.common.domain.TasteTag;

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

@Entity
@Table(
        name = "show_tag_scores",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_show_tag_scores_performance_tag",
                columnNames = {"performance_id", "tag"}
        )
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ShowTagScore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "performance_id", nullable = false)
    private Long performanceId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TasteTag tag;

    @Column(nullable = false)
    private double synopsisScore;

    @Column(nullable = false)
    private double score;

    public ShowTagScore(Long performanceId, TasteTag tag, double synopsisScore) {
        this.performanceId = performanceId;
        this.tag = tag;
        this.synopsisScore = synopsisScore;
        this.score = synopsisScore;
    }

    public void applyReviewScore(double reviewScore, double reviewWeight) {
        this.score = (1.0 - reviewWeight) * synopsisScore + reviewWeight * reviewScore;
    }
}
