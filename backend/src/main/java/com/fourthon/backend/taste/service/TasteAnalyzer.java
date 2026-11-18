package com.fourthon.backend.taste.service;

import com.fourthon.backend.ai.client.LlmClient;
import com.fourthon.backend.ai.prompt.PromptLoader;
import com.fourthon.backend.common.domain.TagType;
import com.fourthon.backend.common.domain.TasteTag;
import com.fourthon.backend.taste.domain.LifeWork;
import com.fourthon.backend.taste.domain.TasteAnalysis;
import com.fourthon.backend.taste.domain.TasteAnswers;
import com.fourthon.backend.taste.domain.VsChoice;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class TasteAnalyzer {

    private static final String PROMPT_NAME = "taste-analysis";
    private static final int MAX_BRIEFING_LENGTH = 60;
    private static final int FALLBACK_TAG_COUNT = 2;
    private static final String PREFERENCE_SUFFIX = " 작품을 선호해요";
    private static final String DEFAULT_BRIEFING = "다양한 작품을 두루 즐기는 편이에요";

    private final LlmClient llmClient;
    private final PromptLoader promptLoader;
    private final ObjectMapper objectMapper;

    public TasteAnalysis analyze(TasteAnswers answers) {
        try {
            String json = llmClient.generateJson(promptLoader.render(PROMPT_NAME, variables(answers)));
            LlmResult result = objectMapper.readValue(json, LlmResult.class);
            return new TasteAnalysis(briefingOrFallback(result.briefing(), answers), toScores(result.scores()));
        } catch (RuntimeException e) {
            log.warn("AI 취향 분석에 실패해 기본 브리핑을 사용합니다", e);
            return new TasteAnalysis(fallbackBriefing(answers), new EnumMap<>(TasteTag.class));
        }
    }

    public String fallbackBriefing(TasteAnswers answers) {
        List<String> labels = answers.selectedTags().stream()
                .sorted(Comparator.comparing((TasteTag tag) -> tag.getType() != TagType.GENRE)
                        .thenComparing(Enum::ordinal))
                .limit(FALLBACK_TAG_COUNT)
                .map(TasteTag::getLabel)
                .toList();
        if (labels.isEmpty()) {
            return DEFAULT_BRIEFING;
        }
        return String.join("·", labels) + PREFERENCE_SUFFIX;
    }

    private Map<String, String> variables(TasteAnswers answers) {
        return Map.of(
                "selectedTags", answers.selectedTags().stream()
                        .map(TasteTag::getLabel)
                        .collect(Collectors.joining(", ")),
                "lifeWorks", answers.lifeWorks().stream()
                        .map(this::describe)
                        .collect(Collectors.joining(", ")),
                "vsChoices", answers.vsChoices().stream()
                        .map(VsChoice::getChoice)
                        .collect(Collectors.joining(", ")),
                "freeText", answers.freeText(),
                "tagList", Arrays.stream(TasteTag.values())
                        .map(tag -> tag.name() + ": " + tag.getLabel())
                        .collect(Collectors.joining("\n"))
        );
    }

    private String describe(LifeWork lifeWork) {
        return "[" + lifeWork.getCategory().getLabel() + "] " + lifeWork.getTitle();
    }

    private String briefingOrFallback(String briefing, TasteAnswers answers) {
        if (briefing == null || briefing.isBlank() || briefing.length() > MAX_BRIEFING_LENGTH) {
            return fallbackBriefing(answers);
        }
        return briefing.trim();
    }

    private Map<TasteTag, Double> toScores(Map<String, Double> rawScores) {
        Map<TasteTag, Double> scores = new EnumMap<>(TasteTag.class);
        if (rawScores == null) {
            return scores;
        }
        rawScores.forEach((code, value) -> TasteTag.fromCode(code)
                .filter(tag -> value != null)
                .ifPresent(tag -> scores.put(tag, Math.max(0.0, Math.min(1.0, value)))));
        return scores;
    }

    record LlmResult(
            String briefing,
            Map<String, Double> scores
    ) {
    }
}
