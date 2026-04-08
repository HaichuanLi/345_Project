package com.soen345.ticketing.application.event;

import com.soen345.ticketing.domain.event.Event;
import com.soen345.ticketing.domain.event.EventStatus;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
public class EventDetailsViewDTOTest {
    private final UUID eventId = UUID.randomUUID();
    private final LocalDateTime start = LocalDateTime.of(2026, 8, 1, 9, 0);
    private final LocalDateTime end = LocalDateTime.of(2026, 8, 1, 17, 0);

    private EventDetailsViewDTO validDTO() {
        return new EventDetailsViewDTO(
                eventId, "EVT-001", "Tech Conference", "Technology",
                "A great event", "Main Hall", start, end,
                100, 80, 80, 49.99
        );
    }

    @Test
    void createsValidDTO() {
        EventDetailsViewDTO dto = validDTO();
        assertEquals(eventId, dto.eventId());
        assertEquals("EVT-001", dto.eventCode());
        assertEquals("Tech Conference", dto.eventName());
        assertEquals(100, dto.totalCapacity());
        assertEquals(80, dto.availableSeats());
        assertEquals(49.99, dto.pricePerTicket());
    }

    @Test
    void rejectsNullEventId() {
        assertThrows(NullPointerException.class, () -> new EventDetailsViewDTO(
                null, "EVT-001", "Name", "Cat", "Desc", "Venue",
                start, end, 100, 80, 80, 49.99
        ));
    }

    @Test
    void rejectsNegativeAvailableSeats() {
        assertThrows(IllegalArgumentException.class, () -> new EventDetailsViewDTO(
                eventId, "EVT-001", "Name", "Cat", "Desc", "Venue",
                start, end, 100, -1, -1, 49.99
        ));
    }

    @Test
    void rejectsNegativePrice() {
        assertThrows(IllegalArgumentException.class, () -> new EventDetailsViewDTO(
                eventId, "EVT-001", "Name", "Cat", "Desc", "Venue",
                start, end, 100, 80, 80, -1.0
        ));
    }

    @Test
    void calculateOrderTotalCorrectly() {
        EventDetailsViewDTO dto = validDTO();
        assertEquals(49.99 * 3, dto.calculateOrderTotal(3), 0.001);
    }

    @Test
    void calculateOrderTotalRejectsNegativeQuantity() {
        EventDetailsViewDTO dto = validDTO();
        assertThrows(IllegalArgumentException.class, () -> dto.calculateOrderTotal(-1));
    }

    @Test
    void hasAvailableSeatsReturnsTrueWhenEnough() {
        EventDetailsViewDTO dto = validDTO();
        assertTrue(dto.hasAvailableSeats(5));
    }

    @Test
    void hasAvailableSeatsReturnsFalseWhenTooMany() {
        EventDetailsViewDTO dto = validDTO();
        assertFalse(dto.hasAvailableSeats(81));
    }

    @Test
    void hasAvailableSeatsReturnsFalseForZero() {
        EventDetailsViewDTO dto = validDTO();
        assertFalse(dto.hasAvailableSeats(0));
    }

    @Test
    void fromEventCreatesCorrectDTO() {
        Event event = new Event(
                eventId, "EVT-001", "Tech Conference", "Technology",
                "A great event", "Main Hall", start, end,
                100, 80, UUID.randomUUID(), EventStatus.PUBLISHED, 49.99
        );

        EventDetailsViewDTO dto = EventDetailsViewDTO.fromEvent(event);

        assertEquals(eventId, dto.eventId());
        assertEquals("EVT-001", dto.eventCode());
        assertEquals("Tech Conference", dto.eventName());
        assertEquals(100, dto.totalCapacity());
        assertEquals(80, dto.availableSeats());
        assertEquals(49.99, dto.pricePerTicket());
    }
}
