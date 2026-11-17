package com.fourthon.backend.internal.event;

public record TicketChangedEvent(
        Long userId,
        Long ticketId,
        TicketChangeType type
) {
}
