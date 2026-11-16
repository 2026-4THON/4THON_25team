package com.fourthon.backend.recommendation;

import com.fourthon.backend.ai.client.LlmClient;
import com.fourthon.backend.ai.dto.ReviewAnalysisResult;
import com.fourthon.backend.ai.prompt.PromptLoader;
import com.fourthon.backend.recommendation.config.RecommendationProperties;
import com.fourthon.backend.recommendation.domain.GeneratedReason;
import com.fourthon.backend.recommendation.domain.ReasonTarget;
import com.fourthon.backend.recommendation.service.AiReasonGenerator;
import com.fourthon.backend.recommendation.service.ReasonGenerator;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

class AiReasonGeneratorTest {

    private final LlmClient llmClient = mock(LlmClient.class);
    private final RecommendationProperties properties = new RecommendationProperties(
            new RecommendationProperties.Home(5, 3, 0.3,
                    new RecommendationProperties.HomeWeights(0.45, 0.30, 0.15, 0.10)),
            9,
            24,
            new RecommendationProperties.Reason(3, 300, 40, 4000),
            new RecommendationProperties.Explore(4, 50, 0.3, 10, 300,
                    new RecommendationProperties.ExploreWeights(0.35, 0.25, 0.25, 0.10, 0.05))
    );
    private final AiReasonGenerator generator = new AiReasonGenerator(
            llmClient,
            new PromptLoader(),
            JsonMapper.builder().build(),
            new ReasonGenerator(properties),
            properties
    );
    private final List<ReasonTarget> targets = List.of(
            new ReasonTarget(1L, "빨래", "로맨스를 좋아하는 당신에게",
                    List.of(new ReviewAnalysisResult.Evidence(10L, "설레는 이야기", 0.9))),
            new ReasonTarget(2L, "쉬어 매드니스", "코미디를 좋아하는 당신에게", List.of())
    );

    @Test
    void 검증을_통과한_추천_이유만_돌려준다() {
        given(llmClient.generateJson(anyString())).willReturn("""
                {"reasons": [
                  {"performanceId": 1, "reason": "설렘을 좋아하는 당신에게", "citationIds": [10]},
                  {"performanceId": 2, "reason": "웃음이 필요한 당신에게", "citationIds": []},
                  {"performanceId": 3, "reason": "대상이 아닌 공연", "citationIds": []}
                ]}
                """);

        List<GeneratedReason> reasons = generator.generate("로맨스를 선호해요", targets);

        assertThat(reasons).extracting(GeneratedReason::performanceId).containsExactly(1L, 2L);
        assertThat(reasons.getFirst().citationIds()).containsExactly(10L);
    }

    @Test
    void 넣지_않은_후기를_인용하거나_24자를_넘으면_버린다() {
        given(llmClient.generateJson(anyString())).willReturn("""
                {"reasons": [
                  {"performanceId": 1, "reason": "설렘을 좋아하는 당신에게", "citationIds": [99]},
                  {"performanceId": 2, "reason": "웃음과 눈물이 함께하는 따뜻한 무대를 좋아하는 당신에게", "citationIds": []}
                ]}
                """);

        assertThat(generator.generate("로맨스를 선호해요", targets)).isEmpty();
    }
}
