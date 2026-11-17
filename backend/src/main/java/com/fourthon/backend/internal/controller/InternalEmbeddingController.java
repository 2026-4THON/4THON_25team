package com.fourthon.backend.internal.controller;

import com.fourthon.backend.ai.dto.ReviewAnalysisResult;
import com.fourthon.backend.ai.service.ReviewAnalysisService;
import com.fourthon.backend.internal.dto.InternalResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Tag(
        name = "관리용 API",
        description = "동기화 수동 실행. X-Admin-Key 헤더 필요"
)
@RestController
@RequestMapping("/internal/embeddings")
@RequiredArgsConstructor
public class InternalEmbeddingController {

    private final ReviewAnalysisService reviewAnalysisService;

    @Operation(summary = "후기 임베딩·태그 점수 실패 목록")
    @GetMapping("/failures")
    public List<ReviewAnalysisResult.Failure> findFailures() {
        return reviewAnalysisService.findFailures();
    }

    @Operation(
            summary = "실패한 후기 다시 시도",
            description = "실패 횟수를 모두 지우고 누락된 후기 분석을 백그라운드로 실행"
    )
    @PostMapping("/retry")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public InternalResponse.Started retry() {
        if (reviewAnalysisService.isBatchRunning()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "후기 분석이 이미 실행 중입니다"
            );
        }
        reviewAnalysisService.resetFailures();
        reviewAnalysisService.runBatchInBackground();
        return new InternalResponse.Started("실패한 후기를 다시 분석합니다");
    }
}
