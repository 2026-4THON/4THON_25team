package com.fourthon.backend.ticket.dto;
import com.fourthon.backend.ticket.domain.Ticket;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.List;



public record TicketRequest(
    @NotNull Long performanceId,
    @NotNull Long sessionId,
    @NotNull @Min(1) Integer seatFloor,
    @NotNull @Min(1) Integer seatRow,
    @NotNull @Min(1) Integer seatNumber,
    @NotNull List<@NotNull Long> castingActorIds,
    @NotNull @PositiveOrZero Long ticketPrice,
    @NotNull @PositiveOrZero Long culturePassAmount,
    @NotNull @PositiveOrZero Long mdPrice,
    @NotNull Ticket.Companion companion,
    @NotNull Ticket.CompanionRating companionRating,
    @NotNull BigDecimal rating,
    String performanceReview,
    @Valid List<ActorReviewInput> actorReviews,
    @Valid List<PairReviewInput> pairReviews
) {
    public record ActorReviewInput(@NotNull Long actorId, @NotBlank String content) {}
    public record PairReviewInput(@NotEmpty List<@NotNull Long> actorIds,@NotBlank String content) {}
}
