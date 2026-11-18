package com.fourthon.backend.recommendation;

import com.fourthon.backend.ai.client.LlmClient;
import com.fourthon.backend.ai.prompt.PromptLoader;
import com.fourthon.backend.common.domain.TagType;
import com.fourthon.backend.common.domain.TasteTag;
import com.fourthon.backend.recommendation.config.RecommendationProperties;
import com.fourthon.backend.recommendation.domain.WishJudgement;
import com.fourthon.backend.recommendation.score.TasteMatch;
import com.fourthon.backend.recommendation.service.WishJudge;
import com.fourthon.backend.show.dto.ShowQuery;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

class ExploreScoreTest {

    private final RecommendationProperties properties = new RecommendationProperties(
            new RecommendationProperties.Home(5, 3, 0.3,
                    new RecommendationProperties.HomeWeights(0.45, 0.30, 0.15, 0.10)),
            9,
            24,
            new RecommendationProperties.Reason(3, 300, 40, 4000),
            new RecommendationProperties.Explore(4, 50, 0.3, 10, 300,
                    new RecommendationProperties.ExploreWeights(0.35, 0.25, 0.25, 0.10, 0.05)),
            new RecommendationProperties.Pair(25, 5, 5, 3, 300, 60)
    );
    private final LlmClient llmClient = mock(LlmClient.class);
    private final WishJudge wishJudge = new WishJudge(
            llmClient, new PromptLoader(), JsonMapper.builder().build(), properties);

    @Test
    void 새로_설정_모드는_고른_장르_중_최고_점수와_고른_분위기_평균을_쓴다() {
        Map<TasteTag, Double> show = Map.of(
                TasteTag.ROMANCE, 0.2, TasteTag.COMEDY, 0.9,
                TasteTag.BRIGHT, 0.8, TasteTag.WARM, 0.4
        );
        Set<TasteTag> selected = Set.of(TasteTag.ROMANCE, TasteTag.COMEDY, TasteTag.BRIGHT, TasteTag.WARM);

        assertThat(TasteMatch.selectionMatch(selected, show, TagType.GENRE)).isEqualTo(0.9);
        assertThat(TasteMatch.selectionMatch(selected, show, TagType.MOOD)).isCloseTo(0.6, within(1e-9));
    }

    @Test
    void 한_종류를_고르지_않으면_그_종류는_중간값이다() {
        assertThat(TasteMatch.selectionMatch(Set.of(TasteTag.BRIGHT), Map.of(), TagType.GENRE)).isEqualTo(0.5);
    }

    @Test
    void 희망사항_점수는_후보에_있는_공연만_받는다() {
        given(llmClient.isConfigured()).willReturn(true);
        given(llmClient.generateJson(anyString())).willReturn("""
                {"summary": "웃음이 가득한 작품을 추천해드려요", "scores": {"1": 0.8, "2": 1.5, "999": 1.0, "abc": 0.3}}
                """);

        WishJudgement judgement = wishJudge.judge("친구", "웃긴 거", Set.of(TasteTag.COMEDY), true,
                List.of(show(1L), show(2L)));

        assertThat(judgement.scores()).containsOnlyKeys(1L, 2L);
        assertThat(judgement.scores()).containsEntry(2L, 1.0);
        assertThat(judgement.summary()).contains("웃음이 가득한 작품을 추천해드려요");
    }

    @Test
    void 응답을_읽지_못하면_희망사항_없이_계산한다() {
        given(llmClient.isConfigured()).willReturn(true);
        given(llmClient.generateJson(anyString())).willReturn("잘 모르겠어요");

        assertThat(wishJudge.judge("혼자", "슬픈 건 싫어", Set.of(), false, List.of(show(1L))).hasScores()).isFalse();
    }

    private ShowQuery.ExploreShow show(Long id) {
        return new ShowQuery.ExploreShow(
                new ShowQuery.Candidate(id, "공연" + id, "PLAY", null,
                        LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31), Map.of()),
                30000,
                List.of(LocalDate.of(2026, 11, 20)),
                "줄거리"
        );
    }
}
