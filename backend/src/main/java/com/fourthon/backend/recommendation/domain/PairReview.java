package com.fourthon.backend.recommendation.domain;

import java.util.List;
import java.util.stream.Collectors;

public record PairReview(
        Long reviewId,
        List<Long> actorIds,
        List<String> actorNames,
        String content
) {

    public String comboKey() {
        return actorIds.stream().sorted().map(String::valueOf).collect(Collectors.joining("-"));
    }
}
