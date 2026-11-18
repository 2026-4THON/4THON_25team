package com.fourthon.backend.taste.domain;

import java.util.Arrays;
import java.util.Optional;

public enum LifeWorkCategory {
    BOOK("book", "문학"),
    VIDEO("video", "영상"),
    COMIC("comic", "만화"),
    MUSIC("music", "음악"),
    THEATER("theater", "공연");

    private final String key;
    private final String label;

    LifeWorkCategory(String key, String label) {
        this.key = key;
        this.label = label;
    }

    public String getKey() {
        return key;
    }

    public String getLabel() {
        return label;
    }

    public static Optional<LifeWorkCategory> fromKey(String key) {
        return Arrays.stream(values())
                .filter(category -> category.key.equals(key))
                .findFirst();
    }
}
