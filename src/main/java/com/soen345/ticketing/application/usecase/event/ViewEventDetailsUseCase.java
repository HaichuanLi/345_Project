package com.soen345.ticketing.application.usecase.event;

import com.soen345.ticketing.application.event.EventDetailsViewDTO;
import com.soen345.ticketing.domain.event.EventRepository;

import java.util.Optional;
import java.util.UUID;

public class ViewEventDetailsUseCase {
    private final EventRepository eventRepository;

    public ViewEventDetailsUseCase(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    /**
     * Retrieve complete event details for display
     */
    public Optional<EventDetailsViewDTO> getEventDetails(UUID eventId) {
        return eventRepository.findById(eventId)
                .map(EventDetailsViewDTO::fromEvent);
    }

    /**
     * Retrieve event details and validate it exists
     */
    public EventDetailsViewDTO getEventDetailsOrThrow(UUID eventId) {
        return getEventDetails(eventId)
                .orElseThrow(() -> new EventNotFoundException(eventId));
    }

    private static class EventNotFoundException extends RuntimeException {
        EventNotFoundException(UUID eventId) {
            super("Event not found with id: " + eventId);
        }
    }
}
