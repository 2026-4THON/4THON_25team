package com.fourthon.backend.kopis.client;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface KopisClient {

    List<KopisResponse.Performance> fetchPerformances(
            LocalDate from,
            LocalDate to,
            LocalDate updatedAfter,
            int page,
            int rows
    );

    Optional<KopisResponse.PerformanceDetail> fetchPerformanceDetail(String kopisId);

    Optional<KopisResponse.Facility> fetchFacility(String kopisVenueId);

    List<KopisResponse.BoxOffice> fetchBoxOffice(LocalDate date);
}
