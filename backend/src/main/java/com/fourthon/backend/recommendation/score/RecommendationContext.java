package com.fourthon.backend.recommendation.score;

import com.fourthon.backend.common.domain.TasteTag;

import java.util.Map;

public record RecommendationContext(
        Map<TasteTag, Double> tasteScores,
        Map<Long, Double> precedentScores,
        Map<Long, Double> adjustedRatings,
        Map<Long, Double> popularityScores,
        Map<Long, Double> wishScores
) {

    public static RecommendationContext forHome(
            Map<TasteTag, Double> tasteScores,
            Map<Long, Double> precedentScores,
            Map<Long, Double> adjustedRatings) {

        return new RecommendationContext(tasteScores, precedentScores, adjustedRatings, Map.of(), Map.of());
    }
}
