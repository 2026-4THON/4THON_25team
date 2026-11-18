package com.fourthon.backend.show.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public final class ShowSync {

    private ShowSync() {
    }

    public record Upsert(
            Long performanceId,
            String kopisId,
            Long venueId,
            String kopisGenre,
            LocalDate startDate,
            LocalDate endDate,
            boolean openRun,
            String synopsis,
            String ageText,
            int minAge,
            String runtimeText,
            String timeGuide,
            String priceText,
            Integer minPrice,
            List<SeatPrice> prices,
            List<BookingLink> bookingLinks,
            List<String> introImageUrls,
            LocalDateTime kopisUpdatedAt
    ) {
    }

    public record SeatPrice(
            String grade,
            int price
    ) {
    }

    public record BookingLink(
            String name,
            String url
    ) {
    }

    public record Info(
            Long performanceId,
            LocalDateTime kopisUpdatedAt,
            String timeGuide
    ) {
    }

    public record SessionPlan(
            Long performanceId,
            LocalDate startDate,
            LocalDate endDate,
            String timeGuide
    ) {
    }

    public record Rank(
            Long performanceId,
            int rank
    ) {
    }
}
