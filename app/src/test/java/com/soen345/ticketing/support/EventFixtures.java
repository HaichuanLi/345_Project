package com.soen345.ticketing.support;

import com.soen345.ticketing.domain.event.Event;
import com.soen345.ticketing.domain.event.EventStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public final class EventFixtures {
    private EventFixtures() {
    }

    public static Event publishedEvent(
            String title,
            String eventCode,
            int capacity,
            int availableTickets,
            double price
    ) {
        return new Event(
                UUID.randomUUID(),
                eventCode,
                title,
                "Technology",
                "Test event description with detailed information about the event.",
                "Test Venue",
                LocalDateTime.now().plusDays(7),
                LocalDateTime.now().plusDays(7).plusHours(4),
                capacity,
                availableTickets,
                UUID.randomUUID(),
                EventStatus.PUBLISHED,
                price
        );
    }

    public static Event publishedEvent(String title) {
        return publishedEvent(title, "EVT-" + UUID.randomUUID().toString().substring(0, 8), 100, 50, 99.99);
    }

    public static Event publishedEvent(String title, int availableTickets) {
        return publishedEvent(title, "EVT-" + UUID.randomUUID().toString().substring(0, 8), 100, availableTickets, 99.99);
    }
}
