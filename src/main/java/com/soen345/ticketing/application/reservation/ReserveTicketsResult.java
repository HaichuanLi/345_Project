package com.soen345.ticketing.application.reservation;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record ReserveTicketsResult(
        UUID reservationId,
        UUID eventId,
        int quantity,
        String status,
        Instant reservedAt
) {
    public ReserveTicketsResult {
        Objects.requireNonNull(reservationId, "reservationId must not be null");
        Objects.requireNonNull(eventId, "eventId must not be null");
        Objects.requireNonNull(status, "status must not be null");
        Objects.requireNonNull(reservedAt, "reservedAt must not be null");
    }
}
