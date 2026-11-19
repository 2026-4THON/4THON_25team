package com.fourthon.backend.show.dto;

import com.fourthon.backend.show.domain.ShowDetail;
import com.fourthon.backend.show.domain.ShowStatus;
import com.fourthon.backend.ticket.domain.CatalogPerformance;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.List;

public final class ShowResponse {

    private ShowResponse() {
    }

    @Schema(description = "BE-SHOW-02 작품 상세")
    public record Detail(
            Long performanceId,
            String title,
            String posterUrl,
            @Schema(description = "MUSICAL, PLAY, ETC") String genre,
            @Schema(description = "KOPIS 장르명") String kopisGenre,
            @Schema(description = "UPCOMING, ONGOING, ENDED") String status,
            Rating rating,
            @Schema(description = "AI 키워드. 아직 없으면 빈 목록") List<String> keywords,
            String synopsis,
            @Schema(description = "OFFICIAL: KOPIS 등 공식 시놉시스, EXTRACTED: 소개 이미지에서 추출한 시놉시스, NONE: 시놉시스 없음") String synopsisSource,
            LocalDate startDate,
            LocalDate endDate,
            boolean openRun,
            String ageText,
            Venue venue,
            String runtimeText,
            int runningMinutes,
            List<SeatPrice> prices,
            Integer minPrice,
            List<BookingLink> bookingLinks,
            List<Actor> cast,
            List<String> introImageUrls,
            @Schema(description = "유튜브 영상 ID. 아직 없으면 빈 목록") List<String> videoIds
    ) {

        public static Detail from(
                CatalogPerformance performance,
                ShowDetail detail,
                List<Actor> cast,
                Rating rating,
                LocalDate today) {

            if (detail == null) {
                return new Detail(
                        performance.getId(),
                        performance.getTitle(),
                        performance.getPosterUrl(),
                        performance.getGenre().name(),
                        null,
                        null,
                        rating,
                        List.of(),
                        null,
                        ShowDetail.SynopsisSource.NONE.name(),
                        null,
                        null,
                        false,
                        null,
                        new Venue(null, performance.getVenue()),
                        null,
                        performance.getRunningMinutes(),
                        List.of(),
                        null,
                        List.of(),
                        cast,
                        List.of(),
                        List.of()
                );
            }
            return new Detail(
                    performance.getId(),
                    performance.getTitle(),
                    performance.getPosterUrl(),
                    performance.getGenre().name(),
                    detail.getKopisGenre(),
                    ShowStatus.of(detail.getStartDate(), detail.getEndDate(), today).name(),
                    rating,
                    List.copyOf(detail.getKeywords()),
                    detail.displaySynopsis(),
                    detail.synopsisSource().name(),
                    detail.getStartDate(),
                    detail.getEndDate(),
                    detail.isOpenRun(),
                    detail.getAgeText(),
                    new Venue(detail.getVenueId(), performance.getVenue()),
                    detail.getRuntimeText(),
                    performance.getRunningMinutes(),
                    detail.getPrices().stream()
                            .map(price -> new SeatPrice(price.getGrade(), price.getPrice()))
                            .toList(),
                    detail.getMinPrice(),
                    detail.getBookingLinks().stream()
                            .map(link -> new BookingLink(link.getName(), link.getUrl()))
                            .toList(),
                    cast,
                    List.copyOf(detail.getIntroImageUrls()),
                    List.copyOf(detail.getVideoIds())
            );
        }
    }

    @Schema(description = "평점. 평가가 없으면 average는 null")
    public record Rating(
            @Schema(description = "티켓 별점 단순 평균, 소수점 한 자리") Double average,
            @Schema(description = "평가 수 (티켓 수)") long count
    ) {
    }

    public record Venue(
            Long venueId,
            String name
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

    public record Actor(
            Long actorId,
            String name
    ) {
    }
}
