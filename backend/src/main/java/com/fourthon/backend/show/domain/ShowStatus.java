package com.fourthon.backend.show.domain;

import java.time.LocalDate;

public enum ShowStatus {
    UPCOMING,
    ONGOING,
    ENDED;

    public static ShowStatus of(LocalDate startDate, LocalDate endDate, LocalDate today) {
        if (startDate != null && today.isBefore(startDate)) {
            return UPCOMING;
        }
        if (endDate != null && today.isAfter(endDate)) {
            return ENDED;
        }
        return ONGOING;
    }
}
