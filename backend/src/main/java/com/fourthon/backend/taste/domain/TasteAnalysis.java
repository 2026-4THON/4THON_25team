package com.fourthon.backend.taste.domain;

import java.util.Map;

public record TasteAnalysis(
        String briefing,
        Map<TasteTag, Double> aiScores
) {
}
