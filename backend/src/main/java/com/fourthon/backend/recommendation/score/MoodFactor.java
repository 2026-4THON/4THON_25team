package com.fourthon.backend.recommendation.score;

import com.fourthon.backend.common.domain.TagType;
import com.fourthon.backend.show.dto.ShowQuery;

import org.springframework.stereotype.Component;

@Component
public class MoodFactor implements ScoreFactor {

    @Override
    public FactorType type() {
        return FactorType.MOOD;
    }

    @Override
    public double score(RecommendationContext context, ShowQuery.Candidate candidate) {
        return TasteMatch.match(context.tasteScores(), candidate.tagScores(), TagType.MOOD);
    }
}
