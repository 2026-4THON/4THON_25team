package com.fourthon.backend.taste.domain;

public record TicketRating(
        Long userId,
        Long performanceId,
        double rating
) {

    private static final double HALF_UNITS_PER_STAR = 2.0;

    public static TicketRating ofHalfUnits(Long userId, Long performanceId, int ratingHalfUnits) {
        return new TicketRating(userId, performanceId, ratingHalfUnits / HALF_UNITS_PER_STAR);
    }
}
