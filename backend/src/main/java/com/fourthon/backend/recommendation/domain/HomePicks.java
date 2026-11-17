package com.fourthon.backend.recommendation.domain;

import com.fourthon.backend.common.domain.TasteTag;
import com.fourthon.backend.recommendation.score.FactorType;
import com.fourthon.backend.recommendation.score.ScoredShow;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public record HomePicks(
        boolean hasReport,
        String briefing,
        Map<TasteTag, Double> tasteScores,
        Map<FactorType, Double> weights,
        Optional<String> lifeWorkTitle,
        List<ScoredShow> shows
) {

    public static HomePicks noReport() {
        return new HomePicks(false, null, Map.of(), Map.of(), Optional.empty(), List.of());
    }
}
