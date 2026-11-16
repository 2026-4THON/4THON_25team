package com.fourthon.backend.taste;

import com.fourthon.backend.ai.client.AiException;
import com.fourthon.backend.ai.client.LlmClient;
import com.fourthon.backend.ai.prompt.PromptLoader;
import com.fourthon.backend.common.domain.TasteTag;
import com.fourthon.backend.taste.domain.LifeWork;
import com.fourthon.backend.taste.domain.LifeWorkCategory;
import com.fourthon.backend.taste.domain.TasteAnalysis;
import com.fourthon.backend.taste.domain.TasteAnswers;
import com.fourthon.backend.taste.domain.VsChoice;
import com.fourthon.backend.taste.service.TasteAnalyzer;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

class TasteAnalyzerTest {

    private final LlmClient llmClient = mock(LlmClient.class);
    private final TasteAnalyzer analyzer = new TasteAnalyzer(
            llmClient,
            new PromptLoader(),
            JsonMapper.builder().build()
    );
    private final TasteAnswers answers = new TasteAnswers(
            Set.of(TasteTag.ROMANCE, TasteTag.BRIGHT),
            List.of(new LifeWork(LifeWorkCategory.BOOK, "폭풍의 언덕")),
            List.of(new VsChoice("3-1-1", "열정적이고 감정적인")),
            "웃다가 울 수 있는 극이 좋아요"
    );

    @Test
    void AI_응답에서_브리핑과_점수를_읽는다() {
        given(llmClient.generateJson(anyString())).willReturn("""
                {"briefing": "격정적인 사랑과 여운을 좋아하는 작품을 선호해요",
                 "scores": {"ROMANCE": 0.9, "TRAGEDY": 1.4, "UNKNOWN": 0.5}}
                """);

        TasteAnalysis analysis = analyzer.analyze(answers);

        assertThat(analysis.briefing()).isEqualTo("격정적인 사랑과 여운을 좋아하는 작품을 선호해요");
        assertThat(analysis.aiScores()).containsEntry(TasteTag.ROMANCE, 0.9);
        assertThat(analysis.aiScores()).containsEntry(TasteTag.TRAGEDY, 1.0);
        assertThat(analysis.aiScores()).hasSize(2);
    }

    @Test
    void AI_호출이_실패하면_고른_태그로_브리핑을_만든다() {
        given(llmClient.generateJson(anyString())).willThrow(new AiException("실패"));

        TasteAnalysis analysis = analyzer.analyze(answers);

        assertThat(analysis.briefing()).isEqualTo("로맨스·밝고 유쾌한 작품을 선호해요");
        assertThat(analysis.aiScores()).isEmpty();
    }

    @Test
    void 인생작_제목은_공백과_문장부호를_빼고_비교한다() {
        assertThat(LifeWork.normalize("폭풍의 언덕!")).isEqualTo(LifeWork.normalize("폭풍의언덕"));
    }
}
