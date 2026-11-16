package com.fourthon.backend.show;

import com.fourthon.backend.ai.client.AiException;
import com.fourthon.backend.ai.client.LlmClient;
import com.fourthon.backend.ai.prompt.PromptLoader;
import com.fourthon.backend.common.domain.TasteTag;
import com.fourthon.backend.show.domain.ShowAnalysis;
import com.fourthon.backend.show.service.ShowAnalyzer;
import com.fourthon.backend.ticket.domain.CatalogPerformance;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

class ShowAnalyzerTest {

    private final LlmClient llmClient = mock(LlmClient.class);
    private final ShowAnalyzer analyzer = new ShowAnalyzer(
            llmClient,
            new PromptLoader(),
            JsonMapper.builder().build()
    );

    @Test
    void AI_응답에서_키워드와_태그_점수를_읽는다() {
        given(llmClient.generateJson(anyString())).willReturn("""
                {"keywords": ["#가족애", "1930년대 경성", "가족애", "아주아주아주아주아주아주긴키워드", "반전"],
                 "scores": {"DRAMA": 0.9, "TRAGEDY": 1.3, "UNKNOWN": 0.5}}
                """);

        ShowAnalysis analysis = analyzer.analyze("제목", "연극", "줄거리");

        assertThat(analysis.keywords()).containsExactly("가족애", "1930년대 경성", "반전");
        assertThat(analysis.tagScores()).hasSize(TasteTag.values().length);
        assertThat(analysis.tagScores()).containsEntry(TasteTag.DRAMA, 0.9);
        assertThat(analysis.tagScores()).containsEntry(TasteTag.TRAGEDY, 1.0);
        assertThat(analysis.tagScores()).containsEntry(TasteTag.ROMANCE, 0.0);
    }

    @Test
    void 키워드나_점수가_없으면_실패로_본다() {
        given(llmClient.generateJson(anyString())).willReturn("""
                {"keywords": [], "scores": {"DRAMA": 0.9}}
                """);

        assertThatThrownBy(() -> analyzer.analyze("제목", "연극", "줄거리"))
                .isInstanceOf(AiException.class);
    }

    @Test
    void 응답이_JSON이_아니면_실패로_본다() {
        given(llmClient.generateJson(anyString())).willReturn("분석할 수 없습니다");

        assertThatThrownBy(() -> analyzer.analyze("제목", "연극", "줄거리"))
                .isInstanceOf(AiException.class);
    }

    @Test
    void 줄거리가_없으면_장르_기본값을_쓴다() {
        ShowAnalysis play = analyzer.defaultAnalysis(CatalogPerformance.Genre.PLAY);
        ShowAnalysis musical = analyzer.defaultAnalysis(CatalogPerformance.Genre.MUSICAL);

        assertThat(play.keywords()).isEmpty();
        assertThat(play.tagScores()).containsEntry(TasteTag.DRAMA, 0.5);
        assertThat(musical.tagScores()).containsEntry(TasteTag.MUSIC_ART, 0.5);
        assertThat(musical.tagScores()).containsEntry(TasteTag.DRAMA, 0.0);
    }
}
