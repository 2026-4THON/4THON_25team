package com.fourthon.backend.ticket.domain;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;



@Getter @NoArgsConstructor
@Entity @Table(name="catalog_performances")
public class CatalogPerformance {
    @Id private Long id; // 공연 시스템의 실제 ID와 매핑 (사전 데이터 등록 필요)
    @Column(nullable=false) private String title;
    @Enumerated(EnumType.STRING) @Column(nullable=false) private Genre genre;
    @Column(nullable=false) private String venue;
    @Column(nullable=false) private Integer runningMinutes;
    @Column(length=1000) private String posterUrl;
    public enum Genre { MUSICAL, PLAY, ETC }
}
