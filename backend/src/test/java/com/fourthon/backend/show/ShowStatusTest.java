package com.fourthon.backend.show;

import com.fourthon.backend.show.domain.ShowStatus;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class ShowStatusTest {

    private final LocalDate start = LocalDate.of(2026, 11, 1);
    private final LocalDate end = LocalDate.of(2026, 11, 30);

    @Test
    void 시작일_전이면_예정이다() {
        assertThat(ShowStatus.of(start, end, start.minusDays(1))).isEqualTo(ShowStatus.UPCOMING);
    }

    @Test
    void 기간_안이면_진행중이다() {
        assertThat(ShowStatus.of(start, end, start)).isEqualTo(ShowStatus.ONGOING);
        assertThat(ShowStatus.of(start, end, end)).isEqualTo(ShowStatus.ONGOING);
    }

    @Test
    void 종료일_다음날부터_완료다() {
        assertThat(ShowStatus.of(start, end, end.plusDays(1))).isEqualTo(ShowStatus.ENDED);
    }
}
