package com.fourthon.backend.ticket.service;
import com.fourthon.backend.ticket.domain.*;
import com.fourthon.backend.ticket.dto.*;
import com.fourthon.backend.ticket.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDate;
import java.util.List;



@Service @Transactional(readOnly=true)
public class CatalogService {
    private final CatalogPerformanceRepository performances;
    private final CatalogSessionRepository sessions;
    private final CatalogSessionCastRepository casts;
    public CatalogService(CatalogPerformanceRepository performances,CatalogSessionRepository sessions,CatalogSessionCastRepository casts){
        this.performances=performances;this.sessions=sessions;this.casts=casts;
    }
    public CatalogPerformance requirePerformance(Long id){return performances.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"등록되어 있지 않은 작품입니다"));}
    public CatalogSession requireSession(Long performanceId,Long sessionId){return sessions.findByIdAndPerformance_Id(sessionId,performanceId).orElseThrow(()->new ResponseStatusException(HttpStatus.BAD_REQUEST,"해당 공연의 실제 회차가 아닙니다"));}
    public CatalogResponse.Performance toResponse(CatalogPerformance p){return new CatalogResponse.Performance(p.getId(),p.getTitle(),p.getGenre().name(),p.getVenue(),p.getRunningMinutes(),p.getPosterUrl());}
    public List<CatalogResponse.Performance> search(String keyword){return performances.findTop30ByTitleContainingIgnoreCaseOrderByTitleAsc(keyword).stream().map(this::toResponse).toList();}
    public CatalogResponse.Dates dates(Long id){requirePerformance(id);return new CatalogResponse.Dates(id,sessions.findByPerformance_IdOrderByStartsAtAsc(id).stream().map(s->s.getStartsAt().toLocalDate()).distinct().toList());}
    public List<CatalogResponse.Session> sessions(Long id,LocalDate date){requirePerformance(id);return sessions.findByPerformance_IdAndStartsAtGreaterThanEqualAndStartsAtLessThanOrderByStartsAtAsc(id,date.atStartOfDay(),date.plusDays(1).atStartOfDay()).stream().map(s->new CatalogResponse.Session(s.getId(),s.getStartsAt())).toList();}
    public CatalogResponse.Details details(Long id,Long sessionId){CatalogSession s=requireSession(id,sessionId);return new CatalogResponse.Details(toResponse(s.getPerformance()),s.getId(),casts.findBySession_IdOrderByCastOrderAsc(sessionId).stream().map(c->new TicketResponse.Actor(c.getActor().getId(),c.getActor().getName())).toList());}
}
