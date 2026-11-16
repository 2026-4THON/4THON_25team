package com.fourthon.backend.taste.domain;

import java.util.Collection;

public final class RatingBaseline {

    private static final int MIN_TICKETS_FOR_MEAN = 3;
    private static final double DEFAULT_BASELINE = 3.0;
    private static final double ALL_EQUAL_HIGH_RATING = 4.0;
    private static final double DEVIATION_SCALE = 2.0;

    private final double baseline;
    private final boolean allEqual;

    private RatingBaseline(double baseline, boolean allEqual) {
        this.baseline = baseline;
        this.allEqual = allEqual;
    }

    public static RatingBaseline of(Collection<Double> ratings) {
        if (ratings.size() < MIN_TICKETS_FOR_MEAN) {
            return new RatingBaseline(DEFAULT_BASELINE, false);
        }
        double mean = ratings.stream().mapToDouble(Double::doubleValue).average().orElse(DEFAULT_BASELINE);
        boolean allEqual = ratings.stream().distinct().count() == 1;
        return new RatingBaseline(mean, allEqual);
    }

    public double deviation(double rating) {
        if (allEqual) {
            return rating >= ALL_EQUAL_HIGH_RATING ? (rating - DEFAULT_BASELINE) / DEVIATION_SCALE : 0.0;
        }
        return (rating - baseline) / DEVIATION_SCALE;
    }

    public boolean isHigh(double rating) {
        return deviation(rating) > 0.0;
    }
}
