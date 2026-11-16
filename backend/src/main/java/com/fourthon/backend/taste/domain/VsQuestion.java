package com.fourthon.backend.taste.domain;

import java.util.Arrays;
import java.util.Optional;

public enum VsQuestion {
    CHARACTER_EMOTION("3-1-1", "열정적이고 감정적인", "냉철하고 이성적인"),
    CHARACTER_ATTITUDE("3-1-2", "능글맞고 여유로운", "냉소적이고 비판적인"),
    CHARACTER_MYSTERY("3-1-3", "신비롭고 속을 알수없는", "순수하고 솔직한"),
    VALUE_GOAL("3-2-1", "야망과 권력을 쫒는", "신념과 사랑을 지키는"),
    VALUE_ACTION("3-2-2", "계산적이고 치밀한", "충동적이고 자유로운"),
    RELATION("3-3-1", "대립하고 경쟁하는", "의지하고 이해하는"),
    NARRATIVE_ENDING("3-3-2", "추락하고 파멸하는", "극복하고 성장하는"),
    NARRATIVE_MORALITY("3-3-3", "선과 악이 모호한", "선명한 신념을 가진");

    private final String code;
    private final String left;
    private final String right;

    VsQuestion(String code, String left, String right) {
        this.code = code;
        this.left = left;
        this.right = right;
    }

    public String getCode() {
        return code;
    }

    public static Optional<VsQuestion> findByChoice(String choice) {
        return Arrays.stream(values())
                .filter(question -> question.left.equals(choice) || question.right.equals(choice))
                .findFirst();
    }
}
