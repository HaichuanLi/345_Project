package com.soen345.ticketing.application.reservation;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record ReservationConfirmationPageContent(
        UUID reservationId,
        EventDetailsDTO eventDetails,
        int ticketsReserved,
        double totalPrice,
        Instant reservationTime
) {
    public ReservationConfirmationPageContent {
        Objects.requireNonNull(reservationId, "reservationId must not be null");
        Objects.requireNonNull(eventDetails, "eventDetails must not be null");
        Objects.requireNonNull(reservationTime, "reservationTime must not be null");

        if (ticketsReserved <= 0) {
            throw new IllegalArgumentException("ticketsReserved must be greater than zero");
        }
        if (totalPrice < 0) {
            throw new IllegalArgumentException("totalPrice must not be negative");
        }
    }

    public static ReservationConfirmationPageContent from(ReservationConfirmation confirmation) {
        Objects.requireNonNull(confirmation, "confirmation must not be null");

        double computedTotalPrice = confirmation.quantityReserved() * confirmation.eventDetails().price();
        return new ReservationConfirmationPageContent(
                confirmation.reservationId(),
                confirmation.eventDetails(),
                confirmation.quantityReserved(),
                computedTotalPrice,
                confirmation.reservedAt()
        );
    }
}
