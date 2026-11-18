package com.fourthon.backend.ai;

import com.fourthon.backend.ai.domain.ReviewEmbedding;
import com.fourthon.backend.ai.domain.ReviewSource;
import com.fourthon.backend.ai.service.ReviewTextBuilder;
import com.fourthon.backend.ticket.domain.TicketReview;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ReviewTextBuilderTest {

    private final ReviewTextBuilder builder = new ReviewTextBuilder();

    @Test
    void 페어_후기는_배우와_별점을_함께_넣는다() {
        ReviewSource source = new ReviewSource(
                1L, 2L, 3L, "빨래", TicketReview.ReviewType.PAIR,
                List.of("배우A", "배우B"), 4.5, "  호흡이 정말 좋았어요  "
        );

        assertThat(builder.build(source)).isEqualTo("""
                공연: 빨래
                후기 종류: 페어 후기
                배우: 배우A, 배우B
                별점: 4.5
                후기: 호흡이 정말 좋았어요""");
    }

    @Test
    void 공연_후기는_배우_줄을_넣지_않는다() {
        ReviewSource source = new ReviewSource(
                1L, 2L, 3L, "빨래", TicketReview.ReviewType.PERFORMANCE,
                List.of(), 3.0, "따뜻했어요"
        );

        assertThat(builder.build(source)).doesNotContain("배우:").contains("후기 종류: 공연 후기");
    }

    @Test
    void 임베딩_벡터를_저장했다가_그대로_읽는다() {
        ReviewSource source = new ReviewSource(
                1L, 2L, 3L, "빨래", TicketReview.ReviewType.ACTOR,
                List.of("배우A"), 5.0, "좋아요"
        );
        float[] values = {0.6f, -0.8f, 0.0f};

        ReviewEmbedding embedding = new ReviewEmbedding(source, "gemini-embedding-2", values, LocalDateTime.now());

        assertThat(embedding.values()).containsExactly(values);
        assertThat(embedding.getDimension()).isEqualTo(3);
    }
}
