package com.fourthon.backend.show.domain;

import com.fourthon.backend.common.domain.TasteTag;

import java.util.List;
import java.util.Map;

public record ShowAnalysis(
        List<String> keywords,
        Map<TasteTag, Double> tagScores
) {
}
