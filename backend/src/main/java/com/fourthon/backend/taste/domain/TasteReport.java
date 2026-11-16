package com.fourthon.backend.taste.domain;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.MapKeyEnumerated;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Entity
@Table(
        name = "taste_reports",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_taste_reports_user_id",
                columnNames = "user_id"
        )
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TasteReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @ElementCollection
    @CollectionTable(name = "taste_report_tags", joinColumns = @JoinColumn(name = "report_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "tag", nullable = false, length = 30)
    private Set<TasteTag> selectedTags = new HashSet<>();

    @ElementCollection
    @CollectionTable(name = "taste_report_life_works", joinColumns = @JoinColumn(name = "report_id"))
    @OrderColumn(name = "sort_order")
    private List<LifeWork> lifeWorks = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "taste_report_choices", joinColumns = @JoinColumn(name = "report_id"))
    @OrderColumn(name = "sort_order")
    private List<VsChoice> vsChoices = new ArrayList<>();

    @Column(nullable = false, columnDefinition = "TEXT")
    private String freeText;

    @Column(nullable = false, length = 200)
    private String briefing;

    @ElementCollection
    @CollectionTable(name = "taste_report_ai_scores", joinColumns = @JoinColumn(name = "report_id"))
    @MapKeyEnumerated(EnumType.STRING)
    @MapKeyColumn(name = "tag", length = 30)
    @Column(name = "score", nullable = false)
    private Map<TasteTag, Double> aiScores = new HashMap<>();

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public TasteReport(Long userId, TasteAnswers answers, TasteAnalysis analysis, LocalDateTime now) {
        this.userId = userId;
        this.createdAt = now;
        apply(answers, analysis, now);
    }

    public void update(TasteAnswers answers, TasteAnalysis analysis, LocalDateTime now) {
        apply(answers, analysis, now);
    }

    public Set<TasteTag> selectedTagSet() {
        return selectedTags.isEmpty() ? EnumSet.noneOf(TasteTag.class) : EnumSet.copyOf(selectedTags);
    }

    public Map<TasteTag, Double> aiScoreMap() {
        return aiScores.isEmpty() ? new EnumMap<>(TasteTag.class) : new EnumMap<>(aiScores);
    }

    private void apply(TasteAnswers answers, TasteAnalysis analysis, LocalDateTime now) {
        selectedTags.clear();
        selectedTags.addAll(answers.selectedTags());
        lifeWorks.clear();
        lifeWorks.addAll(answers.lifeWorks());
        vsChoices.clear();
        vsChoices.addAll(answers.vsChoices());
        freeText = answers.freeText();
        briefing = analysis.briefing();
        aiScores.clear();
        aiScores.putAll(analysis.aiScores());
        updatedAt = now;
    }
}
