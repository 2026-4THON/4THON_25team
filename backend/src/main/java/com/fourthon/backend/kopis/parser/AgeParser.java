package com.fourthon.backend.kopis.parser;

import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class AgeParser {

    private static final Pattern YEARS = Pattern.compile("(\\d+)\\s*세");
    private static final Pattern MONTHS = Pattern.compile("(\\d+)\\s*개월");
    private static final String ADULT_ONLY = "청소년 관람불가";
    private static final int ADULT_AGE = 19;
    private static final int MONTHS_PER_YEAR = 12;

    public int parseMinAge(String text) {
        if (text == null || text.isBlank()) {
            return 0;
        }
        if (text.contains(ADULT_ONLY)) {
            return ADULT_AGE;
        }
        Matcher years = YEARS.matcher(text);
        if (years.find()) {
            return Integer.parseInt(years.group(1));
        }
        Matcher months = MONTHS.matcher(text);
        if (months.find()) {
            return Integer.parseInt(months.group(1)) / MONTHS_PER_YEAR;
        }
        return 0;
    }
}
