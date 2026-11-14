package com.fourthon.backend.ticket.domain;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;



@Getter @NoArgsConstructor
@Entity @Table(name="ticket_review_actors")
public class TicketReviewActor {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch=FetchType.LAZY,optional=false)
    @JoinColumn(name="review_id") private TicketReview review;
    @ManyToOne(fetch=FetchType.LAZY,optional=false)
    @JoinColumn(name="actor_id") private CatalogActor actor;
    @Column(nullable=false) private int selectionOrder;
    public TicketReviewActor(CatalogActor actor,int selectionOrder){this.actor=actor;this.selectionOrder=selectionOrder;}
    void attach(TicketReview review){this.review=review;}
}
