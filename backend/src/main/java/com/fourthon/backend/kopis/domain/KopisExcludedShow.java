package com.fourthon.backend.kopis.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "kopis_excluded_shows")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class KopisExcludedShow {

    @Id
    @Column(name = "kopis_id", length = 20)
    private String kopisId;

    @Column(nullable = false)
    private LocalDateTime checkedAt;

    public KopisExcludedShow(String kopisId, LocalDateTime checkedAt) {
        this.kopisId = kopisId;
        this.checkedAt = checkedAt;
    }
}
