package com.fourthon.backend.recommendation.score;

import com.fourthon.backend.common.domain.TagType;
import com.fourthon.backend.show.dto.ShowQuery;

import org.springframework.stereotype.Component;

@Component
public class GenreFactor implements ScoreFactor {

    @Override
    public FactorType type() {
        return FactorType.GENRE;
    }

    @Override
    public double score(RecommendationContext context, ShowQuery.Candidate candidate) {
        if (context.usesSelection()) {
            return TasteMatch.selectionMatch(context.selectedTags(), candidate.tagScores(), TagType.GENRE);
        }
        return TasteMatch.match(context.tasteScores(), candidate.tagScores(), TagType.GENRE);
    }
}
