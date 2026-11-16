package com.fourthon.backend.recommendation;

import com.fourthon.backend.ai.client.LlmClient;
import com.fourthon.backend.ai.prompt.PromptLoader;
import com.fourthon.backend.recommendation.config.RecommendationProperties;
import com.fourthon.backend.recommendation.domain.PairJudgement;
import com.fourthon.backend.recommendation.domain.PairReview;
import com.fourthon.backend.recommendation.service.PairJudge;
import com.fourthon.backend.recommendation.service.PairSelector;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

class PairRecommendationTest {

    private final RecommendationProperties properties = new RecommendationProperties(
            new RecommendationProperties.Home(5, 3, 0.3,
                    new RecommendationProperties.HomeWeights(0.45, 0.30, 0.15, 0.10)),
            9,
            24,
            new RecommendationProperties.Reason(3, 300, 40, 4000),
            new RecommendationProperties.Explore(4, 50, 0.3, 10, 300,
                    new RecommendationProperties.ExploreWeights(0.35, 0.25, 0.25, 0.10, 0.05)),
            new RecommendationProperties.Pair(4, 2, 2, 3, 300, 60)
    );
    private final PairSelector selector = new PairSelector(properties);
    private final LlmClient llmClient = mock(LlmClient.class);
    private final PairJudge judge = new PairJudge(llmClient, new PromptLoader(), JsonMapper.builder().build(), properties);

    @Test
    void 후보가_적으면_전부_근거로_쓴다() {
        List<PairReview> reviews = List.of(review(1L, 10L, 20L), review(2L, 10L, 30L));

        assertThat(selector.selectEvidence(reviews, Map.of(), null)).hasSize(2);
    }

    @Test
    void 후보가_많으면_유사도가_높은_조합과_후기만_고른다() {
        List<PairReview> reviews = new ArrayList<>();
        Map<Long, float[]> vectors = new HashMap<>();
        addReview(reviews, vectors, 1L, new long[]{10, 20}, 0.9f);
        addReview(reviews, vectors, 2L, new long[]{20, 10}, 0.8f);
        addReview(reviews, vectors, 3L, new long[]{10, 20}, 0.7f);
        addReview(reviews, vectors, 4L, new long[]{10, 30}, 0.6f);
        addReview(reviews, vectors, 5L, new long[]{10, 30}, 0.5f);
        addReview(reviews, vectors, 6L, new long[]{40, 50}, 0.1f);

        List<Long> picked = selector.selectEvidence(reviews, vectors, new float[]{1.0f}).stream()
                .map(PairReview::reviewId)
                .toList();

        assertThat(picked).containsExactly(1L, 2L, 4L, 5L);
    }

    @Test
    void 인용한_후기가_가장_많이_가리키는_조합을_추천한다() {
        List<PairReview> reviews = List.of(
                review(1L, 10L, 20L), review(2L, 20L, 10L), review(3L, 10L, 30L)
        );

        assertThat(selector.comboOf(List.of(1L, 2L, 3L), reviews)).contains(List.of(10L, 20L));
        assertThat(selector.mostReviewedCombo(reviews)).contains(List.of(10L, 20L));
    }

    @Test
    void 후보에_없는_후기를_인용하면_근거_부족으로_본다() {
        given(llmClient.generateJson(anyString())).willReturn("""
                {"status": "OK", "reason": "호흡이 좋다는 후기가 많아요", "citationIds": [1, 99]}
                """);

        PairJudgement judgement = judge.judge("로맨스를 선호해요", List.of(), List.of(review(1L, 10L, 20L)));

        assertThat(judgement.sufficient()).isFalse();
    }

    @Test
    void 인용이_올바르면_추천_이유와_함께_돌려준다() {
        given(llmClient.generateJson(anyString())).willReturn("""
                {"status": "OK", "reason": "호흡이 좋다는 후기가 많아요", "citationIds": [1]}
                """);

        PairJudgement judgement = judge.judge(null, List.of("좋았어요"), List.of(review(1L, 10L, 20L)));

        assertThat(judgement.sufficient()).isTrue();
        assertThat(judgement.citationIds()).containsExactly(1L);
        assertThat(judgement.reason()).isEqualTo("호흡이 좋다는 후기가 많아요");
    }

    private void addReview(List<PairReview> reviews, Map<Long, float[]> vectors, Long id, long[] actors, float similarity) {
        reviews.add(review(id, actors[0], actors[1]));
        vectors.put(id, new float[]{similarity});
    }

    private PairReview review(Long id, Long firstActor, Long secondActor) {
        List<Long> actorIds = List.of(firstActor, secondActor).stream().sorted().toList();
        return new PairReview(id, actorIds, List.of("배우" + firstActor, "배우" + secondActor), "후기" + id);
    }
}
