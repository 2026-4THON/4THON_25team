package com.fourthon.backend.show.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public final class ShowQuery {

    private ShowQuery() {
    }

    public record VenueShow(
            Long performanceId,
            String title,
            String genre,
            String posterUrl,
            LocalDate startDate,
            LocalDate endDate
    ) {
    }

    public record VenueTicket(
            Long ticketId,
            Long performanceId,
            String title,
            LocalDateTime viewedAt
    ) {
    }
}
