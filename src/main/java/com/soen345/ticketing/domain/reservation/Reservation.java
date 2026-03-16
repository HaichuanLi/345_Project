package com.soen345.ticketing.domain.reservation;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record Reservation(
        UUID id,
        UUID eventId,
        UUID customerId,
        int quantity,
        Instant reservedAt,
        ReservationStatus status,
        String confirmationCode
) {
    public Reservation {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(eventId, "eventId must not be null");
        Objects.requireNonNull(customerId, "customerId must not be null");
        Objects.requireNonNull(reservedAt, "reservedAt must not be null");
        Objects.requireNonNull(status, "status must not be null");
        Objects.requireNonNull(confirmationCode, "confirmationCode must not be null");

        if (quantity <= 0) {
            throw new IllegalArgumentException("quantity must be greater than zero");
        }
    }
}
