package com.fourthon.backend.recommendation.score;

import com.fourthon.backend.show.dto.ShowQuery;

import java.util.Map;

public record ScoredShow(
        ShowQuery.Candidate candidate,
        double total,
        Map<FactorType, Double> factorScores
) {

    public double factor(FactorType type) {
        return factorScores.getOrDefault(type, 0.0);
    }
}
