package com.fourthon.backend.recommendation.score;

import com.fourthon.backend.common.domain.TagScores;
import com.fourthon.backend.show.dto.ShowQuery;

import org.springframework.stereotype.Component;

@Component
public class RatingFactor implements ScoreFactor {

    private static final double MAX_RATING = 5.0;

    @Override
    public FactorType type() {
        return FactorType.RATING;
    }

    @Override
    public double score(RecommendationContext context, ShowQuery.Candidate candidate) {
        return TagScores.clamp(context.adjustedRatings().getOrDefault(candidate.performanceId(), 0.0) / MAX_RATING);
    }
}
