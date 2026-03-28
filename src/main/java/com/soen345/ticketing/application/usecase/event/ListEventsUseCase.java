package com.soen345.ticketing.application.usecase.event;

import com.soen345.ticketing.domain.event.Event;
import com.soen345.ticketing.domain.event.EventRepository;

import java.util.List;

public class ListEventsUseCase {
    private final EventRepository eventRepository;

    public ListEventsUseCase(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    public List<Event> listAvailableEvents() {
        return eventRepository.listAvailable();
    }
}

