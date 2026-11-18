package com.fourthon.backend.common.domain;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;

public final class TagScores {

    private TagScores() {
    }

    public static Map<TasteTag, Double> empty() {
        Map<TasteTag, Double> scores = new EnumMap<>(TasteTag.class);
        Arrays.stream(TasteTag.values()).forEach(tag -> scores.put(tag, 0.0));
        return scores;
    }

    public static Map<TasteTag, Double> fromCodes(Map<String, Double> rawScores) {
        Map<TasteTag, Double> scores = empty();
        if (rawScores == null) {
            return scores;
        }
        rawScores.forEach((code, value) -> TasteTag.fromCode(code)
                .filter(tag -> value != null)
                .ifPresent(tag -> scores.put(tag, clamp(value))));
        return scores;
    }

    public static double clamp(double value) {
        return Math.max(0.0, Math.min(1.0, value));
    }
}
