package com.fourthon.backend.recommendation.score;

import com.fourthon.backend.show.dto.ShowQuery;

public interface ScoreFactor {

    FactorType type();

    double score(RecommendationContext context, ShowQuery.Candidate candidate);
}
