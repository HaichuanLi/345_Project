package com.soen345.ticketing.application.usecase.event;

import com.soen345.ticketing.application.event.EventDetailsViewDTO;
import com.soen345.ticketing.application.event.TicketOrderDTO;
import com.soen345.ticketing.domain.event.EventRepository;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class CalculateEventTicketOrderUseCase {
    private final EventRepository eventRepository;

    public CalculateEventTicketOrderUseCase(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    /**
     * Calculate ticket order for a given event and quantity
     */
    public Optional<TicketOrderDTO> calculateOrder(UUID eventId, int quantityRequested) {
        Objects.requireNonNull(eventId, "eventId must not be null");

        return eventRepository.findById(eventId)
                .map(event -> TicketOrderDTO.create(
                        quantityRequested,
                        event.price(),
                        event.availableTickets()
                ));
    }

    /**
     * Calculate ticket order or throw exception if event not found
     */
    public TicketOrderDTO calculateOrderOrThrow(UUID eventId, int quantityRequested) {
        return calculateOrder(eventId, quantityRequested)
                .orElseThrow(() -> new EventNotFoundException(eventId));
    }

    /**
     * Get order validation error if any
     */
    public Optional<String> validateOrder(UUID eventId, int quantityRequested) {
        return calculateOrder(eventId, quantityRequested)
                .map(TicketOrderDTO::getValidationError);
    }

    /**
     * Check if order is valid
     */
    public boolean isOrderValid(UUID eventId, int quantityRequested) {
        return calculateOrder(eventId, quantityRequested)
                .map(TicketOrderDTO::isValid)
                .orElse(false);
    }

    private static class EventNotFoundException extends RuntimeException {
        EventNotFoundException(UUID eventId) {
            super("Event not found with id: " + eventId);
        }
    }
}
