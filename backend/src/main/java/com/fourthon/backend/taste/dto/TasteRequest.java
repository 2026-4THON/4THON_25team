package com.fourthon.backend.taste.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public final class TasteRequest {

    private TasteRequest() {
    }

    @Schema(description = "BE-ANL-01 취향 리포트 저장·수정")
    public record Save(
            @Schema(description = "고른 장르 이름 (예: 로맨스, 미스터리·추리)")
            @NotEmpty List<@NotBlank String> genres,

            @Schema(description = "고른 분위기 이름 (예: 밝고 유쾌한)")
            @NotEmpty List<@NotBlank String> moods,

            @NotEmpty @Valid List<LifeWorkInput> lifeWorks,

            @Schema(description = "VS 8문항에서 고른 쪽의 문구 8개")
            @NotNull List<@NotBlank String> vsChoices,

            @NotBlank @Size(max = 1000) String freeText
    ) {
    }

    public record LifeWorkInput(
            @Schema(description = "book, video, comic, music, theater")
            @NotBlank String category,

            @NotBlank @Size(max = 100) String title
    ) {
    }
}
