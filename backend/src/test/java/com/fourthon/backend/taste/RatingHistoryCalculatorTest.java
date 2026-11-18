package com.fourthon.backend.taste;

import com.fourthon.backend.common.domain.TasteTag;
import com.fourthon.backend.taste.domain.RatingBaseline;
import com.fourthon.backend.taste.domain.TicketRating;
import com.fourthon.backend.taste.service.RatingHistoryCalculator;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class RatingHistoryCalculatorTest {

    private final RatingHistoryCalculator calculator = new RatingHistoryCalculator();

    @Test
    void 티켓이_3개_미만이면_기준_별점은_3점이다() {
        RatingBaseline baseline = RatingBaseline.of(List.of(4.0, 5.0));

        assertThat(baseline.isHigh(3.5)).isTrue();
        assertThat(baseline.isHigh(3.0)).isFalse();
    }

    @Test
    void 티켓이_3개_이상이면_내_평균보다_높아야_높게_평가한_것이다() {
        RatingBaseline baseline = RatingBaseline.of(List.of(4.0, 4.5, 5.0));

        assertThat(baseline.isHigh(5.0)).isTrue();
        assertThat(baseline.isHigh(4.5)).isFalse();
        assertThat(baseline.deviation(4.0)).isEqualTo(-0.25);
    }

    @Test
    void 모든_별점이_같으면_4점_이상일_때만_높게_평가한_것이다() {
        assertThat(RatingBaseline.of(List.of(4.0, 4.0, 4.0)).isHigh(4.0)).isTrue();
        assertThat(RatingBaseline.of(List.of(3.5, 3.5, 3.5)).isHigh(3.5)).isFalse();
    }

    @Test
    void 높게_준_공연의_태그는_올리고_낮게_준_공연의_태그는_내린다() {
        List<TicketRating> ratings = List.of(
                new TicketRating(1L, 10L, 5.0),
                new TicketRating(1L, 20L, 1.0)
        );
        Map<Long, Map<TasteTag, Double>> showScores = Map.of(
                10L, Map.of(TasteTag.ROMANCE, 0.8, TasteTag.HORROR, 0.0),
                20L, Map.of(TasteTag.ROMANCE, 0.0, TasteTag.HORROR, 0.9)
        );

        Map<TasteTag, Double> scores = calculator.historyScores(ratings, showScores);

        assertThat(scores.get(TasteTag.ROMANCE)).isCloseTo(0.4, within(1e-9));
        assertThat(scores.get(TasteTag.HORROR)).isEqualTo(0.0);
        assertThat(scores).hasSize(TasteTag.values().length);
    }

    @Test
    void 반영할_공연_점수가_없으면_별점_기록도_없다() {
        List<TicketRating> ratings = List.of(new TicketRating(1L, 10L, 5.0));

        assertThat(calculator.historyScores(ratings, Map.of())).isEmpty();
    }

    @Test
    void 선례_점수는_같은_인생작_사용자_중_높게_평가한_비율이다() {
        List<TicketRating> peerRatings = List.of(
                new TicketRating(2L, 10L, 5.0),
                new TicketRating(2L, 30L, 3.0),
                new TicketRating(2L, 40L, 3.0),
                new TicketRating(3L, 10L, 2.0),
                new TicketRating(4L, 20L, 4.5)
        );

        Map<Long, Double> scores = calculator.precedentScores(4, peerRatings, Set.of(10L, 20L));

        assertThat(scores.get(10L)).isEqualTo(0.25);
        assertThat(scores.get(20L)).isEqualTo(0.25);
        assertThat(scores).doesNotContainKey(30L);
    }
}
