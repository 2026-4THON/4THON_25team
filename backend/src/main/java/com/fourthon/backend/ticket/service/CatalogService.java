
package com.fourthon.backend.ticket.service;

import com.fourthon.backend.ticket.domain.*;
import com.fourthon.backend.ticket.dto.*;
import com.fourthon.backend.ticket.repository.*;

import com.fourthon.backend.show.domain.CatalogPerformanceCast;
import com.fourthon.backend.show.repository.CatalogPerformanceCastRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class CatalogService {

    private final CatalogPerformanceRepository performances;
    private final CatalogSessionRepository sessions;
    private final CatalogPerformanceCastRepository casts;

    public CatalogService(
            CatalogPerformanceRepository performances,
            CatalogSessionRepository sessions,
            CatalogPerformanceCastRepository casts
    ) {
        this.performances = performances;
        this.sessions = sessions;
        this.casts = casts;
    }

    // 공연 존재 여부 확인
    public CatalogPerformance requirePerformance(Long id) {
        return performances.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "등록되어 있지 않은 작품입니다"
                        )
                );
    }

    // 선택한 회차가 해당 공연에 속하는지 확인
    public CatalogSession requireSession(
            Long performanceId,
            Long sessionId
    ) {
        return sessions.findByIdAndPerformance_Id(
                        sessionId,
                        performanceId
                )
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.BAD_REQUEST,
                                "해당 공연의 실제 회차가 아닙니다"
                        )
                );
    }

    // 공연 정보 응답 변환
    public CatalogResponse.Performance toResponse(
            CatalogPerformance p
    ) {
        return new CatalogResponse.Performance(
                p.getId(),
                p.getTitle(),
                p.getGenre().name(),
                p.getVenue(),
                p.getRunningMinutes(),
                p.getPosterUrl()
        );
    }

    // 공연 검색
    public List<CatalogResponse.Performance> search(
            String keyword
    ) {
        return performances
                .findTop30ByTitleContainingIgnoreCaseOrderByTitleAsc(
                        keyword
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // 공연 날짜 조회
    public CatalogResponse.Dates dates(Long id) {
        requirePerformance(id);

        return new CatalogResponse.Dates(
                id,
                sessions.findByPerformance_IdOrderByStartsAtAsc(id)
                        .stream()
                        .map(s -> s.getStartsAt().toLocalDate())
                        .distinct()
                        .toList()
        );
    }

    // 날짜별 공연 회차 조회
    public List<CatalogResponse.Session> sessions(
            Long id,
            LocalDate date
    ) {
        requirePerformance(id);

        return sessions
                .findByPerformance_IdAndStartsAtGreaterThanEqualAndStartsAtLessThanOrderByStartsAtAsc(
                        id,
                        date.atStartOfDay(),
                        date.plusDays(1).atStartOfDay()
                )
                .stream()
                .map(s -> new CatalogResponse.Session(
                        s.getId(),
                        s.getStartsAt()
                ))
                .toList();
    }

    // 공연 상세: 해당 작품의 전체 출연진 반환
    public CatalogResponse.Details details(
            Long id,
            Long sessionId
    ) {
        // 선택한 회차가 해당 공연에 속하는지 검증
        CatalogSession session = requireSession(id, sessionId);

        // 회차별 캐스팅이 아닌 작품 전체 출연진 조회
        List<TicketResponse.Actor> actors = casts
                .findCastWithActors(id)
                .stream()
                .map(CatalogPerformanceCast::getActor)
                .map(actor -> new TicketResponse.Actor(
                        actor.getId(),
                        actor.getName()
                ))
                .toList();

        return new CatalogResponse.Details(
                toResponse(session.getPerformance()),
                session.getId(),
                actors
        );
    }
}
