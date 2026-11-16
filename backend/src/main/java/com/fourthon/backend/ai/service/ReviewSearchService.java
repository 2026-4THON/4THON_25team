package com.fourthon.backend.ai.service;

import com.fourthon.backend.ai.client.EmbeddingClient;
import com.fourthon.backend.ai.domain.ReviewEmbedding;
import com.fourthon.backend.ai.dto.ReviewAnalysisResult;
import com.fourthon.backend.ai.repository.ReviewEmbeddingRepository;
import com.fourthon.backend.ai.repository.ReviewSourceRepository;
import com.fourthon.backend.ticket.domain.TicketReview;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewSearchService {

    private static final int CANDIDATE_MULTIPLIER = 3;

    private final EmbeddingClient embeddingClient;
    private final ReviewEmbeddingRepository reviewEmbeddingRepository;
    private final ReviewSourceRepository reviewSourceRepository;

    public boolean isAvailable() {
        return embeddingClient.isConfigured();
    }

    public float[] embedQuery(String text) {
        return embeddingClient.embed(text);
    }

    public List<ReviewAnalysisResult.Evidence> findSimilar(
            Long performanceId,
            float[] query,
            Long excludedUserId,
            int limit) {

        List<Scored> ranked = reviewEmbeddingRepository
                .findByPerformanceIdAndModel(performanceId, embeddingClient.model()).stream()
                .filter(embedding -> embedding.getDimension() == query.length)
                .map(embedding -> new Scored(embedding.getReviewId(), dot(query, embedding.values())))
                .sorted(Comparator.comparingDouble(Scored::similarity).reversed())
                .limit((long) limit * CANDIDATE_MULTIPLIER)
                .toList();
        if (ranked.isEmpty()) {
            return List.of();
        }
        Map<Long, TicketReview> reviews = reviewSourceRepository.findByIds(ranked.stream().map(Scored::reviewId).toList())
                .stream()
                .filter(review -> !Objects.equals(review.getTicket().getUser().getId(), excludedUserId))
                .filter(review -> review.getContent() != null && !review.getContent().isBlank())
                .collect(Collectors.toMap(TicketReview::getId, Function.identity()));
        return ranked.stream()
                .filter(scored -> reviews.containsKey(scored.reviewId()))
                .limit(limit)
                .map(scored -> new ReviewAnalysisResult.Evidence(
                        scored.reviewId(),
                        reviews.get(scored.reviewId()).getContent().strip(),
                        scored.similarity()
                ))
                .toList();
    }

    private double dot(float[] left, float[] right) {
        double sum = 0.0;
        for (int i = 0; i < left.length; i++) {
            sum += left[i] * right[i];
        }
        return sum;
    }

    private record Scored(
            Long reviewId,
            double similarity
    ) {
    }
}
