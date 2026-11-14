package com.fourthon.backend.ticket.domain;
import com.fourthon.backend.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;



@Getter @NoArgsConstructor
@Entity @Table(name="ticket_review_likes",uniqueConstraints=@UniqueConstraint(name="uk_like_review_user",columnNames={"review_id","user_id"}))
public class TicketReviewLike {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch=FetchType.LAZY,optional=false)
    @JoinColumn(name="review_id") private TicketReview review;
    @ManyToOne(fetch=FetchType.LAZY,optional=false)
    @JoinColumn(name="user_id") private User user;
}
