package com.fourthon.backend.show.controller;

import com.fourthon.backend.show.dto.ShowResponse;
import com.fourthon.backend.show.service.ShowService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(
        name = "작품 API",
        description = "작품 상세·평점 조회"
)
@RestController
@RequestMapping("/api/shows")
@RequiredArgsConstructor
public class ShowController {

    private final ShowService showService;

    @Operation(
            summary = "작품 상세 조회",
            description = "포스터, 상태, 평점, 줄거리, 기간, 공연장, 가격, 예매처, 출연진을 조회합니다."
    )
    @GetMapping("/{performanceId}")
    public ShowResponse.Detail getDetail(@PathVariable Long performanceId) {
        return showService.getDetail(performanceId);
    }
}
