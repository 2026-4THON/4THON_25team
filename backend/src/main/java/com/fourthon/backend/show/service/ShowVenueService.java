package com.fourthon.backend.show.service;

import com.fourthon.backend.show.domain.ShowDetail;
import com.fourthon.backend.show.dto.ShowQuery;
import com.fourthon.backend.show.repository.ShowDetailRepository;
import com.fourthon.backend.show.repository.ShowVenueRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ShowVenueService {

    private final ShowVenueRepository showVenueRepository;
    private final ShowDetailRepository showDetailRepository;

    public Map<Long, Long> countOngoingByVenue(LocalDate today) {
        return toCountMap(showVenueRepository.countOngoingByVenue(today));
    }

    public Map<Long, Long> countTicketsByVenue(Long userId) {
        return toCountMap(showVenueRepository.countTicketsByVenue(userId));
    }

    public List<ShowQuery.VenueShow> findOngoingAtVenue(Long venueId, LocalDate today) {
        List<ShowVenueRepository.VenueShowRow> rows = showVenueRepository.findOngoingAtVenue(venueId, today);
        Map<Long, List<String>> keywords = findKeywords(rows.stream()
                .map(ShowVenueRepository.VenueShowRow::getPerformanceId)
                .toList());
        return rows.stream()
                .map(row -> new ShowQuery.VenueShow(
                        row.getPerformanceId(),
                        row.getTitle(),
                        row.getGenre().name(),
                        row.getPosterUrl(),
                        row.getStartDate(),
                        row.getEndDate(),
                        keywords.getOrDefault(row.getPerformanceId(), List.of())
                ))
                .toList();
    }

    public List<ShowQuery.VenueTicket> findTicketsAtVenue(Long userId, Long venueId) {
        List<ShowVenueRepository.VenueTicketRow> rows = showVenueRepository.findTicketsAtVenue(userId, venueId);
        Map<Long, List<String>> keywords = findKeywords(rows.stream()
                .map(ShowVenueRepository.VenueTicketRow::getPerformanceId)
                .distinct()
                .toList());
        return rows.stream()
                .map(row -> new ShowQuery.VenueTicket(
                        row.getTicketId(),
                        row.getPerformanceId(),
                        row.getTitle(),
                        row.getViewedAt(),
                        keywords.getOrDefault(row.getPerformanceId(), List.of())
                ))
                .toList();
    }

    private Map<Long, List<String>> findKeywords(Collection<Long> performanceIds) {
        return showDetailRepository.findAllById(performanceIds).stream()
                .collect(Collectors.toMap(
                        ShowDetail::getPerformanceId,
                        detail -> List.copyOf(detail.getKeywords())
                ));
    }

    private Map<Long, Long> toCountMap(List<ShowVenueRepository.VenueCount> counts) {
        return counts.stream()
                .collect(Collectors.toMap(
                        ShowVenueRepository.VenueCount::getVenueId,
                        ShowVenueRepository.VenueCount::getItemCount
                ));
    }
}
