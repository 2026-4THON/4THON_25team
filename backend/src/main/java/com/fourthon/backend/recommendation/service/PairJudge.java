package com.fourthon.backend.recommendation.service;

import com.fourthon.backend.ai.client.AiException;
import com.fourthon.backend.ai.client.LlmClient;
import com.fourthon.backend.ai.prompt.PromptLoader;
import com.fourthon.backend.recommendation.config.RecommendationProperties;
import com.fourthon.backend.recommendation.domain.PairJudgement;
import com.fourthon.backend.recommendation.domain.PairReview;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class PairJudge {

    private static final String PROMPT_NAME = "pair-recommendation";
    private static final String OK = "OK";
    private static final String NONE = "없음";
    private static final String ELLIPSIS = "…";

    private final LlmClient llmClient;
    private final PromptLoader promptLoader;
    private final ObjectMapper objectMapper;
    private final RecommendationProperties recommendationProperties;

    public boolean isAvailable() {
        return llmClient.isConfigured();
    }

    public PairJudgement judge(String briefing, List<String> myReviews, List<PairReview> evidence) {
        String json = llmClient.generateJson(promptLoader.render(PROMPT_NAME, Map.of(
                "briefing", briefing == null || briefing.isBlank() ? NONE : briefing,
                "myReviews", myReviews.isEmpty() ? NONE : myReviews.stream()
                        .map(review -> "- " + truncate(review))
                        .collect(Collectors.joining("\n")),
                "pairReviews", evidence.stream().map(this::describe).collect(Collectors.joining("\n"))
        )));
        LlmResult result = read(json);
        Set<Long> allowed = evidence.stream().map(PairReview::reviewId).collect(Collectors.toSet());
        List<Long> citations = result.citationIds() == null ? List.of() : result.citationIds().stream()
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        boolean valid = !citations.isEmpty() && allowed.containsAll(citations);
        if (!OK.equals(result.status()) || !valid) {
            return PairJudgement.insufficient();
        }
        return new PairJudgement(true, reasonOrNull(result.reason()), citations);
    }

    private String reasonOrNull(String reason) {
        if (reason == null || reason.isBlank()
                || reason.strip().length() > recommendationProperties.pair().reasonMaxLength()) {
            return null;
        }
        return reason.strip();
    }

    private String describe(PairReview review) {
        return "- (후기 ID " + review.reviewId() + ") [" + String.join("·", review.actorNames()) + "] "
                + truncate(review.content());
    }

    private String truncate(String content) {
        int maxLength = recommendationProperties.pair().reviewMaxLength();
        String singleLine = content.replaceAll("\\s+", " ").strip();
        return singleLine.length() <= maxLength ? singleLine : singleLine.substring(0, maxLength) + ELLIPSIS;
    }

    private LlmResult read(String json) {
        try {
            return objectMapper.readValue(json, LlmResult.class);
        } catch (JacksonException e) {
            throw new AiException("페어 추천 응답을 읽을 수 없습니다", e);
        }
    }

    record LlmResult(
            String status,
            String reason,
            List<Long> citationIds
    ) {
    }
}
