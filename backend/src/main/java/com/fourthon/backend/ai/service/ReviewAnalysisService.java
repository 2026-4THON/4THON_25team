package com.fourthon.backend.ai.service;

import com.fourthon.backend.ai.client.AiRateLimitException;
import com.fourthon.backend.ai.client.EmbeddingClient;
import com.fourthon.backend.ai.config.ReviewAnalysisProperties;
import com.fourthon.backend.ai.domain.ReviewAnalysisFailure;
import com.fourthon.backend.ai.domain.ReviewEmbedding;
import com.fourthon.backend.ai.domain.ReviewSource;
import com.fourthon.backend.ai.domain.ReviewTagScore;
import com.fourthon.backend.ai.dto.ReviewAnalysisResult;
import com.fourthon.backend.ai.event.ReviewTagScoresChangedEvent;
import com.fourthon.backend.ai.repository.ReviewAnalysisFailureRepository;
import com.fourthon.backend.ai.repository.ReviewEmbeddingRepository;
import com.fourthon.backend.ai.repository.ReviewSourceRepository;
import com.fourthon.backend.ai.repository.ReviewTagScoreRepository;
import com.fourthon.backend.common.client.RequestThrottle;
import com.fourthon.backend.common.domain.TasteTag;
import com.fourthon.backend.internal.event.TicketChangeType;
import com.fourthon.backend.internal.event.TicketChangedEvent;
import com.fourthon.backend.ticket.domain.Ticket;
import com.fourthon.backend.ticket.domain.TicketReview;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewAnalysisService {

    private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");

    private final ReviewSourceRepository reviewSourceRepository;
    private final ReviewEmbeddingRepository reviewEmbeddingRepository;
    private final ReviewTagScoreRepository reviewTagScoreRepository;
    private final ReviewAnalysisFailureRepository reviewAnalysisFailureRepository;
    private final EmbeddingClient embeddingClient;
    private final ReviewTagAnalyzer reviewTagAnalyzer;
    private final ReviewTextBuilder reviewTextBuilder;
    private final ReviewAnalysisProperties reviewAnalysisProperties;
    private final RequestThrottle reviewAnalysisThrottle;
    private final TransactionTemplate transactionTemplate;
    private final ApplicationEventPublisher eventPublisher;
    private final AtomicBoolean batchRunning = new AtomicBoolean(false);
    private final ExecutorService batchRunner = Executors.newSingleThreadExecutor(
            runnable -> new Thread(runnable, "review-analysis-batch")
    );
    private ExecutorService eventRunner;

    @PostConstruct
    void startEventRunner() {
        AtomicInteger sequence = new AtomicInteger();
        eventRunner = Executors.newFixedThreadPool(
                reviewAnalysisProperties.threads(),
                runnable -> new Thread(runnable, "review-analysis-" + sequence.incrementAndGet())
        );
    }

    public void handleInBackground(TicketChangedEvent event) {
        eventRunner.execute(() -> handle(event));
    }

    public void handle(TicketChangedEvent event) {
        try {
            Set<Long> changed = new LinkedHashSet<>();
            if (event.type() != TicketChangeType.CREATED) {
                changed.addAll(deleteByTicket(event.ticketId()));
            }
            if (event.type() != TicketChangeType.DELETED) {
                changed.addAll(analyze(loadByTicket(event.ticketId())).changedPerformanceIds());
            }
            publish(changed);
        } catch (RuntimeException e) {
            log.error("티켓 후기 분석 실패: ticketId={}, type={}", event.ticketId(), event.type(), e);
        }
    }

    public ReviewAnalysisResult.Batch fillMissing(Long performanceId) {
        return fillMissing(performanceId, reviewAnalysisProperties.missingLimit());
    }

    public ReviewAnalysisResult.Batch fillMissing(Long performanceId, int limit) {
        if (!embeddingClient.isConfigured()) {
            return new ReviewAnalysisResult.Batch(0, 0, true);
        }
        List<Long> reviewIds = reviewSourceRepository.findMissingIds(
                performanceId,
                embeddingClient.model(),
                reviewAnalysisProperties.maxFailCount(),
                PageRequest.of(0, limit)
        );
        if (reviewIds.isEmpty()) {
            return new ReviewAnalysisResult.Batch(0, 0, false);
        }
        Outcome outcome = analyze(loadByIds(reviewIds));
        publish(outcome.changedPerformanceIds());
        return new ReviewAnalysisResult.Batch(outcome.analyzed(), outcome.failed(), outcome.stopped());
    }

    public boolean isBatchRunning() {
        return batchRunning.get();
    }

    public void runBatchInBackground() {
        batchRunner.execute(this::runBatch);
    }

    public List<ReviewAnalysisResult.Failure> findFailures() {
        return reviewAnalysisFailureRepository.findAllByOrderByFailedAtDesc().stream()
                .map(failure -> new ReviewAnalysisResult.Failure(
                        failure.getReviewId(),
                        failure.getTicketId(),
                        failure.getFailCount(),
                        failure.getLastError(),
                        failure.getFailedAt()
                ))
                .toList();
    }

    public void resetFailures() {
        transactionTemplate.executeWithoutResult(status -> reviewAnalysisFailureRepository.deleteAllInBatch());
    }

    private void runBatch() {
        if (!batchRunning.compareAndSet(false, true)) {
            log.info("후기 분석 배치가 이미 실행 중이라 건너뜀");
            return;
        }
        int analyzed = 0;
        int failed = 0;
        try {
            if (!embeddingClient.isConfigured()) {
                log.warn("GEMINI_API_KEY가 없어 후기 분석 배치를 건너뜀");
                return;
            }
            log.info("후기 분석 배치 시작");
            while (true) {
                ReviewAnalysisResult.Batch batch = fillMissing(null, reviewAnalysisProperties.batchPageSize());
                analyzed += batch.analyzed();
                failed += batch.failed();
                if (batch.stopped() || batch.analyzed() + batch.failed() == 0) {
                    break;
                }
            }
        } catch (RuntimeException e) {
            log.error("후기 분석 배치 실패", e);
        } finally {
            log.info("후기 분석 배치 종료: 성공 {}, 실패 {}", analyzed, failed);
            batchRunning.set(false);
        }
    }

    private Outcome analyze(List<ReviewSource> sources) {
        Outcome outcome = new Outcome();
        if (sources.isEmpty()) {
            return outcome;
        }
        if (!embeddingClient.isConfigured()) {
            log.warn("GEMINI_API_KEY가 없어 후기 분석을 건너뜀");
            outcome.stopped = true;
            return outcome;
        }
        for (ReviewSource source : sources) {
            try {
                analyzeOne(source);
                outcome.analyzed++;
                if (source.isPerformanceReview()) {
                    outcome.changedPerformanceIds.add(source.performanceId());
                }
            } catch (AiRateLimitException e) {
                log.error("Gemini 요청 한도에 걸려 후기 분석을 중단합니다. 남은 후기는 다음 추천 때 다시 시도합니다", e);
                outcome.stopped = true;
                break;
            } catch (RuntimeException e) {
                outcome.failed++;
                recordFailure(source, e);
            }
        }
        return outcome;
    }

    private void analyzeOne(ReviewSource source) {
        reviewAnalysisThrottle.acquire();
        float[] vector = embeddingClient.embed(reviewTextBuilder.build(source));
        Map<TasteTag, Double> tagScores = Map.of();
        if (source.isPerformanceReview()) {
            reviewAnalysisThrottle.acquire();
            tagScores = reviewTagAnalyzer.analyze(source.performanceTitle(), source.content());
        }
        LocalDateTime now = LocalDateTime.now(SEOUL);
        Map<TasteTag, Double> scores = tagScores;
        transactionTemplate.executeWithoutResult(status -> {
            reviewEmbeddingRepository.deleteByReviewId(source.reviewId());
            reviewTagScoreRepository.deleteByReviewId(source.reviewId());
            reviewEmbeddingRepository.save(new ReviewEmbedding(source, embeddingClient.model(), vector, now));
            reviewTagScoreRepository.saveAll(scores.entrySet().stream()
                    .map(entry -> new ReviewTagScore(source, entry.getKey(), entry.getValue()))
                    .toList());
            reviewAnalysisFailureRepository.findById(source.reviewId())
                    .ifPresent(reviewAnalysisFailureRepository::delete);
        });
    }

    private void recordFailure(ReviewSource source, RuntimeException cause) {
        LocalDateTime now = LocalDateTime.now(SEOUL);
        Integer failCount = transactionTemplate.execute(status -> {
            ReviewAnalysisFailure failure = reviewAnalysisFailureRepository.findById(source.reviewId())
                    .orElseGet(() -> new ReviewAnalysisFailure(source.reviewId(), source.ticketId()));
            failure.record(cause.getMessage(), now);
            return reviewAnalysisFailureRepository.save(failure).getFailCount();
        });
        if (failCount != null && failCount >= reviewAnalysisProperties.maxFailCount()) {
            log.error("후기 분석 {}회 실패로 중단: reviewId={}", failCount, source.reviewId(), cause);
        } else {
            log.warn("후기 분석 실패: reviewId={}, 실패 {}회", source.reviewId(), failCount, cause);
        }
    }

    private Set<Long> deleteByTicket(Long ticketId) {
        Set<Long> performanceIds = transactionTemplate.execute(status -> {
            Set<Long> ids = new LinkedHashSet<>(reviewTagScoreRepository.findPerformanceIdsByTicketId(ticketId));
            reviewEmbeddingRepository.deleteByTicketId(ticketId);
            reviewTagScoreRepository.deleteByTicketId(ticketId);
            reviewAnalysisFailureRepository.deleteByTicketId(ticketId);
            return ids;
        });
        return performanceIds == null ? Set.of() : performanceIds;
    }

    private List<ReviewSource> loadByTicket(Long ticketId) {
        return load(() -> reviewSourceRepository.findByTicketId(ticketId));
    }

    private List<ReviewSource> loadByIds(Collection<Long> reviewIds) {
        return load(() -> reviewSourceRepository.findByIds(reviewIds));
    }

    private List<ReviewSource> load(Supplier<List<TicketReview>> query) {
        List<ReviewSource> sources = transactionTemplate.execute(status -> query.get().stream()
                .filter(review -> review.getContent() != null && !review.getContent().isBlank())
                .map(this::toSource)
                .toList());
        return sources == null ? List.of() : sources;
    }

    private ReviewSource toSource(TicketReview review) {
        Ticket ticket = review.getTicket();
        return new ReviewSource(
                review.getId(),
                ticket.getId(),
                ticket.getPerformance().getId(),
                ticket.getPerformance().getTitle(),
                review.getType(),
                review.getActors().stream()
                        .map(actor -> actor.getActor().getName())
                        .toList(),
                ticket.getRatingHalfUnits() / 2.0,
                review.getContent()
        );
    }

    private void publish(Set<Long> performanceIds) {
        if (!performanceIds.isEmpty()) {
            eventPublisher.publishEvent(new ReviewTagScoresChangedEvent(Set.copyOf(performanceIds)));
        }
    }

    @PreDestroy
    public void shutdown() {
        batchRunner.shutdownNow();
        if (eventRunner != null) {
            eventRunner.shutdownNow();
        }
    }

    private static class Outcome {

        private int analyzed;
        private int failed;
        private boolean stopped;
        private final Set<Long> changedPerformanceIds = new LinkedHashSet<>();

        private int analyzed() {
            return analyzed;
        }

        private int failed() {
            return failed;
        }

        private boolean stopped() {
            return stopped;
        }

        private Set<Long> changedPerformanceIds() {
            return changedPerformanceIds;
        }
    }
}
