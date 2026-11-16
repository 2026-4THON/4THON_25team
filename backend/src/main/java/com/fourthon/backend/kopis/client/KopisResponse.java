package com.fourthon.backend.kopis.client;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public final class KopisResponse {

    private KopisResponse() {
    }

    public record Performance(
            String kopisId,
            String title
    ) {
    }

    public record PerformanceDetail(
            String kopisId,
            String kopisVenueId,
            String title,
            LocalDate startDate,
            LocalDate endDate,
            String venueName,
            String cast,
            String runtime,
            String age,
            String price,
            String posterUrl,
            String synopsis,
            String genre,
            boolean openRun,
            boolean daehakro,
            LocalDateTime updatedAt,
            String timeGuide,
            List<String> introImageUrls,
            List<BookingLink> bookingLinks
    ) {
    }

    public record BookingLink(
            String name,
            String url
    ) {
    }

    public record Facility(
            String kopisVenueId,
            String name,
            String address,
            Double latitude,
            Double longitude
    ) {
    }

    public record BoxOffice(
            int rank,
            String kopisId
    ) {
    }
}
