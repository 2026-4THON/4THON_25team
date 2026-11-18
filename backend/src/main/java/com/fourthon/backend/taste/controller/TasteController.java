package com.fourthon.backend.taste.controller;

import com.fourthon.backend.auth.resolver.CurrentUser;
import com.fourthon.backend.taste.dto.TasteRequest;
import com.fourthon.backend.taste.dto.TasteResponse;
import com.fourthon.backend.taste.service.TasteService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(
        name = "취향 분석 API",
        description = "분석 탭 취향 리포트 저장·조회·수정"
)
@RestController
@RequestMapping("/api/analysis/report")
@RequiredArgsConstructor
public class TasteController {

    private final TasteService tasteService;

    @Operation(summary = "내 취향 리포트 조회", description = "리포트가 없으면 404")
    @GetMapping
    public TasteResponse.Report getReport(@Parameter(hidden = true) @CurrentUser Long userId) {
        return tasteService.getReport(userId);
    }

    @Operation(summary = "취향 리포트 저장", description = "AI 브리핑과 취향 점수를 함께 만듭니다. 이미 있으면 409")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TasteResponse.Report create(
            @Parameter(hidden = true) @CurrentUser Long userId,
            @Valid @RequestBody TasteRequest.Save request) {

        return tasteService.create(userId, request);
    }

    @Operation(summary = "취향 리포트 수정", description = "전체 입력을 다시 받아 브리핑과 취향 점수를 다시 만듭니다")
    @PutMapping
    public TasteResponse.Report update(
            @Parameter(hidden = true) @CurrentUser Long userId,
            @Valid @RequestBody TasteRequest.Save request) {

        return tasteService.update(userId, request);
    }
}
