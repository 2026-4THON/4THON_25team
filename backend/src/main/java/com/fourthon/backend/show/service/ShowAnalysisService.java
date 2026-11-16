package com.fourthon.backend.show.service;

import com.fourthon.backend.ai.client.AiRateLimitException;
import com.fourthon.backend.common.client.RequestThrottle;
import com.fourthon.backend.show.config.ShowAnalysisProperties;
import com.fourthon.backend.show.domain.ShowAnalysis;
import com.fourthon.backend.show.domain.ShowDetail;
import com.fourthon.backend.show.domain.ShowTagScore;
import com.fourthon.backend.show.repository.ShowDetailRepository;
import com.fourthon.backend.show.repository.ShowTagScoreRepository;
import com.fourthon.backend.ticket.domain.CatalogPerformance;
import com.fourthon.backend.ticket.repository.CatalogPerformanceRepository;

import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShowAnalysisService {

    private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");

    private final ShowDetailRepository showDetailRepository;
    private final ShowTagScoreRepository showTagScoreRepository;
    private final CatalogPerformanceRepository catalogPerformanceRepository;
    private final ShowAnalyzer showAnalyzer;
    private final ShowAnalysisProperties showAnalysisProperties;
    private final RequestThrottle showAnalysisThrottle;
    private final TransactionTemplate transactionTemplate;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final ExecutorService runner = Executors.newSingleThreadExecutor(
            runnable -> new Thread(runnable, "show-analysis")
    );

    public boolean isRunning() {
        return running.get();
    }

    public void runInBackground() {
        runner.execute(this::run);
    }

    public void run() {
        if (!running.compareAndSet(false, true)) {
            log.info("공연 분석이 이미 실행 중이라 건너뜀");
            return;
        }
        try {
            if (!showAnalyzer.isAvailable()) {
                log.warn("GEMINI_API_KEY가 없어 공연 분석을 건너뜀");
                return;
            }
            analyzeTargets();
        } catch (RuntimeException e) {
            log.error("공연 분석 실패", e);
        } finally {
            running.set(false);
        }
    }

    private void analyzeTargets() {
        List<Long> targets = showDetailRepository.findAnalysisTargets(
                LocalDate.now(SEOUL),
                showAnalysisProperties.maxFailCount(),
                PageRequest.of(0, showAnalysisProperties.batchSize())
        );
        log.info("공연 분석 시작: 대상 {}건", targets.size());
        AnalysisCount count = new AnalysisCount();
        for (Long performanceId : targets) {
            try {
                analyzeOne(performanceId, count);
            } catch (AiRateLimitException e) {
                log.error("Gemini 요청 한도에 걸려 공연 분석을 중단합니다. 남은 공연은 다음 실행에서 이어서 분석합니다", e);
                break;
            } catch (RuntimeException e) {
                count.failed++;
                log.warn("공연 분석 실패: performanceId={}", performanceId, e);
                transactionTemplate.executeWithoutResult(status -> showDetailRepository.findById(performanceId)
                        .ifPresent(ShowDetail::failAnalysis));
            }
        }
        log.info("공연 분석 종료: AI 분석 {}, 장르 기본값 {}, 실패 {}, 남음 {}",
                count.analyzed, count.defaulted, count.failed,
                targets.size() - count.analyzed - count.defaulted - count.failed);
    }

    private void analyzeOne(Long performanceId, AnalysisCount count) {
        Optional<ShowDetail> detail = showDetailRepository.findById(performanceId);
        Optional<CatalogPerformance> performance = catalogPerformanceRepository.findById(performanceId);
        if (detail.isEmpty() || performance.isEmpty()) {
            return;
        }
        String synopsis = detail.get().getSynopsis();
        if (synopsis == null || synopsis.strip().length() < showAnalysisProperties.minSynopsisLength()) {
            save(performanceId, showAnalyzer.defaultAnalysis(performance.get().getGenre()));
            count.defaulted++;
            return;
        }
        showAnalysisThrottle.acquire();
        ShowAnalysis analysis = showAnalyzer.analyze(
                performance.get().getTitle(),
                genreName(detail.get(), performance.get()),
                synopsis.strip()
        );
        save(performanceId, analysis);
        count.analyzed++;
    }

    private String genreName(ShowDetail detail, CatalogPerformance performance) {
        if (detail.getKopisGenre() != null && !detail.getKopisGenre().isBlank()) {
            return detail.getKopisGenre();
        }
        return performance.getGenre().name();
    }

    private void save(Long performanceId, ShowAnalysis analysis) {
        LocalDateTime now = LocalDateTime.now(SEOUL);
        transactionTemplate.executeWithoutResult(status -> {
            showTagScoreRepository.deleteByPerformanceId(performanceId);
            showTagScoreRepository.saveAll(analysis.tagScores().entrySet().stream()
                    .map(entry -> new ShowTagScore(performanceId, entry.getKey(), entry.getValue()))
                    .toList());
            showDetailRepository.findById(performanceId)
                    .ifPresent(detail -> detail.completeAnalysis(analysis.keywords(), now));
        });
    }

    @PreDestroy
    public void shutdown() {
        runner.shutdownNow();
    }

    private static class AnalysisCount {

        private int analyzed;
        private int defaulted;
        private int failed;
    }
}
