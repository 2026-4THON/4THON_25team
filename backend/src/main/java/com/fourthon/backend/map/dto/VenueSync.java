package com.fourthon.backend.map.dto;

public final class VenueSync {

    private VenueSync() {
    }

    public record Create(
            String kopisVenueId,
            String name,
            String address,
            Double latitude,
            Double longitude
    ) {
    }
}
