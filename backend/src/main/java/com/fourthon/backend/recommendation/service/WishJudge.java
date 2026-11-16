package com.fourthon.backend.recommendation.service;

import com.fourthon.backend.ai.client.LlmClient;
import com.fourthon.backend.ai.prompt.PromptLoader;
import com.fourthon.backend.common.domain.TagScores;
import com.fourthon.backend.common.domain.TasteTag;
import com.fourthon.backend.recommendation.config.RecommendationProperties;
import com.fourthon.backend.recommendation.domain.WishJudgement;
import com.fourthon.backend.show.dto.ShowQuery;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class WishJudge {

    private static final String PROMPT_NAME = "explore-wish";
    private static final int MAX_SUMMARY_LENGTH = 60;
    private static final String NONE = "없음";
    private static final String SUMMARY_INSTRUCTION =
            "고른 장르·분위기와 희망사항을 바탕으로 이 사람이 찾는 공연을 \"~작품을 추천해드려요\"로 끝나는 해요체 한 문장, 공백 포함 40자 이하로 쓴다.";
    private static final String NO_SUMMARY_INSTRUCTION = "빈 문자열로 둔다.";
    private static final String ELLIPSIS = "…";

    private final LlmClient llmClient;
    private final PromptLoader promptLoader;
    private final ObjectMapper objectMapper;
    private final RecommendationProperties recommendationProperties;

    public boolean isAvailable() {
        return llmClient.isConfigured();
    }

    public WishJudgement judge(
            String companion,
            String wish,
            Set<TasteTag> selectedTags,
            boolean needSummary,
            List<ShowQuery.ExploreShow> shows) {

        if (shows.isEmpty() || !isAvailable()) {
            return WishJudgement.empty();
        }
        try {
            String json = llmClient.generateJson(promptLoader.render(PROMPT_NAME, Map.of(
                    "companion", companion,
                    "selectedTags", selectedTags.isEmpty() ? NONE : selectedTags.stream()
                            .map(TasteTag::getLabel)
                            .collect(Collectors.joining(", ")),
                    "wish", wish == null || wish.isBlank() ? "" : wish.strip(),
                    "candidates", shows.stream().map(this::describe).collect(Collectors.joining("\n")),
                    "summaryInstruction", needSummary ? SUMMARY_INSTRUCTION : NO_SUMMARY_INSTRUCTION
            )));
            LlmResult result = objectMapper.readValue(json, LlmResult.class);
            return new WishJudgement(
                    toScores(result.scores(), shows.stream().map(show -> show.candidate().performanceId()).toList()),
                    toSummary(result.summary(), needSummary)
            );
        } catch (RuntimeException e) {
            log.warn("탐색 희망사항 판단에 실패해 희망사항 없이 계산합니다", e);
            return WishJudgement.empty();
        }
    }

    private Map<Long, Double> toScores(Map<String, Double> rawScores, Collection<Long> candidateIds) {
        Map<Long, Double> scores = new HashMap<>();
        if (rawScores == null) {
            return scores;
        }
        rawScores.forEach((key, value) -> parseId(key)
                .filter(candidateIds::contains)
                .filter(id -> value != null)
                .ifPresent(id -> scores.put(id, TagScores.clamp(value))));
        return scores;
    }

    private Optional<String> toSummary(String summary, boolean needSummary) {
        if (!needSummary || summary == null || summary.isBlank() || summary.strip().length() > MAX_SUMMARY_LENGTH) {
            return Optional.empty();
        }
        return Optional.of(summary.strip());
    }

    private Optional<Long> parseId(String key) {
        try {
            return Optional.of(Long.valueOf(key.strip()));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    private String describe(ShowQuery.ExploreShow show) {
        String synopsis = show.synopsis() == null ? "" : show.synopsis().replaceAll("\\s+", " ").strip();
        int maxLength = recommendationProperties.explore().synopsisMaxLength();
        if (synopsis.length() > maxLength) {
            synopsis = synopsis.substring(0, maxLength) + ELLIPSIS;
        }
        StringBuilder text = new StringBuilder("- [")
                .append(show.candidate().performanceId()).append("] ")
                .append(show.candidate().title())
                .append(" (").append(show.candidate().genre()).append(')');
        if (!synopsis.isEmpty()) {
            text.append(": ").append(synopsis);
        }
        return text.toString();
    }

    record LlmResult(
            String summary,
            Map<String, Double> scores
    ) {
    }
}
