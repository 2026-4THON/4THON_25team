package com.fourthon.backend.show.domain;

import com.fourthon.backend.show.dto.ShowSync;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(
        name = "show_details",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_show_details_kopis_id",
                columnNames = "kopis_id"
        )
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ShowDetail {

    @Id
    @Column(name = "performance_id")
    private Long performanceId;

    @Column(name = "kopis_id", nullable = false, length = 20)
    private String kopisId;

    @Column(name = "venue_id")
    private Long venueId;

    @Column(length = 50)
    private String kopisGenre;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Column(nullable = false)
    private boolean openRun;

    @Column(columnDefinition = "TEXT")
    private String synopsis;

    @Column(length = 100)
    private String ageText;

    @Column(nullable = false)
    private int minAge;

    @Column(length = 100)
    private String runtimeText;

    @Column(length = 1000)
    private String timeGuide;

    @Column(length = 1000)
    private String priceText;

    private Integer minPrice;

    @ElementCollection
    @CollectionTable(name = "show_prices", joinColumns = @JoinColumn(name = "performance_id"))
    @OrderColumn(name = "sort_order")
    private List<ShowSeatPrice> prices = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "show_booking_links", joinColumns = @JoinColumn(name = "performance_id"))
    @OrderColumn(name = "sort_order")
    private List<ShowBookingLink> bookingLinks = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "show_intro_images", joinColumns = @JoinColumn(name = "performance_id"))
    @OrderColumn(name = "sort_order")
    @Column(name = "image_url", nullable = false, length = 1000)
    private List<String> introImageUrls = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "show_keywords", joinColumns = @JoinColumn(name = "performance_id"))
    @OrderColumn(name = "sort_order")
    @Column(name = "keyword", nullable = false, length = 30)
    private List<String> keywords = new ArrayList<>();

    private LocalDateTime analyzedAt;

    @Column(nullable = false)
    private int analysisFailCount;

    private LocalDateTime kopisUpdatedAt;

    @Column(nullable = false)
    private LocalDateTime syncedAt;

    public ShowDetail(ShowSync.Upsert command, LocalDateTime syncedAt) {
        this.performanceId = command.performanceId();
        this.kopisId = command.kopisId();
        apply(command, syncedAt);
    }

    public void update(ShowSync.Upsert command, LocalDateTime syncedAt) {
        apply(command, syncedAt);
    }

    public void completeAnalysis(List<String> newKeywords, LocalDateTime analyzedAt) {
        keywords.clear();
        keywords.addAll(newKeywords);
        this.analyzedAt = analyzedAt;
        this.analysisFailCount = 0;
    }

    public void failAnalysis() {
        analysisFailCount++;
    }

    private void apply(ShowSync.Upsert command, LocalDateTime syncedAt) {
        if (!Objects.equals(this.synopsis, command.synopsis())) {
            this.analyzedAt = null;
            this.analysisFailCount = 0;
        }
        this.venueId = command.venueId();
        this.kopisGenre = command.kopisGenre();
        this.startDate = command.startDate();
        this.endDate = command.endDate();
        this.openRun = command.openRun();
        this.synopsis = command.synopsis();
        this.ageText = command.ageText();
        this.minAge = command.minAge();
        this.runtimeText = command.runtimeText();
        this.timeGuide = command.timeGuide();
        this.priceText = command.priceText();
        this.minPrice = command.minPrice();
        this.kopisUpdatedAt = command.kopisUpdatedAt();
        this.syncedAt = syncedAt;
        replacePrices(command.prices());
        replaceBookingLinks(command.bookingLinks());
        replaceIntroImages(command.introImageUrls());
    }

    private void replacePrices(List<ShowSync.SeatPrice> newPrices) {
        prices.clear();
        newPrices.forEach(price -> prices.add(new ShowSeatPrice(price.grade(), price.price())));
    }

    private void replaceBookingLinks(List<ShowSync.BookingLink> newLinks) {
        bookingLinks.clear();
        newLinks.forEach(link -> bookingLinks.add(new ShowBookingLink(link.name(), link.url())));
    }

    private void replaceIntroImages(List<String> newImageUrls) {
        introImageUrls.clear();
        introImageUrls.addAll(newImageUrls);
    }
}
