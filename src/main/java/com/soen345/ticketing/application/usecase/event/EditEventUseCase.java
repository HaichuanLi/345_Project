package com.soen345.ticketing.application.usecase.event;

import com.soen345.ticketing.application.auth.ValidationException;
import com.soen345.ticketing.application.event.EditEventCommand;
import com.soen345.ticketing.domain.event.Event;
import com.soen345.ticketing.domain.event.EventRepository;
import com.soen345.ticketing.domain.event.EventStatus;
import com.soen345.ticketing.domain.reservation.ReservationRepository;
import com.soen345.ticketing.domain.reservation.ReservationStatus;

import java.util.UUID;

public class EditEventUseCase {
    private final EventRepository eventRepository;
    private final ReservationRepository reservationRepository;

    public EditEventUseCase(EventRepository eventRepository, ReservationRepository reservationRepository) {
        this.eventRepository = eventRepository;
        this.reservationRepository = reservationRepository;
    }

    public Event execute(EditEventCommand command) {
        validate(command);

        Event existing = eventRepository.findById(command.eventId())
                .orElseThrow(() -> new ValidationException("Event not found"));

        if (existing.status() == EventStatus.CANCELLED) {
            throw new ValidationException("Cannot edit a cancelled event");
        }

        int reservedTickets = reservationRepository.findByEventId(command.eventId()).stream()
                .filter(r -> r.status() == ReservationStatus.CONFIRMED || r.status() == ReservationStatus.PENDING)
                .mapToInt(r -> r.quantity())
                .sum();

        if (command.capacity() < reservedTickets) {
            throw new ValidationException(
                    "Capacity cannot be less than reserved tickets (" + reservedTickets + ")"
            );
        }

        int newAvailableTickets = command.capacity() - reservedTickets;

        Event updated = new Event(
                existing.id(),
                command.eventCode().trim(),
                command.title().trim(),
                command.category().trim(),
                command.description().trim(),
                command.venue().trim(),
                command.startDateTime(),
                command.endDateTime(),
                command.capacity(),
                newAvailableTickets,
                existing.organizerId(),
                existing.status(),
                command.price()
        );

        return eventRepository.save(updated);
    }

    private void validate(EditEventCommand command) {
        if (command == null) {
            throw new ValidationException("Edit event request must not be null");
        }
        if (command.eventId() == null) {
            throw new ValidationException("Event ID must not be null");
        }
        if (command.eventCode() == null || command.eventCode().isBlank()) {
            throw new ValidationException("Event code must not be blank");
        }
        if (command.title() == null || command.title().isBlank()) {
            throw new ValidationException("Title must not be blank");
        }
        if (command.category() == null || command.category().isBlank()) {
            throw new ValidationException("Category must not be blank");
        }
        if (command.description() == null || command.description().isBlank()) {
            throw new ValidationException("Description must not be blank");
        }
        if (command.venue() == null || command.venue().isBlank()) {
            throw new ValidationException("Location must not be blank");
        }
        if (command.startDateTime() == null) {
            throw new ValidationException("Start time must not be blank");
        }
        if (command.endDateTime() == null) {
            throw new ValidationException("End time must not be blank");
        }
        if (!command.endDateTime().isAfter(command.startDateTime())) {
            throw new ValidationException("End time must be after start time");
        }
        if (command.capacity() <= 0) {
            throw new ValidationException("Available seats must be greater than zero");
        }
        if (command.price() < 0) {
            throw new ValidationException("Price must not be negative");
        }
    }
}
