package com.fourthon.backend.internal.controller;

import com.fourthon.backend.ai.service.ReviewAnalysisService;
import com.fourthon.backend.internal.dto.InternalResponse;
import com.fourthon.backend.kopis.service.KopisSyncService;
import com.fourthon.backend.show.service.ShowAnalysisService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@Tag(
        name = "관리용 API",
        description = "동기화 수동 실행. X-Admin-Key 헤더 필요"
)
@RestController
@RequestMapping("/internal/batch")
@RequiredArgsConstructor
public class InternalBatchController {

    private final KopisSyncService kopisSyncService;
    private final ShowAnalysisService showAnalysisService;
    private final ReviewAnalysisService reviewAnalysisService;

    @Operation(summary = "KOPIS 동기화 수동 실행")
    @PostMapping("/kopis-sync")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public InternalResponse.Started runKopisSync() {
        if (kopisSyncService.isRunning()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "KOPIS 동기화가 이미 실행 중입니다"
            );
        }
        kopisSyncService.runDailyInBackground();
        return new InternalResponse.Started("KOPIS 동기화를 시작했습니다");
    }

    @Operation(
            summary = "공연 키워드·태그 점수 분석 수동 실행",
            description = "아직 분석하지 않은 공연을 진행중·예정 공연부터 한 번에 최대 show-analysis.batch-size개 분석"
    )
    @PostMapping("/show-analysis")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public InternalResponse.Started runShowAnalysis() {
        if (showAnalysisService.isRunning()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "공연 분석이 이미 실행 중입니다"
            );
        }
        showAnalysisService.runInBackground();
        return new InternalResponse.Started("공연 분석을 시작했습니다");
    }

    @Operation(
            summary = "후기 임베딩·태그 점수 일괄 생성",
            description = "임베딩이 없거나 다른 모델로 만든 후기를 모두 분석. 3번 실패한 후기는 제외"
    )
    @PostMapping("/embeddings")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public InternalResponse.Started runEmbeddings() {
        if (reviewAnalysisService.isBatchRunning()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "후기 분석이 이미 실행 중입니다"
            );
        }
        reviewAnalysisService.runBatchInBackground();
        return new InternalResponse.Started("후기 분석을 시작했습니다");
    }
}
