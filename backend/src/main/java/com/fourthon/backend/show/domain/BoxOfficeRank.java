package com.fourthon.backend.show.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(
        name = "box_office_ranks",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_box_office_ranks_date_performance",
                columnNames = {"ranked_date", "performance_id"}
        )
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BoxOfficeRank {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ranked_date", nullable = false)
    private LocalDate rankedDate;

    @Column(name = "performance_id", nullable = false)
    private Long performanceId;

    @Column(nullable = false)
    private int rankNo;

    public BoxOfficeRank(LocalDate rankedDate, Long performanceId, int rankNo) {
        this.rankedDate = rankedDate;
        this.performanceId = performanceId;
        this.rankNo = rankNo;
    }
}
