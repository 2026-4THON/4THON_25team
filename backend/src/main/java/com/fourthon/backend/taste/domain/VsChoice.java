package com.fourthon.backend.taste.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class VsChoice {

    @Column(nullable = false, length = 10)
    private String questionCode;

    @Column(nullable = false, length = 50)
    private String choice;

    public VsChoice(String questionCode, String choice) {
        this.questionCode = questionCode;
        this.choice = choice;
    }
}
