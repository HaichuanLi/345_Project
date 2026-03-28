package com.soen345.ticketing.android;

import com.soen345.ticketing.domain.event.Event;

public class EventSelectionHandler {
    private Event selectedEvent;

    public Event onEventClick(Event event) {
        selectedEvent = event;
        return selectedEvent;
    }

    public Event getSelectedEvent() {
        return selectedEvent;
    }
}
