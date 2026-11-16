package com.fourthon.backend.ticket.domain;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;



@Getter @NoArgsConstructor
@Entity @Table(name="ticket_castings")
public class TicketCasting {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch=FetchType.LAZY,optional=false)
    @JoinColumn(name="ticket_id") private Ticket ticket;
    @ManyToOne(fetch=FetchType.LAZY,optional=false)
    @JoinColumn(name="actor_id") private CatalogActor actor;
    @Column(nullable=false) private int selectionOrder;
    public TicketCasting(CatalogActor actor,int selectionOrder){this.actor=actor;this.selectionOrder=selectionOrder;}
    void attach(Ticket ticket){this.ticket=ticket;}
}
