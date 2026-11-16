package com.fourthon.backend.show.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public final class ShowQuery {

    private ShowQuery() {
    }

    public record VenueShow(
            Long performanceId,
            String title,
            String genre,
            String posterUrl,
            LocalDate startDate,
            LocalDate endDate,
            List<String> keywords
    ) {
    }

    public record VenueTicket(
            Long ticketId,
            Long performanceId,
            String title,
            LocalDateTime viewedAt,
            List<String> keywords
    ) {
    }
}
