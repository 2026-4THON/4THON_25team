package com.fourthon.backend.recommendation.score;

import com.fourthon.backend.show.dto.ShowQuery;

import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class ScoreCalculator {

    private final Map<FactorType, ScoreFactor> factors = new EnumMap<>(FactorType.class);

    public ScoreCalculator(List<ScoreFactor> scoreFactors) {
        scoreFactors.forEach(factor -> factors.put(factor.type(), factor));
    }

    public ScoredShow score(
            RecommendationContext context,
            ShowQuery.Candidate candidate,
            Map<FactorType, Double> weights) {

        Map<FactorType, Double> factorScores = new EnumMap<>(FactorType.class);
        double total = 0.0;
        for (Map.Entry<FactorType, Double> weight : weights.entrySet()) {
            ScoreFactor factor = factors.get(weight.getKey());
            if (factor == null || weight.getValue() == 0.0) {
                continue;
            }
            double score = factor.score(context, candidate);
            factorScores.put(weight.getKey(), score);
            total += weight.getValue() * score;
        }
        return new ScoredShow(candidate, total, factorScores);
    }
}
