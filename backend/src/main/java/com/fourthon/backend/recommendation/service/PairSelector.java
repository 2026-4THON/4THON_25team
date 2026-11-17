package com.fourthon.backend.recommendation.service;

import com.fourthon.backend.ai.domain.VectorCodec;
import com.fourthon.backend.recommendation.config.RecommendationProperties;
import com.fourthon.backend.recommendation.domain.PairReview;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class PairSelector {

    private final RecommendationProperties recommendationProperties;

    public int maxEvidence() {
        return recommendationProperties.pair().maxEvidence();
    }

    public List<PairReview> selectEvidence(List<PairReview> reviews, Map<Long, float[]> vectors, float[] query) {
        RecommendationProperties.Pair pair = recommendationProperties.pair();
        if (reviews.size() <= pair.maxEvidence()) {
            return reviews;
        }
        if (query == null) {
            return byReviewCount(reviews, pair);
        }
        Map<Long, Double> similarities = reviews.stream()
                .filter(review -> vectors.containsKey(review.reviewId()))
                .collect(Collectors.toMap(
                        PairReview::reviewId,
                        review -> VectorCodec.dot(query, vectors.get(review.reviewId()))
                ));
        Map<String, List<PairReview>> byCombo = reviews.stream()
                .filter(review -> similarities.containsKey(review.reviewId()))
                .sorted(Comparator.comparingDouble((PairReview review) -> similarities.get(review.reviewId())).reversed())
                .collect(Collectors.groupingBy(PairReview::comboKey, LinkedHashMap::new, Collectors.toList()));
        if (byCombo.isEmpty()) {
            return byReviewCount(reviews, pair);
        }
        return byCombo.values().stream()
                .sorted(Comparator.comparingDouble((List<PairReview> combo) ->
                        comboScore(combo, similarities, pair.topSimilarityCount())).reversed())
                .limit(pair.comboCount())
                .flatMap(combo -> combo.stream().limit(pair.reviewsPerCombo()))
                .toList();
    }

    public Optional<List<Long>> comboOf(Collection<Long> citationIds, List<PairReview> reviews) {
        Map<Long, PairReview> reviewById = reviews.stream()
                .collect(Collectors.toMap(PairReview::reviewId, Function.identity(), (left, right) -> left));
        Map<String, Long> totalCounts = countByCombo(reviews);
        Map<String, Long> citedCounts = citationIds.stream()
                .map(reviewById::get)
                .filter(review -> review != null)
                .collect(Collectors.groupingBy(PairReview::comboKey, Collectors.counting()));
        return citedCounts.entrySet().stream()
                .max(Comparator.comparingLong((Map.Entry<String, Long> entry) -> entry.getValue())
                        .thenComparingLong(entry -> totalCounts.getOrDefault(entry.getKey(), 0L)))
                .flatMap(entry -> actorIdsOf(entry.getKey(), reviews));
    }

    public Optional<List<Long>> mostReviewedCombo(List<PairReview> reviews) {
        return countByCombo(reviews).entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .flatMap(entry -> actorIdsOf(entry.getKey(), reviews));
    }

    private List<PairReview> byReviewCount(List<PairReview> reviews, RecommendationProperties.Pair pair) {
        Map<String, List<PairReview>> byCombo = reviews.stream()
                .collect(Collectors.groupingBy(PairReview::comboKey, LinkedHashMap::new, Collectors.toList()));
        return byCombo.values().stream()
                .sorted(Comparator.comparingInt((List<PairReview> combo) -> combo.size()).reversed())
                .limit(pair.comboCount())
                .flatMap(combo -> combo.stream().limit(pair.reviewsPerCombo()))
                .toList();
    }

    private double comboScore(List<PairReview> sortedCombo, Map<Long, Double> similarities, int topCount) {
        return sortedCombo.stream()
                .limit(topCount)
                .mapToDouble(review -> similarities.get(review.reviewId()))
                .average()
                .orElse(0.0);
    }

    private Map<String, Long> countByCombo(List<PairReview> reviews) {
        return reviews.stream().collect(Collectors.groupingBy(PairReview::comboKey, Collectors.counting()));
    }

    private Optional<List<Long>> actorIdsOf(String comboKey, List<PairReview> reviews) {
        return reviews.stream()
                .filter(review -> review.comboKey().equals(comboKey))
                .findFirst()
                .map(PairReview::actorIds);
    }
}
