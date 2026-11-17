package com.fourthon.backend.kopis.parser;

import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class RuntimeParser {

    private static final Pattern HOURS = Pattern.compile("(\\d+)\\s*시간");
    private static final Pattern MINUTES = Pattern.compile("(\\d+)\\s*분");
    private static final int MINUTES_PER_HOUR = 60;

    public int parseMinutes(String text) {
        if (text == null || text.isBlank()) {
            return 0;
        }
        int hours = firstNumber(HOURS, text);
        int minutes = firstNumber(MINUTES, text);
        return hours * MINUTES_PER_HOUR + minutes;
    }

    private int firstNumber(Pattern pattern, String text) {
        Matcher matcher = pattern.matcher(text);
        return matcher.find() ? Integer.parseInt(matcher.group(1)) : 0;
    }
}
