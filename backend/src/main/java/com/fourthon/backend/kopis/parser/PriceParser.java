package com.fourthon.backend.kopis.parser;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class PriceParser {

    private static final Pattern SEAT_PRICE = Pattern.compile("([^,0-9]*?)\\s*([0-9][0-9,]*)\\s*원");
    private static final String ALL_SEATS = "전석";
    private static final String FREE = "무료";

    public List<SeatPrice> parse(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }
        Map<String, Integer> prices = new LinkedHashMap<>();
        Matcher matcher = SEAT_PRICE.matcher(text);
        while (matcher.find()) {
            String grade = normalizeGrade(matcher.group(1));
            int price = Integer.parseInt(matcher.group(2).replace(",", ""));
            prices.putIfAbsent(grade, price);
        }
        if (prices.isEmpty() && text.contains(FREE)) {
            prices.put(ALL_SEATS, 0);
        }
        List<SeatPrice> result = new ArrayList<>();
        prices.forEach((grade, price) -> result.add(new SeatPrice(grade, price)));
        return result;
    }

    public OptionalInt minPrice(List<SeatPrice> prices) {
        return prices.stream()
                .mapToInt(SeatPrice::price)
                .min();
    }

    private String normalizeGrade(String raw) {
        String grade = raw.replaceAll("^[\\s/·()]+", "").trim();
        return grade.isEmpty() ? ALL_SEATS : grade;
    }

    public record SeatPrice(
            String grade,
            int price
    ) {
    }
}
