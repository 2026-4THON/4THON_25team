package com.fourthon.backend.taste.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Locale;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LifeWork {

    private static final String NON_WORD = "[^\\p{L}\\p{N}]";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private LifeWorkCategory category;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false, length = 100)
    private String normalizedTitle;

    public LifeWork(LifeWorkCategory category, String title) {
        this.category = category;
        this.title = title.trim();
        this.normalizedTitle = normalize(title);
    }

    public static String normalize(String title) {
        return title.replaceAll(NON_WORD, "").toLowerCase(Locale.ROOT);
    }
}
