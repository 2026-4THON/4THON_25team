package com.fourthon.backend.show.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ShowSeatPrice {

    @Column(name = "seat_grade", nullable = false, length = 100)
    private String grade;

    @Column(nullable = false)
    private int price;

    public ShowSeatPrice(String grade, int price) {
        this.grade = grade;
        this.price = price;
    }
}
