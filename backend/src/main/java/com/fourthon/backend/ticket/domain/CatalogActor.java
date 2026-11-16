package com.fourthon.backend.ticket.domain;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;




@Getter @NoArgsConstructor
@Entity @Table(name="catalog_actors")
public class CatalogActor {
    @Id private Long id;
    @Column(nullable=false) private String name;
}
