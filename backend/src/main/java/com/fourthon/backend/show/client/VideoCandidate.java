package com.fourthon.backend.show.client;

import java.time.LocalDate;

public record VideoCandidate(
        String videoId,
        String title,
        String channelTitle,
        LocalDate publishedDate
) {
}
