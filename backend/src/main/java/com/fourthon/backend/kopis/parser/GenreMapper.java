package com.fourthon.backend.kopis.parser;

import com.fourthon.backend.ticket.domain.CatalogPerformance;

import org.springframework.stereotype.Component;

@Component
public class GenreMapper {

    private static final String MUSICAL = "뮤지컬";
    private static final String PLAY = "연극";

    public CatalogPerformance.Genre toGenre(String kopisGenre) {
        if (kopisGenre == null) {
            return CatalogPerformance.Genre.ETC;
        }
        if (kopisGenre.contains(MUSICAL)) {
            return CatalogPerformance.Genre.MUSICAL;
        }
        if (kopisGenre.contains(PLAY)) {
            return CatalogPerformance.Genre.PLAY;
        }
        return CatalogPerformance.Genre.ETC;
    }
}
