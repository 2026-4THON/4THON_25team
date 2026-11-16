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
}
