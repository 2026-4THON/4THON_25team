package com.fourthon.backend.ticket.dto;
import com.fourthon.backend.ticket.domain.Ticket;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;



public final class TicketResponse {
    private TicketResponse() {}
    public record Actor(Long id,String name) {}
    public record Review(Long id,String type,String content,List<Actor> actors) {}
    public record Summary(Long id,int ticketNumber,Long performanceId,String title,String genre,
                          String venue,String posterUrl,LocalDateTime viewedAt) {}
    public record Detail(Long id,int ticketNumber,Long performanceId,Long sessionId,
                         String title,String genre,String venue,String posterUrl,int runningMinutes,
                         LocalDateTime viewedAt,int seatFloor,int seatRow,int seatNumber,
                         Ticket.Companion companion,Ticket.CompanionRating companionRating,
                         List<Actor> castings,long ticketPrice,long culturePassAmount,long mdPrice,
                         long totalExpense,BigDecimal rating,List<Review> reviews) {}
    public record Saved(Long id,int ticketNumber,String message) {}
}
