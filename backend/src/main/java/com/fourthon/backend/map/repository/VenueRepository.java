package com.fourthon.backend.map.repository;

import com.fourthon.backend.map.domain.Venue;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VenueRepository extends JpaRepository<Venue, Long> {

    Optional<Venue> findByKopisVenueId(String kopisVenueId);
}
