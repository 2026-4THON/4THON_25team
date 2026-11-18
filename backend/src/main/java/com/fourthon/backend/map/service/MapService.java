package com.fourthon.backend.map.service;

import com.fourthon.backend.map.domain.MapMode;
import com.fourthon.backend.map.domain.Venue;
import com.fourthon.backend.map.dto.MapResponse;
import com.fourthon.backend.map.repository.VenueRepository;
import com.fourthon.backend.show.service.ShowVenueService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MapService {

    private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");

    private final VenueRepository venueRepository;
    private final ShowVenueService showVenueService;

    public List<MapResponse.VenueItem> findVenues(MapMode mode, String keyword, Long userId) {
        Map<Long, Long> counts = mode == MapMode.MINE
                ? showVenueService.countTicketsByVenue(userId)
                : showVenueService.countOngoingByVenue(today());
        String normalizedKeyword = normalize(keyword);
        return venueRepository.findAllById(counts.keySet()).stream()
                .filter(venue -> normalizedKeyword.isEmpty()
                        || normalize(venue.getName()).contains(normalizedKeyword))
                .sorted(Comparator.comparing(Venue::getName))
                .map(venue -> MapResponse.VenueItem.from(venue, counts.get(venue.getId())))
                .toList();
    }

    public List<MapResponse.VenueShow> findOngoingShows(Long venueId) {
        requireVenue(venueId);
        return showVenueService.findOngoingAtVenue(venueId, today()).stream()
                .map(MapResponse.VenueShow::from)
                .toList();
    }

    public List<MapResponse.MyTicket> findMyTickets(Long userId, Long venueId) {
        requireVenue(venueId);
        return showVenueService.findTicketsAtVenue(userId, venueId).stream()
                .map(MapResponse.MyTicket::from)
                .toList();
    }

    private void requireVenue(Long venueId) {
        if (!venueRepository.existsById(venueId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "공연장을 찾을 수 없습니다"
            );
        }
    }

    private String normalize(String text) {
        if (text == null) {
            return "";
        }
        return text.replaceAll("\\s+", "").toLowerCase(Locale.ROOT);
    }

    private LocalDate today() {
        return LocalDate.now(SEOUL);
    }
}
