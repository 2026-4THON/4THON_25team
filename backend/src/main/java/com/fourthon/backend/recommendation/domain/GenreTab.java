package com.fourthon.backend.recommendation.domain;

import com.fourthon.backend.ticket.domain.CatalogPerformance;

public enum GenreTab {
    ALL(null),
    MUSICAL(CatalogPerformance.Genre.MUSICAL),
    PLAY(CatalogPerformance.Genre.PLAY),
    ETC(CatalogPerformance.Genre.ETC);

    private final CatalogPerformance.Genre genre;

    GenreTab(CatalogPerformance.Genre genre) {
        this.genre = genre;
    }

    public CatalogPerformance.Genre toGenre() {
        return genre;
    }
}
