package com.fourthon.backend.ticket.domain;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Getter @NoArgsConstructor
@Entity @Table(name="catalog_session_casts", uniqueConstraints=@UniqueConstraint(name="uk_session_actor",columnNames={"session_id","actor_id"}))
public class CatalogSessionCast {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch=FetchType.LAZY, optional=false)
    @JoinColumn(name="session_id") private CatalogSession session;
    @ManyToOne(fetch=FetchType.LAZY, optional=false)
    @JoinColumn(name="actor_id") private CatalogActor actor;
    @Column(nullable=false) private int castOrder;
}
