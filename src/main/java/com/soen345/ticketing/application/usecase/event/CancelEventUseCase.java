package com.soen345.ticketing.application.usecase.event;

import com.soen345.ticketing.application.auth.ValidationException;
import com.soen345.ticketing.domain.event.Event;
import com.soen345.ticketing.domain.event.EventRepository;
import com.soen345.ticketing.domain.event.EventStatus;

import java.util.UUID;

public class CancelEventUseCase {
    private final EventRepository eventRepository;

    public CancelEventUseCase(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    public Event execute(UUID eventId) {
        if (eventId == null) {
            throw new ValidationException("Event ID must not be null");
        }

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ValidationException("Event not found"));

        if (event.status() == EventStatus.CANCELLED) {
            throw new ValidationException("Event is already cancelled");
        }

        Event cancelled = new Event(
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
                event.organizerId(),
                EventStatus.CANCELLED,
                event.price()
        );

        return eventRepository.save(cancelled);
    }
}
