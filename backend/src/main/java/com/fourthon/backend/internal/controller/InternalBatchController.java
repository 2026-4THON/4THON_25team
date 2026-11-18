package com.fourthon.backend.internal.controller;

import com.fourthon.backend.ai.service.ReviewAnalysisService;
import com.fourthon.backend.internal.dto.InternalResponse;
import com.fourthon.backend.recommendation.service.RecommendationReasonService;
import com.fourthon.backend.kopis.service.KopisSyncService;
import com.fourthon.backend.show.service.ShowAnalysisService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

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
    private final RecommendationReasonService recommendationReasonService;

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
            description = "KOPIS 줄거리가 없는 진행중·예정 공연의 소개 이미지에서 줄거리를 뽑은 뒤, 아직 분석하지 않은 공연을 진행중·예정 공연부터 한 번에 최대 show-analysis.batch-size개 분석"
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
            summary = "지정한 공연의 소개 이미지 줄거리 다시 추출",
            description = "KOPIS 줄거리가 있어도 소개 이미지에서 줄거리를 뽑아 extracted_synopsis에 저장. KOPIS 줄거리가 없는 공연은 다시 분석"
    )
    @PostMapping("/synopsis-extraction")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public InternalResponse.Started runSynopsisExtraction(@RequestParam List<Long> performanceIds) {
        if (showAnalysisService.isRunning()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "공연 분석이 이미 실행 중입니다"
            );
        }
        showAnalysisService.extractInBackground(performanceIds);
        return new InternalResponse.Started("소개 이미지 줄거리 추출을 시작했습니다");
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

    @Operation(
            summary = "추천 이유 일괄 생성",
            description = "취향 리포트가 있는 모든 사용자의 홈 추천 중 이유가 없는 조합만 LLM으로 생성"
    )
    @PostMapping("/recommendation-reasons")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public InternalResponse.Started runRecommendationReasons() {
        if (recommendationReasonService.isBatchRunning()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "추천 이유 생성이 이미 실행 중입니다"
            );
        }
        recommendationReasonService.runBatchInBackground();
        return new InternalResponse.Started("추천 이유 생성을 시작했습니다");
    }
}
