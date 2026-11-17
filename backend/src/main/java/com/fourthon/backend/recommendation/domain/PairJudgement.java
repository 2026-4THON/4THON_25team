package com.fourthon.backend.recommendation.domain;

import java.util.List;

public record PairJudgement(
        boolean sufficient,
        String reason,
        List<Long> citationIds
) {

    public static PairJudgement insufficient() {
        return new PairJudgement(false, null, List.of());
    }
}
