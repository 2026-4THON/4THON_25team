package com.fourthon.backend.show.dto;

import com.fourthon.backend.common.domain.TasteTag;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

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

    public record Candidate(
            Long performanceId,
            String title,
            String genre,
            String posterUrl,
            LocalDate startDate,
            LocalDate endDate,
            Map<TasteTag, Double> tagScores
    ) {
    }

    public record RankedShow(
            int rankNo,
            Long performanceId,
            String title,
            String genre,
            String posterUrl,
            LocalDate startDate,
            LocalDate endDate
    ) {
    }
}
