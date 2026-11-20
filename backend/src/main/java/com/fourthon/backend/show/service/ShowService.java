package com.fourthon.backend.show.service;

import com.fourthon.backend.common.exception.CodedException;
import com.fourthon.backend.show.dto.ShowResponse;
import com.fourthon.backend.show.repository.CatalogPerformanceCastRepository;
import com.fourthon.backend.show.repository.ShowDetailRepository;
import com.fourthon.backend.show.repository.ShowRatingRepository;
import com.fourthon.backend.ticket.domain.CatalogPerformance;
import com.fourthon.backend.ticket.repository.CatalogPerformanceRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ShowService {

    private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");
    private static final double HALF_UNITS_PER_STAR = 2.0;
    private static final double ONE_DECIMAL = 10.0;

    private final CatalogPerformanceRepository catalogPerformanceRepository;
    private final ShowDetailRepository showDetailRepository;
    private final CatalogPerformanceCastRepository catalogPerformanceCastRepository;
    private final ShowRatingRepository showRatingRepository;

    public ShowResponse.Detail getDetail(Long performanceId) {
        CatalogPerformance performance = catalogPerformanceRepository.findById(performanceId)
                .orElseThrow(() -> new CodedException(
                        HttpStatus.NOT_FOUND,
                        "SHOW_NOT_FOUND",
                        "공연을 찾을 수 없습니다"
                ));
        List<ShowResponse.Actor> cast = catalogPerformanceCastRepository.findCastWithActors(performanceId).stream()
                .map(member -> new ShowResponse.Actor(member.getActor().getId(), member.getActor().getName()))
                .toList();
        return ShowResponse.Detail.from(
                performance,
                showDetailRepository.findById(performanceId).orElse(null),
                cast,
                getRating(performanceId),
                LocalDate.now(SEOUL)
        );
    }

    public ShowResponse.Rating getRating(Long performanceId) {
        ShowRatingRepository.RatingSummary summary = showRatingRepository.findSummary(performanceId);
        long count = summary.getRatingCount() == null ? 0 : summary.getRatingCount();
        Double averageHalfUnits = summary.getAverageHalfUnits();
        if (count == 0 || averageHalfUnits == null) {
            return new ShowResponse.Rating(null, 0);
        }
        double average = averageHalfUnits / HALF_UNITS_PER_STAR;
        return new ShowResponse.Rating(Math.round(average * ONE_DECIMAL) / ONE_DECIMAL, count);
    }
}
