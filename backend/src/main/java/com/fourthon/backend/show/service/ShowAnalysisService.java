package com.fourthon.backend.show.service;

import com.fourthon.backend.ai.client.AiUnavailableException;
import com.fourthon.backend.ai.client.ImageInput;
import com.fourthon.backend.common.client.RequestThrottle;
import com.fourthon.backend.show.client.ShowImageClient;
import com.fourthon.backend.show.config.ShowAnalysisProperties;
import com.fourthon.backend.show.domain.ShowAnalysis;
import com.fourthon.backend.show.domain.ShowDetail;
import com.fourthon.backend.show.domain.ShowTagScore;
import com.fourthon.backend.show.event.ShowAnalysisCompletedEvent;
import com.fourthon.backend.show.repository.ShowDetailRepository;
import com.fourthon.backend.show.repository.ShowTagScoreRepository;
import com.fourthon.backend.ticket.domain.CatalogPerformance;
import com.fourthon.backend.ticket.repository.CatalogPerformanceRepository;

import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
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
import java.util.function.Supplier;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShowAnalysisService {

    private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");

    private final ShowDetailRepository showDetailRepository;
    private final ShowTagScoreRepository showTagScoreRepository;
    private final CatalogPerformanceRepository catalogPerformanceRepository;
    private final ShowAnalyzer showAnalyzer;
    private final ShowImageClient showImageClient;
    private final ShowTagScoreService showTagScoreService;
    private final ShowAnalysisProperties showAnalysisProperties;
    private final RequestThrottle showAnalysisThrottle;
    private final TransactionTemplate transactionTemplate;
    private final ApplicationEventPublisher eventPublisher;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final ExecutorService runner = Executors.newSingleThreadExecutor(
            runnable -> new Thread(runnable, "show-analysis")
    );

    public boolean isRunning() {
        return running.get();
    }

    public void runInBackground() {
        runner.execute(() -> run(this::findExtractionTargets));
    }

    public void extractInBackground(List<Long> performanceIds) {
        List<Long> targets = List.copyOf(performanceIds);
        runner.execute(() -> run(() -> targets));
    }

    private void run(Supplier<List<Long>> extractionTargets) {
        if (!running.compareAndSet(false, true)) {
            log.info("공연 분석이 이미 실행 중이라 건너뜀");
            return;
        }
        try {
            if (!showAnalyzer.isAvailable()) {
                log.warn("GEMINI_API_KEY가 없어 공연 분석을 건너뜀");
                return;
            }
            if (extractSynopses(extractionTargets.get())) {
                analyzeTargets();
            }
        } catch (RuntimeException e) {
            log.error("공연 분석 실패", e);
        } finally {
            running.set(false);
        }
    }

    private List<Long> findExtractionTargets() {
        return showDetailRepository.findExtractionTargets(
                LocalDate.now(SEOUL),
                showAnalysisProperties.minSynopsisLength(),
                showAnalysisProperties.extractionBatchSize()
        );
    }

    private boolean extractSynopses(List<Long> targets) {
        if (targets.isEmpty()) {
            return true;
        }
        log.info("소개 이미지 줄거리 추출 시작: 대상 {}건", targets.size());
        int extracted = 0;
        int empty = 0;
        int failed = 0;
        for (Long performanceId : targets) {
            try {
                if (extractOne(performanceId)) {
                    extracted++;
                } else {
                    empty++;
                }
            } catch (AiUnavailableException e) {
                log.error("Gemini를 지금 쓸 수 없어 줄거리 추출을 중단합니다. 남은 공연은 다음 실행에서 이어서 추출합니다", e);
                return false;
            } catch (RuntimeException e) {
                failed++;
                log.warn("소개 이미지 줄거리 추출 실패: performanceId={}", performanceId, e);
            }
        }
        log.info("소개 이미지 줄거리 추출 종료: 추출 {}, 내용 없음 {}, 실패 {}", extracted, empty, failed);
        return true;
    }

    private boolean extractOne(Long performanceId) {
        Optional<ShowDetail> detail = showDetailRepository.findById(performanceId);
        Optional<CatalogPerformance> performance = catalogPerformanceRepository.findById(performanceId);
        if (detail.isEmpty() || performance.isEmpty()) {
            return false;
        }
        List<ImageInput> images = showImageClient.fetch(showDetailRepository.findIntroImageUrls(performanceId));
        if (images.isEmpty()) {
            throw new IllegalStateException("소개 이미지를 하나도 받지 못했습니다");
        }
        showAnalysisThrottle.acquire();
        Optional<String> synopsis = showAnalyzer.extractSynopsis(
                performance.get().getTitle(),
                genreName(detail.get(), performance.get()),
                images
        );
        boolean reanalyze = !hasEnoughText(detail.get().getSynopsis());
        String extracted = synopsis.orElse("");
        transactionTemplate.executeWithoutResult(status -> showDetailRepository.findById(performanceId)
                .ifPresent(found -> found.saveExtractedSynopsis(extracted, reanalyze)));
        return synopsis.isPresent();
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
            } catch (AiUnavailableException e) {
                log.error("Gemini를 지금 쓸 수 없어(한도 초과 또는 과부하) 공연 분석을 중단합니다. 남은 공연은 다음 실행에서 이어서 분석합니다", e);
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
        eventPublisher.publishEvent(new ShowAnalysisCompletedEvent(count.analyzed + count.defaulted));
    }

    private void analyzeOne(Long performanceId, AnalysisCount count) {
        Optional<ShowDetail> detail = showDetailRepository.findById(performanceId);
        Optional<CatalogPerformance> performance = catalogPerformanceRepository.findById(performanceId);
        if (detail.isEmpty() || performance.isEmpty()) {
            return;
        }
        String synopsis = analysisText(detail.get());
        if (synopsis == null) {
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

    private String analysisText(ShowDetail detail) {
        return Stream.of(detail.getSynopsis(), detail.getExtractedSynopsis())
                .filter(this::hasEnoughText)
                .map(String::strip)
                .findFirst()
                .orElse(null);
    }

    private boolean hasEnoughText(String text) {
        return text != null && text.strip().length() >= showAnalysisProperties.minSynopsisLength();
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
        showTagScoreService.refreshReviewScores(List.of(performanceId));
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
