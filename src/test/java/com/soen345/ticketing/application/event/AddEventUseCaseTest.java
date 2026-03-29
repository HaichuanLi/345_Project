package com.soen345.ticketing.application.event;

import com.soen345.ticketing.application.auth.ValidationException;
import com.soen345.ticketing.application.usecase.event.AddEventUseCase;
import com.soen345.ticketing.domain.event.Event;
import com.soen345.ticketing.domain.event.EventStatus;
import com.soen345.ticketing.infrastructure.persistence.inmemory.InMemoryEventRepository;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class AddEventUseCaseTest {
    private final InMemoryEventRepository eventRepository = new InMemoryEventRepository();
    private final AddEventUseCase addEventUseCase = new AddEventUseCase(eventRepository);

    private final UUID organizerId = UUID.randomUUID();
    private final LocalDateTime start = LocalDateTime.of(2026, 8, 1, 9, 0);
    private final LocalDateTime end = LocalDateTime.of(2026, 8, 1, 17, 0);

    private AddEventCommand validCommand() {
        return new AddEventCommand(
                "EVT-NEW", "Test Event", "Technology", "A great event",
                "Main Hall", start, end, 100, 49.99, organizerId
        );
    }

    @Test
    void createsEventSuccessfully() {
        Event event = addEventUseCase.execute(validCommand());

        assertNotNull(event.id());
        assertEquals("EVT-NEW", event.eventCode());
        assertEquals("Test Event", event.title());
        assertEquals("Technology", event.category());
        assertEquals("Main Hall", event.venue());
        assertEquals(100, event.capacity());
        assertEquals(100, event.availableTickets());
        assertEquals(EventStatus.PUBLISHED, event.status());
        assertEquals(49.99, event.price());
        assertTrue(eventRepository.findById(event.id()).isPresent());
    }

    @Test
    void rejectsNullCommand() {
        assertThrows(ValidationException.class, () -> addEventUseCase.execute(null));
    }

    @Test
    void rejectsBlankEventCode() {
        AddEventCommand cmd = new AddEventCommand(
                "", "Title", "Cat", "Desc", "Venue", start, end, 10, 5.0, organizerId
        );
        ValidationException ex = assertThrows(ValidationException.class, () -> addEventUseCase.execute(cmd));
        assertEquals("Event code must not be blank", ex.getMessage());
    }

    @Test
    void rejectsBlankTitle() {
        AddEventCommand cmd = new AddEventCommand(
                "CODE", "", "Cat", "Desc", "Venue", start, end, 10, 5.0, organizerId
        );
        ValidationException ex = assertThrows(ValidationException.class, () -> addEventUseCase.execute(cmd));
        assertEquals("Title must not be blank", ex.getMessage());
    }

    @Test
    void rejectsBlankCategory() {
        AddEventCommand cmd = new AddEventCommand(
                "CODE", "Title", "", "Desc", "Venue", start, end, 10, 5.0, organizerId
        );
        ValidationException ex = assertThrows(ValidationException.class, () -> addEventUseCase.execute(cmd));
        assertEquals("Category must not be blank", ex.getMessage());
    }

    @Test
    void rejectsBlankDescription() {
        AddEventCommand cmd = new AddEventCommand(
                "CODE", "Title", "Cat", "", "Venue", start, end, 10, 5.0, organizerId
        );
        ValidationException ex = assertThrows(ValidationException.class, () -> addEventUseCase.execute(cmd));
        assertEquals("Description must not be blank", ex.getMessage());
    }

    @Test
    void rejectsBlankVenue() {
        AddEventCommand cmd = new AddEventCommand(
                "CODE", "Title", "Cat", "Desc", "", start, end, 10, 5.0, organizerId
        );
        ValidationException ex = assertThrows(ValidationException.class, () -> addEventUseCase.execute(cmd));
        assertEquals("Location must not be blank", ex.getMessage());
    }

    @Test
    void rejectsNullStartTime() {
        AddEventCommand cmd = new AddEventCommand(
                "CODE", "Title", "Cat", "Desc", "Venue", null, end, 10, 5.0, organizerId
        );
        ValidationException ex = assertThrows(ValidationException.class, () -> addEventUseCase.execute(cmd));
        assertEquals("Start time must not be blank", ex.getMessage());
    }

    @Test
    void rejectsNullEndTime() {
        AddEventCommand cmd = new AddEventCommand(
                "CODE", "Title", "Cat", "Desc", "Venue", start, null, 10, 5.0, organizerId
        );
        ValidationException ex = assertThrows(ValidationException.class, () -> addEventUseCase.execute(cmd));
        assertEquals("End time must not be blank", ex.getMessage());
    }

    @Test
    void rejectsEndTimeBeforeStartTime() {
        LocalDateTime earlyEnd = start.minusHours(1);
        AddEventCommand cmd = new AddEventCommand(
                "CODE", "Title", "Cat", "Desc", "Venue", start, earlyEnd, 10, 5.0, organizerId
        );
        ValidationException ex = assertThrows(ValidationException.class, () -> addEventUseCase.execute(cmd));
        assertEquals("End time must be after start time", ex.getMessage());
    }

    @Test
    void rejectsZeroSeats() {
        AddEventCommand cmd = new AddEventCommand(
                "CODE", "Title", "Cat", "Desc", "Venue", start, end, 0, 5.0, organizerId
        );
        ValidationException ex = assertThrows(ValidationException.class, () -> addEventUseCase.execute(cmd));
        assertEquals("Available seats must be greater than zero", ex.getMessage());
    }

    @Test
    void rejectsNegativePrice() {
        AddEventCommand cmd = new AddEventCommand(
                "CODE", "Title", "Cat", "Desc", "Venue", start, end, 10, -1.0, organizerId
        );
        ValidationException ex = assertThrows(ValidationException.class, () -> addEventUseCase.execute(cmd));
        assertEquals("Price must not be negative", ex.getMessage());
    }

    @Test
    void rejectsNullOrganizerId() {
        AddEventCommand cmd = new AddEventCommand(
                "CODE", "Title", "Cat", "Desc", "Venue", start, end, 10, 5.0, null
        );
        ValidationException ex = assertThrows(ValidationException.class, () -> addEventUseCase.execute(cmd));
        assertEquals("Organizer ID must not be null", ex.getMessage());
    }

    @Test
    void acceptsZeroPrice() {
        AddEventCommand cmd = new AddEventCommand(
                "CODE", "Title", "Cat", "Desc", "Venue", start, end, 10, 0.0, organizerId
        );
        Event event = addEventUseCase.execute(cmd);
        assertEquals(0.0, event.price());
    }
}
