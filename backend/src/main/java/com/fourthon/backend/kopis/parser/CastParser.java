package com.fourthon.backend.kopis.parser;

import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class CastParser {

    private static final String SEPARATOR = ",";
    private static final String ETC_SUFFIX = "\\s*등$";

    public List<String> parseNames(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }
        return Arrays.stream(text.split(SEPARATOR))
                .map(String::trim)
                .map(name -> name.replaceAll(ETC_SUFFIX, "").trim())
                .filter(name -> !name.isEmpty())
                .distinct()
                .toList();
    }
}
