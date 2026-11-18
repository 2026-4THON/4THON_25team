package com.fourthon.backend.ai.service;

import com.fourthon.backend.ai.client.AiException;
import com.fourthon.backend.ai.client.LlmClient;
import com.fourthon.backend.ai.prompt.PromptLoader;
import com.fourthon.backend.common.domain.TagScores;
import com.fourthon.backend.common.domain.TasteTag;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ReviewTagAnalyzer {

    private static final String PROMPT_NAME = "review-tag-analysis";

    private final LlmClient llmClient;
    private final PromptLoader promptLoader;
    private final ObjectMapper objectMapper;

    public Map<TasteTag, Double> analyze(String title, String content) {
        String json = llmClient.generateJson(promptLoader.render(PROMPT_NAME, Map.of(
                "title", title,
                "content", content.strip(),
                "tagList", Arrays.stream(TasteTag.values())
                        .map(tag -> tag.name() + ": " + tag.getLabel())
                        .collect(Collectors.joining("\n"))
        )));
        LlmResult result = read(json);
        if (result.scores() == null || result.scores().isEmpty()) {
            throw new AiException("후기 태그 점수 응답이 비어 있습니다");
        }
        return TagScores.fromCodes(result.scores());
    }

    private LlmResult read(String json) {
        try {
            return objectMapper.readValue(json, LlmResult.class);
        } catch (JacksonException e) {
            throw new AiException("후기 태그 점수 응답을 읽을 수 없습니다", e);
        }
    }

    record LlmResult(
            Map<String, Double> scores
    ) {
    }
}
