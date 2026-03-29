package com.soen345.ticketing.application.reservation;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public record EventDetailsDTO(
        UUID eventId,
        String eventCode,
        String title,
        String category,
        String description,
        String venue,
        LocalDateTime startDateTime,
        LocalDateTime endDateTime,
        int capacity,
        int availableTickets,
        double price
) implements Serializable {
    private static final long serialVersionUID = 1L;

    public EventDetailsDTO {
        Objects.requireNonNull(eventId, "eventId must not be null");
        Objects.requireNonNull(eventCode, "eventCode must not be null");
        Objects.requireNonNull(title, "title must not be null");
        Objects.requireNonNull(category, "category must not be null");
        Objects.requireNonNull(description, "description must not be null");
        Objects.requireNonNull(venue, "venue must not be null");
        Objects.requireNonNull(startDateTime, "startDateTime must not be null");
        Objects.requireNonNull(endDateTime, "endDateTime must not be null");
    }

    public static EventDetailsDTO fromEvent(com.soen345.ticketing.domain.event.Event event) {
        return new EventDetailsDTO(
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
                event.price()
        );
    }
}
