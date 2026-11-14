
package com.fourthon.backend.ticket.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "ticket_reviews")
public class TicketReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(name = "ticket_id")
    private Ticket ticket;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReviewType type;

    @Lob
    private String content;

    @OneToMany(
            mappedBy = "review",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @OrderBy("selectionOrder ASC")
    private List<TicketReviewActor> actors =
            new ArrayList<>();

    @OneToMany(
            mappedBy = "review",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<TicketReviewLike> likes =
            new ArrayList<>();

    public TicketReview(
            ReviewType type,
            String content
    ) {
        this.type = type;
        this.content = content;
    }

    void attach(Ticket ticket) {
        this.ticket = ticket;
    }


    public void changeContent(String content) {
        this.content = content;
    }


    public void replaceActors(List<CatalogActor> cast) {

        boolean same = actors.size() == cast.size();

        if (same) {
            for (int i = 0; i < cast.size(); i++) {
                if (!actors.get(i).getActor().getId()
                        .equals(cast.get(i).getId())) {
                    same = false;
                    break;
                }
            }
        }

        if (same) {
            return;
        }

        actors.clear();

        for (int i = 0; i < cast.size(); i++) {
            addActor(cast.get(i), i);
        }
    }

    public void addActor(
            CatalogActor actor,
            int order
    ) {
        TicketReviewActor reviewActor =
                new TicketReviewActor(actor, order);

        reviewActor.attach(this);
        actors.add(reviewActor);
    }

    public enum ReviewType {
        PERFORMANCE,
        ACTOR,
        PAIR
    }
}
