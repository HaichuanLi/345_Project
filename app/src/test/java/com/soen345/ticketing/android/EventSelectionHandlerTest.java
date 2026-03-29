package com.soen345.ticketing.android;

import com.soen345.ticketing.domain.event.Event;
import com.soen345.ticketing.domain.event.EventStatus;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

public class EventSelectionHandlerTest {

    @Test
    public void EventSelectionHandler_OnEventClick_ReturnsSelectedEvent() {
        EventSelectionHandler handler = new EventSelectionHandler();
        Event event = new Event(
                UUID.randomUUID(),
                "EVT-UT-001",
                "Unit Test Event",
                "Technology",
                "Event used for unit testing event selection handler",
                "Test Venue",
                LocalDateTime.of(2026, 4, 1, 9, 0),
                LocalDateTime.of(2026, 4, 1, 17, 0),
                100,
                100,
                UUID.randomUUID(),
                EventStatus.PUBLISHED,
                99.99
        );

        Event selected = handler.onEventClick(event);

        assertSame(event, selected);
        assertEquals(event, handler.getSelectedEvent());
    }
}
