package com.fourthon.backend.show.service;

import com.fourthon.backend.show.dto.ShowQuery;
import com.fourthon.backend.show.repository.ShowVenueRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ShowVenueService {

    private final ShowVenueRepository showVenueRepository;

    public Map<Long, Long> countOngoingByVenue(LocalDate today) {
        return toCountMap(showVenueRepository.countOngoingByVenue(today));
    }

    public Map<Long, Long> countTicketsByVenue(Long userId) {
        return toCountMap(showVenueRepository.countTicketsByVenue(userId));
    }

    public List<ShowQuery.VenueShow> findOngoingAtVenue(Long venueId, LocalDate today) {
        return showVenueRepository.findOngoingAtVenue(venueId, today).stream()
                .map(row -> new ShowQuery.VenueShow(
                        row.getPerformanceId(),
                        row.getTitle(),
                        row.getGenre().name(),
                        row.getPosterUrl(),
                        row.getStartDate(),
                        row.getEndDate()
                ))
                .toList();
    }

    public List<ShowQuery.VenueTicket> findTicketsAtVenue(Long userId, Long venueId) {
        return showVenueRepository.findTicketsAtVenue(userId, venueId).stream()
                .map(row -> new ShowQuery.VenueTicket(
                        row.getTicketId(),
                        row.getPerformanceId(),
                        row.getTitle(),
                        row.getViewedAt()
                ))
                .toList();
    }

    private Map<Long, Long> toCountMap(List<ShowVenueRepository.VenueCount> counts) {
        return counts.stream()
                .collect(Collectors.toMap(
                        ShowVenueRepository.VenueCount::getVenueId,
                        ShowVenueRepository.VenueCount::getItemCount
                ));
    }
}
