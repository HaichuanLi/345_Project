package com.soen345.ticketing.application.reservation;

import java.util.UUID;

public class EventNotFoundException extends RuntimeException {
    private final UUID eventId;

    public EventNotFoundException(UUID eventId) {
        super(String.format("Event not found with id: %s", eventId));
        this.eventId = eventId;
    }

    public UUID getEventId() {
        return eventId;
    }
}
