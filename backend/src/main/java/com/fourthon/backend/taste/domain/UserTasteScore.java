package com.fourthon.backend.taste.domain;

import com.fourthon.backend.common.domain.TasteTag;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "user_taste_scores",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_user_taste_scores_user_tag",
                columnNames = {"user_id", "tag"}
        )
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserTasteScore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "tag", nullable = false, length = 30)
    private TasteTag tag;

    @Column(nullable = false)
    private double score;

    public UserTasteScore(Long userId, TasteTag tag, double score) {
        this.userId = userId;
        this.tag = tag;
        this.score = score;
    }
}
