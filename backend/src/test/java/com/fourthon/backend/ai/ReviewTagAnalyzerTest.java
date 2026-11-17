package com.fourthon.backend.ai;

import com.fourthon.backend.ai.client.AiException;
import com.fourthon.backend.ai.client.LlmClient;
import com.fourthon.backend.ai.prompt.PromptLoader;
import com.fourthon.backend.ai.service.ReviewTagAnalyzer;
import com.fourthon.backend.common.domain.TasteTag;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

class ReviewTagAnalyzerTest {

    private final LlmClient llmClient = mock(LlmClient.class);
    private final ReviewTagAnalyzer analyzer = new ReviewTagAnalyzer(
            llmClient,
            new PromptLoader(),
            JsonMapper.builder().build()
    );

    @Test
    void 후기_태그_점수를_22개_모두_채워_읽는다() {
        given(llmClient.generateJson(anyString())).willReturn("""
                {"scores": {"WARM": 0.9, "SAD": -0.2, "UNKNOWN": 1.0}}
                """);

        Map<TasteTag, Double> scores = analyzer.analyze("빨래", "따뜻하고 뭉클했어요");

        assertThat(scores).hasSize(TasteTag.values().length);
        assertThat(scores).containsEntry(TasteTag.WARM, 0.9);
        assertThat(scores).containsEntry(TasteTag.SAD, 0.0);
    }

    @Test
    void 점수가_없으면_실패로_본다() {
        given(llmClient.generateJson(anyString())).willReturn("{\"scores\": {}}");

        assertThatThrownBy(() -> analyzer.analyze("빨래", "좋았어요"))
                .isInstanceOf(AiException.class);
    }
}
