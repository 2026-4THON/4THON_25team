package com.fourthon.backend.taste.domain;

import java.util.Arrays;
import java.util.Optional;

public enum TasteTag {
    ROMANCE(TagType.GENRE, "로맨스"),
    FANTASY(TagType.GENRE, "판타지"),
    MYSTERY(TagType.GENRE, "미스터리·추리"),
    COMEDY(TagType.GENRE, "코미디"),
    THRILLER(TagType.GENRE, "스릴러"),
    HORROR(TagType.GENRE, "공포"),
    HISTORICAL(TagType.GENRE, "역사·시대극"),
    ACTION(TagType.GENRE, "모험·액션"),
    DRAMA(TagType.GENRE, "드라마·휴먼"),
    MUSIC_ART(TagType.GENRE, "음악·예술"),
    SOCIAL(TagType.GENRE, "사회·정치"),
    SF(TagType.GENRE, "SF"),
    YOUTH(TagType.GENRE, "성장·청춘"),
    TRAGEDY(TagType.GENRE, "비극"),
    BRIGHT(TagType.MOOD, "밝고 유쾌한"),
    WARM(TagType.MOOD, "따뜻하고 감동적인"),
    DARK(TagType.MOOD, "어둡고 무거운"),
    DREAMY(TagType.MOOD, "몽환적이고 신비로운"),
    TENSE(TagType.MOOD, "긴장감 있고 몰입감 있는"),
    SAD(TagType.MOOD, "비극적이고 슬픈"),
    LINGERING(TagType.MOOD, "여운이 길고 사색적인"),
    CALM(TagType.MOOD, "잔잔하고 뭉클한");

    private final TagType type;
    private final String label;

    TasteTag(TagType type, String label) {
        this.type = type;
        this.label = label;
    }

    public TagType getType() {
        return type;
    }

    public String getLabel() {
        return label;
    }

    public static Optional<TasteTag> fromLabel(String label, TagType type) {
        return Arrays.stream(values())
                .filter(tag -> tag.type == type && tag.label.equals(label))
                .findFirst();
    }

    public static Optional<TasteTag> fromCode(String code) {
        return Arrays.stream(values())
                .filter(tag -> tag.name().equals(code))
                .findFirst();
    }
}
