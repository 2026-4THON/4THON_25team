package com.fourthon.backend.map.dto;

import com.fourthon.backend.map.domain.Venue;
import com.fourthon.backend.show.dto.ShowQuery;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public final class MapResponse {

    private MapResponse() {
    }

    @Schema(description = "지도 핀에 표시할 공연장")
    public record VenueItem(
            Long venueId,
            String name,
            String address,
            Double latitude,
            Double longitude,
            @Schema(description = "NOW: 진행중 작품 수, MINE: 내 관람 횟수") long count
    ) {

        public static VenueItem from(Venue venue, long count) {
            return new VenueItem(
                    venue.getId(),
                    venue.getName(),
                    venue.getAddress(),
                    venue.getLatitude(),
                    venue.getLongitude(),
                    count
            );
        }
    }

    @Schema(description = "공연장의 진행중 작품")
    public record VenueShow(
            Long performanceId,
            String title,
            @Schema(description = "MUSICAL, PLAY, ETC") String genre,
            String posterUrl,
            LocalDate startDate,
            LocalDate endDate,
            @Schema(description = "AI 키워드. 아직 없으면 빈 목록") List<String> keywords
    ) {

        public static VenueShow from(ShowQuery.VenueShow show) {
            return new VenueShow(
                    show.performanceId(),
                    show.title(),
                    show.genre(),
                    show.posterUrl(),
                    show.startDate(),
                    show.endDate(),
                    List.of()
            );
        }
    }

    @Schema(description = "공연장의 내 관람 기록")
    public record MyTicket(
            Long ticketId,
            Long performanceId,
            String title,
            LocalDateTime viewedAt,
            @Schema(description = "AI 키워드. 아직 없으면 빈 목록") List<String> keywords
    ) {

        public static MyTicket from(ShowQuery.VenueTicket ticket) {
            return new MyTicket(
                    ticket.ticketId(),
                    ticket.performanceId(),
                    ticket.title(),
                    ticket.viewedAt(),
                    List.of()
            );
        }
    }
}
