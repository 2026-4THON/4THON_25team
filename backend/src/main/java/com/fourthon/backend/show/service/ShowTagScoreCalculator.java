package com.fourthon.backend.show.service;

import org.springframework.stereotype.Component;

@Component
public class ShowTagScoreCalculator {

    private static final double REVIEW_PRIOR_COUNT = 5.0;
    private static final double MAX_REVIEW_WEIGHT = 0.5;

    public double reviewWeight(long reviewCount) {
        if (reviewCount <= 0) {
            return 0.0;
        }
        return Math.min(reviewCount / (reviewCount + REVIEW_PRIOR_COUNT), MAX_REVIEW_WEIGHT);
    }
}
