package com.fourthon.backend.ticket.dto;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;



public final class CatalogResponse {
    private CatalogResponse() {}
    public record Performance(Long id,String title,String genre,String venue,Integer runningMinutes,String posterUrl) {}
    public record Dates(Long performanceId,List<LocalDate> dates) {}
    public record Session(Long id,LocalDateTime startsAt) {}
    public record Details(Performance performance,Long sessionId,List<TicketResponse.Actor> castings) {}
}
