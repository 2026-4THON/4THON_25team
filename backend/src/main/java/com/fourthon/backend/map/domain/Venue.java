package com.fourthon.backend.map.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "venues",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_venues_kopis_venue_id",
                columnNames = "kopis_venue_id"
        )
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Venue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "kopis_venue_id", nullable = false, length = 20)
    private String kopisVenueId;

    @Column(nullable = false)
    private String name;

    @Column(length = 500)
    private String address;

    private Double latitude;

    private Double longitude;

    public Venue(
            String kopisVenueId,
            String name,
            String address,
            Double latitude,
            Double longitude) {

        this.kopisVenueId = kopisVenueId;
        this.name = name;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
