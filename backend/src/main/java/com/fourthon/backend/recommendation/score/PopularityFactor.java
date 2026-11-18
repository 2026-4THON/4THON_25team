package com.fourthon.backend.recommendation.score;

import com.fourthon.backend.common.domain.TagScores;
import com.fourthon.backend.show.dto.ShowQuery;

import org.springframework.stereotype.Component;

@Component
public class PopularityFactor implements ScoreFactor {

    @Override
    public FactorType type() {
        return FactorType.POPULARITY;
    }

    @Override
    public double score(RecommendationContext context, ShowQuery.Candidate candidate) {
        return TagScores.clamp(context.popularityScores().getOrDefault(candidate.performanceId(), 0.0));
    }
}
