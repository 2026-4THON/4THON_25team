package com.fourthon.backend.recommendation.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.List;

public final class RecommendationResponse {

    private RecommendationResponse() {
    }

    @Schema(description = "BE-REC-01 홈 취향 추천")
    public record Home(
            @Schema(description = "취향 리포트가 있는지. false면 items는 빈 목록") boolean hasReport,
            @Schema(description = "취향 한 줄 브리핑. 리포트가 없으면 null") String briefing,
            @Schema(description = "추천 공연. 기준을 통과한 공연이 적으면 5개보다 적음") List<HomeItem> items
    ) {
    }

    public record HomeItem(
            Long performanceId,
            String title,
            @Schema(description = "MUSICAL, PLAY, ETC") String genre,
            String posterUrl,
            @Schema(description = "UPCOMING, ONGOING, ENDED") String status,
            LocalDate startDate,
            LocalDate endDate,
            @Schema(description = "추천 이유. 공백 포함 24자 이하") String reason
    ) {
    }

    @Schema(description = "BE-REC-02 지금 가장 인기 있는 공연")
    public record PopularItem(
            @Schema(description = "장르 탭 안에서의 순위 (1부터)") int rank,
            Long performanceId,
            String title,
            @Schema(description = "MUSICAL, PLAY, ETC") String genre,
            String posterUrl,
            @Schema(description = "UPCOMING, ONGOING, ENDED") String status
    ) {
    }

    @Schema(description = "BE-REC-03 탐색 추천 결과")
    public record Explore(
            @Schema(description = "OK, NO_MATCH(조건에 맞는 공연 없음)") String status,
            @Schema(description = "요약 문구") String summary,
            @Schema(description = "추천 공연 최대 4개") List<ExploreItem> items
    ) {
    }

    public record ExploreItem(
            Long performanceId,
            String title,
            @Schema(description = "MUSICAL, PLAY, ETC") String genre,
            String posterUrl,
            LocalDate startDate,
            LocalDate endDate,
            @Schema(description = "최저가 (원)") Integer minPrice,
            @Schema(description = "희망 날짜 중 회차가 있는 날짜") List<LocalDate> availableDates
    ) {
    }

    @Schema(description = "BE-REC-08 AI 페어 추천")
    public record Pair(
            @Schema(description = "RECOMMENDED, INSUFFICIENT_EVIDENCE(근거 부족, 후기가 가장 많은 조합), NO_PAIR_REVIEWS(페어 후기 없음)")
            String status,
            @Schema(description = "추천 페어 배우 ID. 후기 목록은 GET /api/shows/{performanceId}/pairs/reviews?actorIds= 로 조회")
            List<Long> actorIds,
            List<PairActor> actors,
            @Schema(description = "추천 이유. 없으면 null") String reason
    ) {
    }

    public record PairActor(
            Long actorId,
            String name
    ) {
    }
}
