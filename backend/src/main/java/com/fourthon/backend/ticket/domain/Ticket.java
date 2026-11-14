
package com.fourthon.backend.ticket.domain;

import com.fourthon.backend.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor
@Entity
@Table(
        name = "tickets",
        indexes = @Index(
                name = "ix_ticket_owner_viewed",
                columnList = "user_id,viewed_at"
        )
)
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "performance_id", nullable = false)
    private CatalogPerformance performance;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "session_id", nullable = false)
    private CatalogSession session;

    @Column(name = "viewed_at", nullable = false)
    private LocalDateTime viewedAt;

    @Column(nullable = false)
    private int seatFloor;

    @Column(nullable = false)
    private int seatRow;

    @Column(nullable = false)
    private int seatNumber;

    @Column(nullable = false)
    private long ticketPrice;

    @Column(nullable = false)
    private long culturePassAmount;

    @Column(nullable = false)
    private long mdPrice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Companion companion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CompanionRating companionRating;

    // 1~10 = 0.5~5.0점
    @Column(nullable = false)
    private int ratingHalfUnits;

    @OneToMany(
            mappedBy = "ticket",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @OrderBy("selectionOrder ASC")
    private List<TicketCasting> castings =
            new ArrayList<>();

    @OneToMany(
            mappedBy = "ticket",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<TicketReview> reviews =
            new ArrayList<>();

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public void setIdentity(
            User user,
            CatalogPerformance performance,
            CatalogSession session
    ) {
        this.user = user;
        this.performance = performance;
        this.session = session;
        this.viewedAt = session.getStartsAt();
    }

    public void setDetails(
            int floor,
            int row,
            int number,
            long price,
            long pass,
            long md,
            Companion companion,
            CompanionRating companionRating,
            int ratingHalfUnits
    ) {
        this.seatFloor = floor;
        this.seatRow = row;
        this.seatNumber = number;
        this.ticketPrice = price;
        this.culturePassAmount = pass;
        this.mdPrice = md;
        this.companion = companion;
        this.companionRating = companionRating;
        this.ratingHalfUnits = ratingHalfUnits;
    }

    // 총지출 계산
    public long totalExpense() {
        return ticketPrice - culturePassAmount + mdPrice;
    }

    // 캐스팅 정보만 새로 구성
    public void replaceCastings() {
        castings.clear();
    }

    // 실제로 제거한 후기만 삭제
    public void removeReview(TicketReview review) {
        reviews.remove(review);
    }

    public void addCasting(TicketCasting casting) {
        castings.add(casting);
        casting.attach(this);
    }

    public void addReview(TicketReview review) {
        reviews.add(review);
        review.attach(this);
    }

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum Companion {
        ALONE,
        PARTNER,
        FRIEND,
        FAMILY
    }

    public enum CompanionRating {
        RECOMMEND,
        NORMAL,
        NOT_RECOMMEND
    }
}
