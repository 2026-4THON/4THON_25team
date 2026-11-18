package com.fourthon.backend.show;

import com.fourthon.backend.common.domain.TasteTag;
import com.fourthon.backend.show.domain.ShowTagScore;
import com.fourthon.backend.show.service.ShowTagScoreCalculator;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class ShowTagScoreCalculatorTest {

    private final ShowTagScoreCalculator calculator = new ShowTagScoreCalculator();

    @Test
    void 후기_가중치는_후기_수에_따라_커지고_최대_0점5다() {
        assertThat(calculator.reviewWeight(0)).isEqualTo(0.0);
        assertThat(calculator.reviewWeight(5)).isEqualTo(0.5);
        assertThat(calculator.reviewWeight(1)).isCloseTo(1.0 / 6.0, within(1e-9));
        assertThat(calculator.reviewWeight(100)).isEqualTo(0.5);
    }

    @Test
    void 최종_점수는_줄거리_점수와_후기_평균을_가중치로_섞는다() {
        ShowTagScore score = new ShowTagScore(1L, TasteTag.DRAMA, 0.8);

        score.applyReviewScore(0.2, 0.25);

        assertThat(score.getScore()).isCloseTo(0.65, within(1e-9));
        assertThat(score.getSynopsisScore()).isEqualTo(0.8);
    }
}
