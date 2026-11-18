package com.fourthon.backend.kopis.service;

import com.fourthon.backend.kopis.client.KopisClient;
import com.fourthon.backend.kopis.client.KopisException;
import com.fourthon.backend.kopis.client.KopisResponse;
import com.fourthon.backend.kopis.config.KopisProperties;
import com.fourthon.backend.kopis.parser.AgeParser;
import com.fourthon.backend.kopis.parser.CastParser;
import com.fourthon.backend.kopis.parser.GenreMapper;
import com.fourthon.backend.kopis.parser.PriceParser;
import com.fourthon.backend.kopis.parser.RuntimeParser;
import com.fourthon.backend.kopis.parser.SessionParser;
import com.fourthon.backend.map.dto.VenueSync;
import com.fourthon.backend.map.service.VenueService;
import com.fourthon.backend.show.dto.ShowSync;
import com.fourthon.backend.show.service.ShowDetailService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;

@Component
@RequiredArgsConstructor
public class ShowImporter {

    private final KopisClient kopisClient;
    private final ShowDetailService showDetailService;
    private final VenueService venueService;
    private final CatalogWriter catalogWriter;
    private final PriceParser priceParser;
    private final RuntimeParser runtimeParser;
    private final AgeParser ageParser;
    private final CastParser castParser;
    private final SessionParser sessionParser;
    private final GenreMapper genreMapper;
    private final KopisProperties kopisProperties;

    public ImportResult importShow(KopisResponse.PerformanceDetail detail, LocalDateTime now) {
        if (detail.startDate() == null || detail.endDate() == null) {
            throw new KopisException("공연 기간이 없습니다: " + detail.kopisId());
        }
        Optional<ShowSync.Info> existing = showDetailService.findSyncInfo(detail.kopisId());
        if (existing.isPresent() && isUnchanged(existing.get(), detail)) {
            return ImportResult.SKIPPED;
        }

        CatalogWriter.PerformanceRow row = toPerformanceRow(detail);
        Long performanceId = existing
                .map(info -> updatePerformance(info.performanceId(), row))
                .orElseGet(() -> catalogWriter.insertPerformance(row));

        catalogWriter.syncCast(performanceId, castParser.parseNames(detail.cast()));
        showDetailService.save(toUpsert(performanceId, detail), now);

        boolean scheduleChanged = existing
                .map(info -> !Objects.equals(info.timeGuide(), detail.timeGuide()))
                .orElse(true);
        if (scheduleChanged) {
            LocalDate today = now.toLocalDate();
            catalogWriter.syncSessions(
                    performanceId,
                    sessionParser.generate(
                            detail.timeGuide(),
                            sessionStart(detail.startDate()),
                            sessionEnd(detail.endDate(), today)
                    ),
                    today.atStartOfDay()
            );
        }
        return existing.isPresent() ? ImportResult.UPDATED : ImportResult.CREATED;
    }

    private boolean isUnchanged(ShowSync.Info info, KopisResponse.PerformanceDetail detail) {
        return info.kopisUpdatedAt() != null
                && info.kopisUpdatedAt().equals(detail.updatedAt());
    }

    private Long updatePerformance(Long performanceId, CatalogWriter.PerformanceRow row) {
        catalogWriter.updatePerformance(performanceId, row);
        return performanceId;
    }

    private CatalogWriter.PerformanceRow toPerformanceRow(KopisResponse.PerformanceDetail detail) {
        return new CatalogWriter.PerformanceRow(
                detail.title(),
                genreMapper.toGenre(detail.genre()),
                detail.venueName(),
                runtimeParser.parseMinutes(detail.runtime()),
                detail.posterUrl()
        );
    }

    private ShowSync.Upsert toUpsert(Long performanceId, KopisResponse.PerformanceDetail detail) {
        List<PriceParser.SeatPrice> seatPrices = priceParser.parse(detail.price());
        OptionalInt lowestPrice = priceParser.minPrice(seatPrices);
        Integer minPrice = lowestPrice.isPresent() ? lowestPrice.getAsInt() : null;
        return new ShowSync.Upsert(
                performanceId,
                detail.kopisId(),
                resolveVenueId(detail.kopisVenueId()),
                detail.genre(),
                detail.startDate(),
                detail.endDate(),
                detail.openRun(),
                detail.synopsis(),
                detail.age(),
                ageParser.parseMinAge(detail.age()),
                detail.runtime(),
                detail.timeGuide(),
                detail.price(),
                minPrice,
                seatPrices.stream()
                        .map(price -> new ShowSync.SeatPrice(price.grade(), price.price()))
                        .toList(),
                detail.bookingLinks().stream()
                        .map(link -> new ShowSync.BookingLink(link.name(), link.url()))
                        .toList(),
                detail.introImageUrls(),
                detail.updatedAt()
        );
    }

    private Long resolveVenueId(String kopisVenueId) {
        if (kopisVenueId == null || kopisVenueId.isBlank()) {
            return null;
        }
        return venueService.findIdByKopisVenueId(kopisVenueId)
                .or(() -> kopisClient.fetchFacility(kopisVenueId)
                        .map(facility -> venueService.create(new VenueSync.Create(
                                facility.kopisVenueId(),
                                facility.name(),
                                facility.address(),
                                facility.latitude(),
                                facility.longitude()
                        ))))
                .orElse(null);
    }

    private LocalDate sessionStart(LocalDate startDate) {
        LocalDate syncStart = kopisProperties.sync().startDate();
        return startDate.isBefore(syncStart) ? syncStart : startDate;
    }

    private LocalDate sessionEnd(LocalDate endDate, LocalDate today) {
        LocalDate limit = today.plusMonths(kopisProperties.sync().futureMonths());
        return endDate.isAfter(limit) ? limit : endDate;
    }

    public enum ImportResult {
        CREATED,
        UPDATED,
        SKIPPED
    }
}
