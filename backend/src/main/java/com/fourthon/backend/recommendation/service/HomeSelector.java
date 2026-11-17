package com.fourthon.backend.recommendation.service;

import com.fourthon.backend.common.domain.TagType;
import com.fourthon.backend.common.domain.TasteTag;
import com.fourthon.backend.recommendation.score.ScoredShow;
import com.fourthon.backend.recommendation.score.TasteMatch;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Component
public class HomeSelector {

    public List<ScoredShow> select(List<ScoredShow> sortedShows, int size, int accurateCount) {
        List<ScoredShow> picked = new ArrayList<>(sortedShows.subList(0, Math.min(accurateCount, sortedShows.size())));
        Set<TasteTag> usedGenres = EnumSet.noneOf(TasteTag.class);
        picked.forEach(show -> dominantGenre(show).ifPresent(usedGenres::add));
        List<ScoredShow> rest = sortedShows.subList(picked.size(), sortedShows.size());
        for (ScoredShow show : rest) {
            if (picked.size() >= size) {
                break;
            }
            Optional<TasteTag> genre = dominantGenre(show);
            if (genre.isPresent() && !usedGenres.contains(genre.get())) {
                picked.add(show);
                usedGenres.add(genre.get());
            }
        }
        for (ScoredShow show : rest) {
            if (picked.size() >= size) {
                break;
            }
            if (!picked.contains(show)) {
                picked.add(show);
            }
        }
        return picked;
    }

    private Optional<TasteTag> dominantGenre(ScoredShow show) {
        return TasteMatch.dominantTag(show.candidate().tagScores(), TagType.GENRE);
    }
}
