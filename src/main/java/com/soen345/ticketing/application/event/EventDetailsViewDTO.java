package com.soen345.ticketing.application.event;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public record EventDetailsViewDTO(
        UUID eventId,
        String eventCode,
        String eventName,
        String category,
        String fullDescription,
        String venue,
        LocalDateTime startDateTime,
        LocalDateTime endDateTime,
        int totalCapacity,
        int availableSeats,
        int ticketsLeft,
        double pricePerTicket
) {
    public EventDetailsViewDTO {
        Objects.requireNonNull(eventId, "eventId must not be null");
        Objects.requireNonNull(eventCode, "eventCode must not be null");
        Objects.requireNonNull(eventName, "eventName must not be null");
        Objects.requireNonNull(category, "category must not be null");
        Objects.requireNonNull(fullDescription, "fullDescription must not be null");
        Objects.requireNonNull(venue, "venue must not be null");
        Objects.requireNonNull(startDateTime, "startDateTime must not be null");
        Objects.requireNonNull(endDateTime, "endDateTime must not be null");

        if (availableSeats < 0) {
            throw new IllegalArgumentException("availableSeats must not be negative");
        }
        if (pricePerTicket < 0) {
            throw new IllegalArgumentException("pricePerTicket must not be negative");
        }
    }

    /**
     * Creates EventDetailsViewDTO from domain Event
     */
    public static EventDetailsViewDTO fromEvent(com.soen345.ticketing.domain.event.Event event) {
        return new EventDetailsViewDTO(
                event.id(),
                event.eventCode(),
                event.title(),
                event.category(),
                event.description(),
                event.venue(),
                event.startDateTime(),
                event.endDateTime(),
                event.capacity(),
                event.availableTickets(),
                event.availableTickets(),
                event.price()
        );
    }

    /**
     * Calculate order total for given quantity
     */
    public double calculateOrderTotal(int quantity) {
        if (quantity < 0) {
            throw new IllegalArgumentException("quantity must not be negative");
        }
        return pricePerTicket * quantity;
    }

    /**
     * Check if quantity is available
     */
    public boolean hasAvailableSeats(int quantity) {
        return quantity > 0 && quantity <= availableSeats;
    }
}
