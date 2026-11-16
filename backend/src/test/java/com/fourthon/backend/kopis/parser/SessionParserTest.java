package com.fourthon.backend.kopis.parser;

import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class SessionParserTest {

    private final SessionParser sessionParser = new SessionParser();

    @Test
    void 요일_범위와_시간을_읽는다() {
        Map<DayOfWeek, List<LocalTime>> schedule = sessionParser.parseSchedule(
                "화요일 ~ 금요일(20:00), 토요일 ~ 일요일(15:00,19:00), HOL(15:00)");

        assertThat(schedule).doesNotContainKey(DayOfWeek.MONDAY);
        assertThat(schedule.get(DayOfWeek.WEDNESDAY)).containsExactly(LocalTime.of(20, 0));
        assertThat(schedule.get(DayOfWeek.SUNDAY)).containsExactly(LocalTime.of(15, 0), LocalTime.of(19, 0));
    }

    @Test
    void 일요일을_넘어가는_범위를_읽는다() {
        Map<DayOfWeek, List<LocalTime>> schedule = sessionParser.parseSchedule("금요일 ~ 월요일(19:30)");

        assertThat(schedule).containsOnlyKeys(
                DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY, DayOfWeek.MONDAY);
    }

    @Test
    void 기간_안의_회차를_만든다() {
        LocalDate monday = LocalDate.of(2026, 11, 16);

        List<LocalDateTime> sessions = sessionParser.generate(
                "화요일 ~ 금요일(20:00), 토요일 ~ 일요일(15:00,19:00)", monday, monday.plusDays(6));

        assertThat(sessions).hasSize(8);
        assertThat(sessions.getFirst()).isEqualTo(LocalDateTime.of(2026, 11, 17, 20, 0));
    }

    @Test
    void 문구를_읽지_못하면_날짜마다_00시_회차를_만든다() {
        LocalDate from = LocalDate.of(2026, 11, 16);

        List<LocalDateTime> sessions = sessionParser.generate("공연 시간 별도 안내", from, from.plusDays(2));

        assertThat(sessions).containsExactly(
                from.atStartOfDay(),
                from.plusDays(1).atStartOfDay(),
                from.plusDays(2).atStartOfDay()
        );
    }

    @Test
    void 시작일이_종료일보다_늦으면_회차가_없다() {
        LocalDate date = LocalDate.of(2026, 11, 16);

        assertThat(sessionParser.generate("월요일(19:00)", date, date.minusDays(1))).isEmpty();
    }
}
