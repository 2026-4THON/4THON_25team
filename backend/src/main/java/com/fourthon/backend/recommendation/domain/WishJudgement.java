package com.fourthon.backend.recommendation.domain;

import java.util.Map;
import java.util.Optional;

public record WishJudgement(
        Map<Long, Double> scores,
        Optional<String> summary
) {

    public static WishJudgement empty() {
        return new WishJudgement(Map.of(), Optional.empty());
    }

    public boolean hasScores() {
        return !scores.isEmpty();
    }
}
