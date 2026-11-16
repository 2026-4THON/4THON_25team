package com.fourthon.backend.recommendation.service;

import com.fourthon.backend.ai.client.AiRateLimitException;
import com.fourthon.backend.ai.dto.ReviewAnalysisResult;
import com.fourthon.backend.ai.service.ReviewSearchService;
import com.fourthon.backend.common.client.RequestThrottle;
import com.fourthon.backend.common.domain.TasteTag;
import com.fourthon.backend.recommendation.config.RecommendationProperties;
import com.fourthon.backend.recommendation.domain.GeneratedReason;
import com.fourthon.backend.recommendation.domain.GenreTab;
import com.fourthon.backend.recommendation.domain.HomePicks;
import com.fourthon.backend.recommendation.domain.HomeScope;
import com.fourthon.backend.recommendation.domain.ReasonTarget;
import com.fourthon.backend.recommendation.domain.RecommendationReason;
import com.fourthon.backend.recommendation.repository.RecommendationReasonRepository;
import com.fourthon.backend.recommendation.score.ScoredShow;
import com.fourthon.backend.taste.service.TasteService;

import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendationReasonService {

    private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");
    private static final int QUERY_TAG_COUNT = 5;

    private final HomeRecommendationService homeRecommendationService;
    private final ReasonGenerator reasonGenerator;
    private final AiReasonGenerator aiReasonGenerator;
    private final ReviewSearchService reviewSearchService;
    private final TasteService tasteService;
    private final RecommendationReasonRepository recommendationReasonRepository;
    private final RecommendationProperties recommendationProperties;
    private final RequestThrottle reasonThrottle;
    private final TransactionTemplate transactionTemplate;
    private final AtomicBoolean batchRunning = new AtomicBoolean(false);
    private final ExecutorService runner = Executors.newSingleThreadExecutor(
            runnable -> new Thread(runnable, "recommendation-reason")
    );

    public void regenerateInBackground(Long userId, boolean clearSaved) {
        runner.execute(() -> {
            try {
                if (clearSaved) {
                    transactionTemplate.executeWithoutResult(status ->
                            recommendationReasonRepository.deleteByUserId(userId));
                }
                generateFor(userId);
            } catch (RuntimeException e) {
                log.error("추천 이유 생성 실패: userId={}", userId, e);
            }
        });
    }

    public boolean isBatchRunning() {
        return batchRunning.get();
    }

    public void runBatchInBackground() {
        runner.execute(this::runBatch);
    }

    public int generateFor(Long userId) {
        if (!aiReasonGenerator.isAvailable()) {
            return 0;
        }
        Set<Long> saved = new HashSet<>(recommendationReasonRepository.findPerformanceIdsByUserId(userId));
        Map<Long, ScoredShow> targets = new LinkedHashMap<>();
        HomePicks context = null;
        for (GenreTab genreTab : GenreTab.values()) {
            for (HomeScope scope : HomeScope.values()) {
                HomePicks picks = homeRecommendationService.pick(userId, genreTab, scope);
                if (!picks.hasReport()) {
                    return 0;
                }
                context = picks;
                picks.shows().stream()
                        .filter(show -> !saved.contains(show.candidate().performanceId()))
                        .forEach(show -> targets.putIfAbsent(show.candidate().performanceId(), show));
            }
        }
        if (targets.isEmpty() || context == null) {
            return 0;
        }
        List<ReasonTarget> reasonTargets = toTargets(userId, context, targets.values().stream()
                .limit(recommendationProperties.reason().maxTargets())
                .toList());
        reasonThrottle.acquire();
        List<GeneratedReason> generated = aiReasonGenerator.generate(context.briefing(), reasonTargets);
        LocalDateTime now = LocalDateTime.now(SEOUL);
        transactionTemplate.executeWithoutResult(status -> {
            Set<Long> existing = new HashSet<>(recommendationReasonRepository.findPerformanceIdsByUserId(userId));
            recommendationReasonRepository.saveAll(generated.stream()
                    .filter(reason -> !existing.contains(reason.performanceId()))
                    .map(reason -> new RecommendationReason(
                            userId,
                            reason.performanceId(),
                            reason.reason(),
                            reason.citationIds(),
                            now
                    ))
                    .toList());
        });
        log.info("추천 이유 생성: userId={}, 대상 {}, 저장 {}", userId, reasonTargets.size(), generated.size());
        return generated.size();
    }

    private void runBatch() {
        if (!batchRunning.compareAndSet(false, true)) {
            log.info("추천 이유 배치가 이미 실행 중이라 건너뜀");
            return;
        }
        int users = 0;
        int saved = 0;
        try {
            if (!aiReasonGenerator.isAvailable()) {
                log.warn("GEMINI_API_KEY가 없어 추천 이유 배치를 건너뜀");
                return;
            }
            for (Long userId : tasteService.findUserIdsWithReport()) {
                try {
                    saved += generateFor(userId);
                    users++;
                } catch (AiRateLimitException e) {
                    log.error("Gemini 요청 한도에 걸려 추천 이유 배치를 중단합니다", e);
                    break;
                } catch (RuntimeException e) {
                    log.warn("추천 이유 생성 실패: userId={}", userId, e);
                }
            }
        } finally {
            log.info("추천 이유 배치 종료: 사용자 {}, 저장 {}", users, saved);
            batchRunning.set(false);
        }
    }

    private List<ReasonTarget> toTargets(Long userId, HomePicks context, List<ScoredShow> shows) {
        float[] query = embedQuery(context);
        List<ReasonTarget> targets = new ArrayList<>();
        for (ScoredShow show : shows) {
            Long performanceId = show.candidate().performanceId();
            List<ReviewAnalysisResult.Evidence> reviews = query == null
                    ? List.of()
                    : reviewSearchService.findSimilar(
                            performanceId,
                            query,
                            userId,
                            recommendationProperties.reason().evidenceCount()
                    );
            targets.add(new ReasonTarget(
                    performanceId,
                    show.candidate().title(),
                    reasonGenerator.homeReason(show, context.weights(), context.tasteScores(), context.lifeWorkTitle()),
                    reviews
            ));
        }
        return targets;
    }

    private float[] embedQuery(HomePicks context) {
        if (!reviewSearchService.isAvailable()) {
            return null;
        }
        String topTags = context.tasteScores().entrySet().stream()
                .sorted(Map.Entry.<TasteTag, Double>comparingByValue(Comparator.reverseOrder()))
                .limit(QUERY_TAG_COUNT)
                .map(entry -> entry.getKey().getLabel())
                .collect(Collectors.joining(", "));
        try {
            return reviewSearchService.embedQuery(context.briefing() + "\n좋아하는 장르·분위기: " + topTags);
        } catch (AiRateLimitException e) {
            throw e;
        } catch (RuntimeException e) {
            log.warn("추천 이유 근거 검색용 임베딩 실패, 후기 없이 생성합니다", e);
            return null;
        }
    }

    @PreDestroy
    public void shutdown() {
        runner.shutdownNow();
    }
}
