package com.fourthon.backend.recommendation.service;

import com.fourthon.backend.ai.client.AiException;
import com.fourthon.backend.ai.client.LlmClient;
import com.fourthon.backend.ai.dto.ReviewAnalysisResult;
import com.fourthon.backend.ai.prompt.PromptLoader;
import com.fourthon.backend.recommendation.config.RecommendationProperties;
import com.fourthon.backend.recommendation.domain.GeneratedReason;
import com.fourthon.backend.recommendation.domain.ReasonTarget;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class AiReasonGenerator {

    private static final String PROMPT_NAME = "recommendation-reason";
    private static final String ELLIPSIS = "…";

    private final LlmClient llmClient;
    private final PromptLoader promptLoader;
    private final ObjectMapper objectMapper;
    private final ReasonGenerator reasonGenerator;
    private final RecommendationProperties recommendationProperties;

    public boolean isAvailable() {
        return llmClient.isConfigured();
    }

    public List<GeneratedReason> generate(String briefing, List<ReasonTarget> targets) {
        if (targets.isEmpty()) {
            return List.of();
        }
        String json = llmClient.generateJson(promptLoader.render(PROMPT_NAME, Map.of(
                "briefing", briefing,
                "targets", targets.stream().map(this::describe).collect(Collectors.joining("\n\n"))
        )));
        return validate(read(json), targets);
    }

    private List<GeneratedReason> validate(LlmResult result, List<ReasonTarget> targets) {
        if (result == null || result.reasons() == null) {
            return List.of();
        }
        Map<Long, Set<Long>> allowedCitations = new HashMap<>();
        targets.forEach(target -> allowedCitations.put(target.performanceId(), target.reviews().stream()
                .map(ReviewAnalysisResult.Evidence::reviewId)
                .collect(Collectors.toSet())));
        Set<Long> seen = new HashSet<>();
        return result.reasons().stream()
                .filter(Objects::nonNull)
                .filter(item -> item.performanceId() != null && allowedCitations.containsKey(item.performanceId()))
                .filter(item -> seen.add(item.performanceId()))
                .filter(item -> item.reason() != null && reasonGenerator.fits(item.reason().strip()))
                .filter(item -> citationsOf(item).stream().allMatch(allowedCitations.get(item.performanceId())::contains))
                .map(item -> new GeneratedReason(item.performanceId(), item.reason().strip(), citationsOf(item)))
                .toList();
    }

    private List<Long> citationsOf(LlmItem item) {
        return item.citationIds() == null ? List.of() : item.citationIds().stream().filter(Objects::nonNull).toList();
    }

    private String describe(ReasonTarget target) {
        StringBuilder text = new StringBuilder()
                .append("[공연 ID ").append(target.performanceId()).append("] ").append(target.title()).append('\n')
                .append("[근거] ").append(target.ground());
        if (!target.reviews().isEmpty()) {
            text.append('\n').append("<후기>");
            target.reviews().forEach(review -> text.append('\n')
                    .append("- (후기 ID ").append(review.reviewId()).append(") ")
                    .append(truncate(review.content())));
            text.append('\n').append("</후기>");
        }
        return text.toString();
    }

    private String truncate(String content) {
        int maxLength = recommendationProperties.reason().evidenceMaxLength();
        String singleLine = content.replaceAll("\\s+", " ").strip();
        return singleLine.length() <= maxLength ? singleLine : singleLine.substring(0, maxLength) + ELLIPSIS;
    }

    private LlmResult read(String json) {
        try {
            return objectMapper.readValue(json, LlmResult.class);
        } catch (JacksonException e) {
            throw new AiException("추천 이유 응답을 읽을 수 없습니다", e);
        }
    }

    record LlmResult(
            List<LlmItem> reasons
    ) {
    }

    record LlmItem(
            Long performanceId,
            String reason,
            List<Long> citationIds
    ) {
    }
}
