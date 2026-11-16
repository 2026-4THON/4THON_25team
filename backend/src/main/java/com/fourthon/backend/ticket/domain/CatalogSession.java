package com.fourthon.backend.ticket.domain;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Getter @NoArgsConstructor
@Entity @Table(name="catalog_sessions", uniqueConstraints=@UniqueConstraint(name="uk_performance_start", columnNames={"performance_id","starts_at"}))
public class CatalogSession {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch=FetchType.LAZY, optional=false)
    @JoinColumn(name="performance_id") private CatalogPerformance performance;
    @Column(name="starts_at",nullable=false) private LocalDateTime startsAt;
    @OneToMany(mappedBy="session") @OrderBy("castOrder ASC") private List<CatalogSessionCast> casts=new ArrayList<>();
}
