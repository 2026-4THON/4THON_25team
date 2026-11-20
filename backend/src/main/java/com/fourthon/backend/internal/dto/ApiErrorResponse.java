package com.fourthon.backend.internal.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "에러 응답")
public record ApiErrorResponse(
        @Schema(description = "에러 코드 (예: TASTE_REPORT_EXISTS, INVALID_INPUT)") String code,
        @Schema(description = "사용자에게 보여줄 한국어 메시지") String message
) {
}
