package com.soen345.ticketing.domain;

import com.soen345.ticketing.domain.event.Event;
import com.soen345.ticketing.domain.event.EventStatus;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EventTest {
    @Test
    void rejectsBlankEventCode() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> event(" ", "Valid Title", 10, 10, 20.0)
        );

        assertEquals("eventCode must not be empty", exception.getMessage());
    }

    @Test
    void rejectsBlankTitle() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> event("EVT-001", " ", 10, 10, 20.0)
        );

        assertEquals("title must not be empty", exception.getMessage());
    }

    @Test
    void rejectsAvailableTicketsGreaterThanCapacity() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> event("EVT-001", "Capacity Test", 5, 6, 20.0)
        );

        assertEquals("availableTickets must be between 0 and capacity", exception.getMessage());
    }

    @Test
    void rejectsNegativePrice() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> event("EVT-001", "Price Test", 10, 10, -1.0)
        );

        assertEquals("price must be zero or greater", exception.getMessage());
    }

    @Test
    void acceptsZeroPriceAndZeroAvailableTickets() {
        assertDoesNotThrow(() -> event("EVT-001", "Free Event", 10, 0, 0.0));
    }

    private Event event(String code, String title, int capacity, int availableTickets, double price) {
        return new Event(
                UUID.randomUUID(),
                code,
                title,
                "Technology",
                "Domain event test",
                "Test Venue",
                LocalDateTime.of(2026, 12, 1, 9, 0),
                LocalDateTime.of(2026, 12, 1, 12, 0),
                capacity,
                availableTickets,
                UUID.randomUUID(),
                EventStatus.PUBLISHED,
                price
        );
    }
}
