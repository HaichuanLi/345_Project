package com.soen345.ticketing.application.reservation;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record ReservationConfirmation(
        UUID reservationId,
        UUID customerId,
        int quantityReserved,
        EventDetailsDTO eventDetails,
        Instant reservedAt,
        String reservationStatus
) implements Serializable {
    private static final long serialVersionUID = 1L;

    public ReservationConfirmation {
        Objects.requireNonNull(reservationId, "reservationId must not be null");
        Objects.requireNonNull(customerId, "customerId must not be null");
        Objects.requireNonNull(eventDetails, "eventDetails must not be null");
        Objects.requireNonNull(reservedAt, "reservedAt must not be null");
        Objects.requireNonNull(reservationStatus, "reservationStatus must not be null");

        if (quantityReserved <= 0) {
            throw new IllegalArgumentException("quantityReserved must be greater than zero");
        }
    }

    @Override
    public String toString() {
        return "=== RESERVATION CONFIRMATION ===\n" +
                "Reservation ID: " + reservationId + "\n" +
                "Status: " + reservationStatus + "\n" +
                "Reserved At: " + reservedAt + "\n" +
                "\n--- Event Details ---\n" +
                "Event Code: " + eventDetails.eventCode() + "\n" +
                "Event Title: " + eventDetails.title() + "\n" +
                "Category: " + eventDetails.category() + "\n" +
                "Venue: " + eventDetails.venue() + "\n" +
                "Start: " + eventDetails.startDateTime() + "\n" +
                "End: " + eventDetails.endDateTime() + "\n" +
                "Description: " + eventDetails.description() + "\n" +
                "\n--- Reservation Details ---\n" +
                "Number of Tickets: " + quantityReserved + "\n" +
                "Available Capacity: " + eventDetails.capacity() + "\n" +
                "Remaining Seats: " + eventDetails.availableTickets() + "\n" +
                "==============================";
    }
}
