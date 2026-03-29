package com.soen345.ticketing.application.reservation;

import java.util.Objects;
import java.util.UUID;

public record ReserveTicketsCommand(
        UUID userId,
        UUID eventId,
        int quantity
) {
    public ReserveTicketsCommand {
        Objects.requireNonNull(userId, "userId must not be null");
        Objects.requireNonNull(eventId, "eventId must not be null");
    }
}
