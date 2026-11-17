package com.fourthon.backend.recommendation.score;

import com.fourthon.backend.common.domain.TagScores;
import com.fourthon.backend.show.dto.ShowQuery;

import org.springframework.stereotype.Component;

@Component
public class PrecedentFactor implements ScoreFactor {

    @Override
    public FactorType type() {
        return FactorType.PRECEDENT;
    }

    @Override
    public double score(RecommendationContext context, ShowQuery.Candidate candidate) {
        return TagScores.clamp(context.precedentScores().getOrDefault(candidate.performanceId(), 0.0));
    }
}
