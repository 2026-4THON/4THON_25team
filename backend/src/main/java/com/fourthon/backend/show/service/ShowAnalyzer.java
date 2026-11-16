package com.fourthon.backend.show.service;

import com.fourthon.backend.ai.client.AiException;
import com.fourthon.backend.ai.client.LlmClient;
import com.fourthon.backend.ai.prompt.PromptLoader;
import com.fourthon.backend.common.domain.TasteTag;
import com.fourthon.backend.show.domain.ShowAnalysis;
import com.fourthon.backend.ticket.domain.CatalogPerformance;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ShowAnalyzer {

    private static final String PROMPT_NAME = "show-analysis";
    private static final int KEYWORD_COUNT = 3;
    private static final int MAX_KEYWORD_LENGTH = 15;
    private static final double DEFAULT_GENRE_SCORE = 0.5;
    private static final String HASH = "#";

    private final LlmClient llmClient;
    private final PromptLoader promptLoader;
    private final ObjectMapper objectMapper;

    public boolean isAvailable() {
        return llmClient.isConfigured();
    }

    public ShowAnalysis analyze(String title, String genre, String synopsis) {
        String json = llmClient.generateJson(promptLoader.render(PROMPT_NAME, Map.of(
                "title", title,
                "genre", genre,
                "synopsis", synopsis,
                "tagList", Arrays.stream(TasteTag.values())
                        .map(tag -> tag.name() + ": " + tag.getLabel())
                        .collect(Collectors.joining("\n"))
        )));
        LlmResult result = read(json);
        List<String> keywords = toKeywords(result.keywords());
        Map<TasteTag, Double> scores = toScores(result.scores());
        if (keywords.isEmpty() || scores.values().stream().allMatch(score -> score == 0.0)) {
            throw new AiException("공연 분석 결과가 비어 있습니다");
        }
        return new ShowAnalysis(keywords, scores);
    }

    public ShowAnalysis defaultAnalysis(CatalogPerformance.Genre genre) {
        Map<TasteTag, Double> scores = emptyScores();
        if (genre == CatalogPerformance.Genre.PLAY) {
            scores.put(TasteTag.DRAMA, DEFAULT_GENRE_SCORE);
        } else if (genre == CatalogPerformance.Genre.MUSICAL) {
            scores.put(TasteTag.MUSIC_ART, DEFAULT_GENRE_SCORE);
        }
        return new ShowAnalysis(List.of(), scores);
    }

    private LlmResult read(String json) {
        try {
            return objectMapper.readValue(json, LlmResult.class);
        } catch (JacksonException e) {
            throw new AiException("공연 분석 응답을 읽을 수 없습니다", e);
        }
    }

    private List<String> toKeywords(List<String> rawKeywords) {
        if (rawKeywords == null) {
            return List.of();
        }
        return rawKeywords.stream()
                .filter(Objects::nonNull)
                .map(keyword -> keyword.replace(HASH, "").trim())
                .filter(keyword -> !keyword.isEmpty() && keyword.length() <= MAX_KEYWORD_LENGTH)
                .distinct()
                .limit(KEYWORD_COUNT)
                .toList();
    }

    private Map<TasteTag, Double> toScores(Map<String, Double> rawScores) {
        Map<TasteTag, Double> scores = emptyScores();
        if (rawScores == null) {
            return scores;
        }
        rawScores.forEach((code, value) -> TasteTag.fromCode(code)
                .filter(tag -> value != null)
                .ifPresent(tag -> scores.put(tag, Math.max(0.0, Math.min(1.0, value)))));
        return scores;
    }

    private Map<TasteTag, Double> emptyScores() {
        Map<TasteTag, Double> scores = new EnumMap<>(TasteTag.class);
        Arrays.stream(TasteTag.values()).forEach(tag -> scores.put(tag, 0.0));
        return scores;
    }

    record LlmResult(
            List<String> keywords,
            Map<String, Double> scores
    ) {
    }
}
