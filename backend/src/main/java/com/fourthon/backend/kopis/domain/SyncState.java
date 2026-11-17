package com.fourthon.backend.kopis.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "sync_states")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SyncState {

    @Id
    @Column(length = 50)
    private String name;

    private LocalDate lastSuccessDate;

    public SyncState(String name) {
        this.name = name;
    }

    public void markSuccess(LocalDate date) {
        this.lastSuccessDate = date;
    }
}
