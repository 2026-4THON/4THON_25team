package com.fourthon.backend.map.service;

import com.fourthon.backend.map.domain.Venue;
import com.fourthon.backend.map.dto.VenueSync;
import com.fourthon.backend.map.repository.VenueRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VenueService {

    private final VenueRepository venueRepository;

    public Optional<Long> findIdByKopisVenueId(String kopisVenueId) {
        return venueRepository.findByKopisVenueId(kopisVenueId)
                .map(Venue::getId);
    }

    @Transactional
    public Long create(VenueSync.Create command) {
        Venue venue = new Venue(
                command.kopisVenueId(),
                command.name(),
                command.address(),
                command.latitude(),
                command.longitude()
        );
        return venueRepository.save(venue).getId();
    }
}
