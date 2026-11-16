package com.fourthon.backend.recommendation.score;

import com.fourthon.backend.common.domain.TasteTag;

import java.util.Map;
import java.util.Set;

public record RecommendationContext(
        Map<TasteTag, Double> tasteScores,
        Set<TasteTag> selectedTags,
        Map<Long, Double> precedentScores,
        Map<Long, Double> adjustedRatings,
        Map<Long, Double> popularityScores,
        Map<Long, Double> wishScores
) {

    public static RecommendationContext forHome(
            Map<TasteTag, Double> tasteScores,
            Map<Long, Double> precedentScores,
            Map<Long, Double> adjustedRatings) {

        return new RecommendationContext(tasteScores, Set.of(), precedentScores, adjustedRatings, Map.of(), Map.of());
    }

    public static RecommendationContext forExplore(
            Map<TasteTag, Double> tasteScores,
            Set<TasteTag> selectedTags,
            Map<Long, Double> adjustedRatings,
            Map<Long, Double> popularityScores,
            Map<Long, Double> wishScores) {

        return new RecommendationContext(tasteScores, selectedTags, Map.of(), adjustedRatings, popularityScores, wishScores);
    }

    public boolean usesSelection() {
        return !selectedTags.isEmpty();
    }
}
