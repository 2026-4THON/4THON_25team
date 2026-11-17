package com.fourthon.backend.show;

import com.fourthon.backend.show.config.RatingProperties;
import com.fourthon.backend.show.repository.ShowRatingRepository;
import com.fourthon.backend.show.service.RatingPrior;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

class RatingPriorTest {

    private final ShowRatingRepository showRatingRepository = mock(ShowRatingRepository.class);
    private final RatingPrior ratingPrior = new RatingPrior(showRatingRepository, new RatingProperties(3, 3.5));

    @Test
    void 평가가_적으면_전체_평균_쪽으로_당긴다() {
        given(showRatingRepository.findRatingCountsPerShow()).willReturn(List.of(1L, 1L, 2L));
        given(showRatingRepository.findAverageHalfUnits()).willReturn(7.6);
        ratingPrior.refresh();

        assertThat(ratingPrior.adjusted(1, 5.0)).isCloseTo(4.1, within(0.001));
    }

    @Test
    void 중앙값이_최솟값보다_크면_중앙값을_쓴다() {
        given(showRatingRepository.findRatingCountsPerShow()).willReturn(List.of(4L, 6L, 8L, 10L));
        given(showRatingRepository.findAverageHalfUnits()).willReturn(8.0);
        ratingPrior.refresh();

        assertThat(ratingPrior.adjusted(7, 5.0)).isCloseTo((7 * 5.0 + 7 * 4.0) / 14, within(0.001));
    }

    @Test
    void 평가가_없으면_전체_평균이다() {
        given(showRatingRepository.findRatingCountsPerShow()).willReturn(List.of());
        given(showRatingRepository.findAverageHalfUnits()).willReturn(null);
        ratingPrior.refresh();

        assertThat(ratingPrior.adjusted(0, null)).isEqualTo(3.5);
    }
}
