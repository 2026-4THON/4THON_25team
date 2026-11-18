package com.fourthon.backend.taste.service;

import com.fourthon.backend.common.domain.TasteTag;

import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

@Component
public class TasteScoreCalculator {

    private static final double SELECTION_WEIGHT = 0.5;
    private static final double AI_WEIGHT = 0.3;
    private static final double RATING_WEIGHT = 0.2;
    private static final double SELECTION_WEIGHT_WITHOUT_RATING = 0.6;
    private static final double AI_WEIGHT_WITHOUT_RATING = 0.4;
    private static final double SELECTED = 1.0;
    private static final double NOT_SELECTED = 0.0;

    public Map<TasteTag, Double> calculate(
            Set<TasteTag> selectedTags,
            Map<TasteTag, Double> aiScores,
            Map<TasteTag, Double> ratingScores) {

        Weights weights = weights(!aiScores.isEmpty(), !ratingScores.isEmpty());
        Map<TasteTag, Double> scores = new EnumMap<>(TasteTag.class);
        for (TasteTag tag : TasteTag.values()) {
            double selection = selectedTags.contains(tag) ? SELECTED : NOT_SELECTED;
            double score = weights.selection() * selection
                    + weights.ai() * aiScores.getOrDefault(tag, 0.0)
                    + weights.rating() * ratingScores.getOrDefault(tag, 0.0);
            scores.put(tag, score);
        }
        return scores;
    }

    private Weights weights(boolean hasAi, boolean hasRating) {
        if (hasAi && hasRating) {
            return new Weights(SELECTION_WEIGHT, AI_WEIGHT, RATING_WEIGHT);
        }
        if (hasAi) {
            return new Weights(SELECTION_WEIGHT_WITHOUT_RATING, AI_WEIGHT_WITHOUT_RATING, 0.0);
        }
        if (hasRating) {
            double total = SELECTION_WEIGHT + RATING_WEIGHT;
            return new Weights(SELECTION_WEIGHT / total, 0.0, RATING_WEIGHT / total);
        }
        return new Weights(1.0, 0.0, 0.0);
    }

    private record Weights(
            double selection,
            double ai,
            double rating
    ) {
    }
}
