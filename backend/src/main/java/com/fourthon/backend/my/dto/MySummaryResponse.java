
package com.fourthon.backend.my.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "BE-MY-01 마이페이지 요약 응답")
public record MySummaryResponse(

        @Schema(description = "사용자 닉네임")
        String nickname,

        @Schema(description = "취향 브리핑. 없으면 빈 문자열")
        String tasteBriefing,

        TicketCounts ticketCounts,

        ReviewCounts reviewCounts,

        Expenses expenses
) {

    @Schema(description = "장르별 티켓 개수")
    public record TicketCounts(
            long musical,
            long play,
            long other
    ) {}

    @Schema(description = "유형별 후기 개수")
    public record ReviewCounts(
            long performance,
            long actor,
            long pair
    ) {}

    @Schema(description = "지출 요약")
    public record Expenses(
            long ticketTotal,
            long culturePassTotal,
            long mdTotal,
            long totalSpending
    ) {}
}
