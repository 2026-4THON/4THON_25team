package com.fourthon.backend.recommendation.service;

import com.fourthon.backend.ai.service.ReviewAnalysisService;
import com.fourthon.backend.ai.service.ReviewSearchService;
import com.fourthon.backend.common.domain.TasteTag;
import com.fourthon.backend.recommendation.domain.PairJudgement;
import com.fourthon.backend.recommendation.domain.PairRecommendation;
import com.fourthon.backend.recommendation.domain.PairReview;
import com.fourthon.backend.recommendation.domain.PairStatus;
import com.fourthon.backend.recommendation.domain.UserTasteEmbedding;
import com.fourthon.backend.recommendation.dto.RecommendationResponse;
import com.fourthon.backend.recommendation.repository.PairRecommendationRepository;
import com.fourthon.backend.recommendation.repository.PairReviewRepository;
import com.fourthon.backend.recommendation.repository.UserTasteEmbeddingRepository;
import com.fourthon.backend.taste.service.TasteService;
import com.fourthon.backend.ticket.domain.CatalogActor;
import com.fourthon.backend.ticket.domain.TicketReview;
import com.fourthon.backend.ticket.domain.TicketReviewActor;
import com.fourthon.backend.ticket.repository.CatalogActorRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PairRecommendationService {

    private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");
    private static final int QUERY_TAG_COUNT = 5;
    private static final int MIN_PAIR_SIZE = 2;

    private final PairReviewRepository pairReviewRepository;
    private final PairRecommendationRepository pairRecommendationRepository;
    private final UserTasteEmbeddingRepository userTasteEmbeddingRepository;
    private final CatalogActorRepository catalogActorRepository;
    private final ReviewAnalysisService reviewAnalysisService;
    private final ReviewSearchService reviewSearchService;
    private final TasteService tasteService;
    private final PairSelector pairSelector;
    private final PairJudge pairJudge;
    private final TransactionTemplate transactionTemplate;

    public RecommendationResponse.Pair recommend(Long userId, Long performanceId) {
        Optional<PairRecommendation> saved = pairRecommendationRepository.findByUserIdAndPerformanceId(userId, performanceId);
        if (saved.isPresent()) {
            return toResponse(saved.get().getStatus(), saved.get().actorIdList(), saved.get().getReason());
        }
        List<PairReview> reviews = loadPairReviews(userId, performanceId);
        if (reviews.isEmpty()) {
            return new RecommendationResponse.Pair(PairStatus.NO_PAIR_REVIEWS.name(), List.of(), List.of(), null);
        }
        List<Long> fallback = pairSelector.mostReviewedCombo(reviews).orElse(List.of());
        if (!pairJudge.isAvailable()) {
            return toResponse(PairStatus.INSUFFICIENT_EVIDENCE, fallback, null);
        }
        Optional<String> briefing = tasteService.findBriefing(userId);
        List<String> myReviews = pairReviewRepository.findMyReviewContents(performanceId, userId).stream()
                .filter(content -> !content.isBlank())
                .toList();
        List<PairReview> evidence = selectEvidence(userId, performanceId, reviews, briefing, myReviews);
        PairJudgement judgement;
        try {
            judgement = pairJudge.judge(briefing.orElse(null), myReviews, evidence);
        } catch (RuntimeException e) {
            log.warn("AI 페어 추천 실패: userId={}, performanceId={}", userId, performanceId, e);
            return toResponse(PairStatus.INSUFFICIENT_EVIDENCE, fallback, null);
        }
        Optional<List<Long>> recommended = judgement.sufficient()
                ? pairSelector.comboOf(judgement.citationIds(), evidence)
                : Optional.empty();
        PairStatus status = recommended.isPresent() ? PairStatus.RECOMMENDED : PairStatus.INSUFFICIENT_EVIDENCE;
        List<Long> actorIds = recommended.orElse(fallback);
        String reason = recommended.isPresent() ? judgement.reason() : null;
        save(new PairRecommendation(
                userId,
                performanceId,
                status,
                actorIds,
                recommended.isPresent() ? judgement.citationIds() : List.of(),
                reason,
                LocalDateTime.now(SEOUL)
        ));
        return toResponse(status, actorIds, reason);
    }

    public void clear(Long userId) {
        transactionTemplate.executeWithoutResult(status -> {
            pairRecommendationRepository.deleteByUserId(userId);
            userTasteEmbeddingRepository.deleteByUserId(userId);
        });
    }

    private List<PairReview> selectEvidence(
            Long userId,
            Long performanceId,
            List<PairReview> reviews,
            Optional<String> briefing,
            List<String> myReviews) {

        if (reviews.size() <= pairSelector.maxEvidence()) {
            return reviews;
        }
        float[] query = null;
        if (reviewSearchService.isAvailable()) {
            try {
                reviewAnalysisService.fillMissing(performanceId);
                query = tasteEmbedding(userId, performanceId, briefing, myReviews);
            } catch (RuntimeException e) {
                log.warn("페어 추천용 임베딩 실패, 후기 수 기준으로 고릅니다: performanceId={}", performanceId, e);
            }
        }
        Map<Long, float[]> vectors = query == null ? Map.of() : reviewSearchService.findVectors(performanceId);
        return pairSelector.selectEvidence(reviews, vectors, query);
    }

    private float[] tasteEmbedding(Long userId, Long performanceId, Optional<String> briefing, List<String> myReviews) {
        String model = reviewSearchService.model();
        Optional<UserTasteEmbedding> saved =
                userTasteEmbeddingRepository.findByUserIdAndPerformanceIdAndModel(userId, performanceId, model);
        if (saved.isPresent()) {
            return saved.get().values();
        }
        String topTags = tasteService.findScores(userId).entrySet().stream()
                .sorted(Map.Entry.<TasteTag, Double>comparingByValue(Comparator.reverseOrder()))
                .limit(QUERY_TAG_COUNT)
                .map(entry -> entry.getKey().getLabel())
                .collect(Collectors.joining(", "));
        String text = briefing.orElse("") + "\n좋아하는 장르·분위기: " + topTags
                + "\n이 작품에 쓴 내 후기: " + String.join(" / ", myReviews);
        float[] values = reviewSearchService.embedQuery(text.strip());
        LocalDateTime now = LocalDateTime.now(SEOUL);
        transactionTemplate.executeWithoutResult(status -> {
            userTasteEmbeddingRepository.deleteByUserIdAndPerformanceId(userId, performanceId);
            userTasteEmbeddingRepository.save(new UserTasteEmbedding(userId, performanceId, model, values, now));
        });
        return values;
    }

    private List<PairReview> loadPairReviews(Long userId, Long performanceId) {
        List<PairReview> reviews = transactionTemplate.execute(status -> pairReviewRepository
                .findOthersReviews(performanceId, TicketReview.ReviewType.PAIR, userId).stream()
                .filter(review -> review.getContent() != null && !review.getContent().isBlank())
                .filter(review -> review.getActors().size() >= MIN_PAIR_SIZE)
                .map(this::toPairReview)
                .toList());
        return reviews == null ? List.of() : reviews;
    }

    private PairReview toPairReview(TicketReview review) {
        List<CatalogActor> actors = review.getActors().stream()
                .sorted(Comparator.comparingInt(TicketReviewActor::getSelectionOrder))
                .map(TicketReviewActor::getActor)
                .toList();
        return new PairReview(
                review.getId(),
                actors.stream().map(CatalogActor::getId).sorted().toList(),
                actors.stream().map(CatalogActor::getName).toList(),
                review.getContent()
        );
    }

    private void save(PairRecommendation recommendation) {
        try {
            transactionTemplate.executeWithoutResult(status -> pairRecommendationRepository.save(recommendation));
        } catch (RuntimeException e) {
            log.warn("페어 추천 저장 실패: userId={}, performanceId={}",
                    recommendation.getUserId(), recommendation.getPerformanceId(), e);
        }
    }

    private RecommendationResponse.Pair toResponse(PairStatus status, List<Long> actorIds, String reason) {
        Map<Long, String> names = catalogActorRepository.findAllById(actorIds).stream()
                .collect(Collectors.toMap(CatalogActor::getId, CatalogActor::getName, (left, right) -> left));
        List<RecommendationResponse.PairActor> actors = actorIds.stream()
                .map(id -> new RecommendationResponse.PairActor(id, names.get(id)))
                .toList();
        return new RecommendationResponse.Pair(status.name(), actorIds, actors, reason);
    }
}
