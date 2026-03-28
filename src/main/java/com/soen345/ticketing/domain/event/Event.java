package com.soen345.ticketing.domain.event;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public record Event(
        UUID id,
        String eventCode,
        String title,
        String category,
        String description,
        String venue,
        LocalDateTime startDateTime,
        LocalDateTime endDateTime,
        int capacity,
        int availableTickets,
        UUID organizerId,
        EventStatus status,
        double price
) {
    public Event {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(eventCode, "eventCode must not be null");
        Objects.requireNonNull(title, "title must not be null");
        Objects.requireNonNull(category, "category must not be null");
        Objects.requireNonNull(description, "description must not be null");
        Objects.requireNonNull(venue, "venue must not be null");
        Objects.requireNonNull(startDateTime, "startDateTime must not be null");
        Objects.requireNonNull(endDateTime, "endDateTime must not be null");
        Objects.requireNonNull(organizerId, "organizerId must not be null");
        Objects.requireNonNull(status, "status must not be null");

        if (eventCode.trim().isEmpty()) {
            throw new IllegalArgumentException("eventCode must not be empty");
        }
        if (title.trim().isEmpty()) {
            throw new IllegalArgumentException("title must not be empty");
        }
        if (category.trim().isEmpty()) {
            throw new IllegalArgumentException("category must not be empty");
        }
        if (capacity < 0) {
            throw new IllegalArgumentException("capacity must be zero or greater");
        }
        if (availableTickets < 0 || availableTickets > capacity) {
            throw new IllegalArgumentException("availableTickets must be between 0 and capacity");
        }
        if (price < 0) {
            throw new IllegalArgumentException("price must be zero or greater");
        }
    }
}

