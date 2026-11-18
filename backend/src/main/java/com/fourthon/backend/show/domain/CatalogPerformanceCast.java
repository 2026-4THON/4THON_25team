package com.fourthon.backend.show.domain;

import com.fourthon.backend.ticket.domain.CatalogActor;
import com.fourthon.backend.ticket.domain.CatalogPerformance;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "catalog_performance_casts",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_catalog_performance_casts_performance_actor",
                columnNames = {"performance_id", "actor_id"}
        )
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CatalogPerformanceCast {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "performance_id")
    private CatalogPerformance performance;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "actor_id")
    private CatalogActor actor;

    @Column(nullable = false)
    private int castOrder;
}
