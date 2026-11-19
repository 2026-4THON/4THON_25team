package com.fourthon.backend.show.dto;

import java.time.LocalDate;
import java.util.List;

public record ShowVideoQuery(
        String title,
        String venueName,
        List<String> actorNames,
        LocalDate startDate
) {
}
