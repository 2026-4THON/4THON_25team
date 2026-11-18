package com.fourthon.backend.recommendation.dto;

import com.fourthon.backend.recommendation.domain.ExploreMode;
import com.fourthon.backend.ticket.domain.Ticket;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

public final class RecommendationRequest {

    private RecommendationRequest() {
    }

    @Schema(description = "BE-REC-03 탐색 추천 입력")
    public record Explore(
            @Schema(description = "ALONE, PARTNER, FRIEND, FAMILY") @NotNull Ticket.Companion companion,
            @Schema(description = "희망 날짜 (복수)") @NotEmpty @Size(max = 31) List<@NotNull LocalDate> dates,
            @Schema(description = "1인 예산 (원)") @NotNull @Min(0) Integer budget,
            @Schema(description = "희망사항 문장. 비워도 됨") @Size(max = 200) String wish,
            @Schema(description = "REPORT(리포트 반영), NEW(새로 설정), ANY(아무거나)") @NotNull ExploreMode mode,
            @Schema(description = "NEW 모드에서 고른 장르 이름 (예: 로맨스)") List<@NotBlank String> genres,
            @Schema(description = "NEW 모드에서 고른 분위기 이름 (예: 밝고 유쾌한)") List<@NotBlank String> moods
    ) {
    }
}
