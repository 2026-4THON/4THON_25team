package com.fourthon.backend.kopis.parser;

import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class SessionParser {

    private static final Pattern SEGMENT = Pattern.compile("([^(),]+?)\\s*\\(([^)]*)\\)");
    private static final Pattern TIME = Pattern.compile("(\\d{1,2}):(\\d{2})");
    private static final String RANGE = "~";
    private static final String DAY_SUFFIX = "요일";
    private static final Map<String, DayOfWeek> DAYS = Map.of(
            "월", DayOfWeek.MONDAY,
            "화", DayOfWeek.TUESDAY,
            "수", DayOfWeek.WEDNESDAY,
            "목", DayOfWeek.THURSDAY,
            "금", DayOfWeek.FRIDAY,
            "토", DayOfWeek.SATURDAY,
            "일", DayOfWeek.SUNDAY
    );

    public Map<DayOfWeek, List<LocalTime>> parseSchedule(String guide) {
        Map<DayOfWeek, TreeSet<LocalTime>> schedule = new EnumMap<>(DayOfWeek.class);
        if (guide == null || guide.isBlank()) {
            return Map.of();
        }
        Matcher segment = SEGMENT.matcher(guide);
        while (segment.find()) {
            List<DayOfWeek> days = parseDays(segment.group(1));
            List<LocalTime> times = parseTimes(segment.group(2));
            for (DayOfWeek day : days) {
                schedule.computeIfAbsent(day, key -> new TreeSet<>()).addAll(times);
            }
        }
        Map<DayOfWeek, List<LocalTime>> result = new EnumMap<>(DayOfWeek.class);
        schedule.forEach((day, times) -> {
            if (!times.isEmpty()) {
                result.put(day, List.copyOf(times));
            }
        });
        return result;
    }

    public List<LocalDateTime> generate(String guide, LocalDate from, LocalDate to) {
        if (from == null || to == null || from.isAfter(to)) {
            return List.of();
        }
        Map<DayOfWeek, List<LocalTime>> schedule = parseSchedule(guide);
        List<LocalDateTime> sessions = new ArrayList<>();
        for (LocalDate date = from; !date.isAfter(to); date = date.plusDays(1)) {
            if (schedule.isEmpty()) {
                sessions.add(date.atStartOfDay());
                continue;
            }
            for (LocalTime time : schedule.getOrDefault(date.getDayOfWeek(), List.of())) {
                sessions.add(date.atTime(time));
            }
        }
        return sessions;
    }

    private List<DayOfWeek> parseDays(String text) {
        String[] parts = text.split(RANGE);
        DayOfWeek start = toDay(parts[0]);
        if (start == null) {
            return List.of();
        }
        if (parts.length == 1) {
            return List.of(start);
        }
        DayOfWeek end = toDay(parts[1]);
        if (end == null) {
            return List.of(start);
        }
        List<DayOfWeek> days = new ArrayList<>();
        DayOfWeek day = start;
        days.add(day);
        while (day != end) {
            day = day.plus(1);
            days.add(day);
        }
        return days;
    }

    private DayOfWeek toDay(String text) {
        String trimmed = text.trim();
        if (!trimmed.endsWith(DAY_SUFFIX)) {
            return null;
        }
        return DAYS.get(trimmed.substring(0, 1));
    }

    private List<LocalTime> parseTimes(String text) {
        List<LocalTime> times = new ArrayList<>();
        Matcher matcher = TIME.matcher(text);
        while (matcher.find()) {
            int hour = Integer.parseInt(matcher.group(1));
            int minute = Integer.parseInt(matcher.group(2));
            if (hour < 24 && minute < 60) {
                times.add(LocalTime.of(hour, minute));
            }
        }
        return times;
    }
}
