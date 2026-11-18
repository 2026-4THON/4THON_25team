package com.fourthon.backend.taste.event;

public record TasteReportSavedEvent(
        Long userId,
        boolean updated
) {
}
