package com.fourthon.backend.taste.domain;

import com.fourthon.backend.common.domain.TasteTag;

import java.util.Map;

public record TasteAnalysis(
        String briefing,
        Map<TasteTag, Double> aiScores
) {
}
