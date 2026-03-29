package com.soen345.ticketing.application.usecase.event;

import com.soen345.ticketing.application.auth.ValidationException;
import com.soen345.ticketing.application.event.AddEventCommand;
import com.soen345.ticketing.domain.event.Event;
import com.soen345.ticketing.domain.event.EventRepository;
import com.soen345.ticketing.domain.event.EventStatus;

import java.util.UUID;

public class AddEventUseCase {
    private final EventRepository eventRepository;

    public AddEventUseCase(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    public Event execute(AddEventCommand command) {
        validate(command);

        Event event = new Event(
                UUID.randomUUID(),
                command.eventCode().trim(),
                command.title().trim(),
                command.category().trim(),
                command.description().trim(),
                command.venue().trim(),
                command.startDateTime(),
                command.endDateTime(),
                command.capacity(),
                command.capacity(),
                command.organizerId(),
                EventStatus.PUBLISHED,
                command.price()
        );

        return eventRepository.save(event);
    }

    private void validate(AddEventCommand command) {
        if (command == null) {
            throw new ValidationException("Add event request must not be null");
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
        if (command.organizerId() == null) {
            throw new ValidationException("Organizer ID must not be null");
        }
    }
}
