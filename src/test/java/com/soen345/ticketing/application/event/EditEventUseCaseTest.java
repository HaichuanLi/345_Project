package com.soen345.ticketing.application.event;

import com.soen345.ticketing.application.auth.ValidationException;
import com.soen345.ticketing.application.usecase.event.EditEventUseCase;
import com.soen345.ticketing.domain.event.Event;
import com.soen345.ticketing.domain.event.EventStatus;
import com.soen345.ticketing.domain.reservation.Reservation;
import com.soen345.ticketing.domain.reservation.ReservationStatus;
import com.soen345.ticketing.infrastructure.persistence.inmemory.InMemoryEventRepository;
import com.soen345.ticketing.infrastructure.persistence.inmemory.InMemoryReservationRepository;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class EditEventUseCaseTest {
    private final InMemoryEventRepository eventRepository = new InMemoryEventRepository();
    private final InMemoryReservationRepository reservationRepository = new InMemoryReservationRepository();
    private final EditEventUseCase editEventUseCase = new EditEventUseCase(eventRepository, reservationRepository);

    private final LocalDateTime start = LocalDateTime.of(2026, 8, 1, 9, 0);
    private final LocalDateTime end = LocalDateTime.of(2026, 8, 1, 17, 0);

    private Event savePublishedEvent(int capacity) {
        Event event = new Event(
                UUID.randomUUID(), "EVT-001", "Original Title", "Technology",
                "Description", "Venue", start, end, capacity, capacity,
                UUID.randomUUID(), EventStatus.PUBLISHED, 50.0
        );
        return eventRepository.save(event);
    }

    private EditEventCommand editCommand(UUID eventId, String title, int capacity, double price) {
        return new EditEventCommand(
                eventId, "EVT-001", title, "Technology",
                "Updated description", "New Venue",
                start, end, capacity, price
        );
    }

    @Test
    void editsEventSuccessfully() {
        Event original = savePublishedEvent(100);

        Event edited = editEventUseCase.execute(
                editCommand(original.id(), "Updated Title", 200, 75.0)
        );

        assertEquals("Updated Title", edited.title());
        assertEquals("New Venue", edited.venue());
        assertEquals(200, edited.capacity());
        assertEquals(200, edited.availableTickets());
        assertEquals(75.0, edited.price());
    }

    @Test
    void updatesAvailableTicketsBasedOnReservations() {
        Event original = savePublishedEvent(100);

        // Simulate 30 tickets reserved
        reservationRepository.save(new Reservation(
                UUID.randomUUID(), original.id(), UUID.randomUUID(),
                30, Instant.now(), ReservationStatus.CONFIRMED
        ));

        Event edited = editEventUseCase.execute(
                editCommand(original.id(), "Title", 150, 50.0)
        );

        assertEquals(150, edited.capacity());
        assertEquals(120, edited.availableTickets()); // 150 - 30 reserved
    }

    @Test
    void rejectsCapacityBelowReservedTickets() {
        Event original = savePublishedEvent(100);

        reservationRepository.save(new Reservation(
                UUID.randomUUID(), original.id(), UUID.randomUUID(),
                60, Instant.now(), ReservationStatus.CONFIRMED
        ));

        ValidationException ex = assertThrows(ValidationException.class,
                () -> editEventUseCase.execute(
                        editCommand(original.id(), "Title", 50, 50.0)
                ));
        assertTrue(ex.getMessage().contains("Capacity cannot be less than reserved tickets"));
    }

    @Test
    void ignoresCancelledReservationsInSeatCount() {
        Event original = savePublishedEvent(100);

        reservationRepository.save(new Reservation(
                UUID.randomUUID(), original.id(), UUID.randomUUID(),
                40, Instant.now(), ReservationStatus.CANCELLED
        ));
        reservationRepository.save(new Reservation(
                UUID.randomUUID(), original.id(), UUID.randomUUID(),
                20, Instant.now(), ReservationStatus.CONFIRMED
        ));

        Event edited = editEventUseCase.execute(
                editCommand(original.id(), "Title", 50, 50.0)
        );

        assertEquals(50, edited.capacity());
        assertEquals(30, edited.availableTickets()); // 50 - 20 (cancelled ignored)
    }

    @Test
    void rejectsEditingCancelledEvent() {
        Event event = new Event(
                UUID.randomUUID(), "EVT-001", "Title", "Cat",
                "Desc", "Venue", start, end, 100, 100,
                UUID.randomUUID(), EventStatus.CANCELLED, 50.0
        );
        eventRepository.save(event);

        ValidationException ex = assertThrows(ValidationException.class,
                () -> editEventUseCase.execute(
                        editCommand(event.id(), "New Title", 100, 50.0)
                ));
        assertEquals("Cannot edit a cancelled event", ex.getMessage());
    }

    @Test
    void rejectsNonExistentEvent() {
        ValidationException ex = assertThrows(ValidationException.class,
                () -> editEventUseCase.execute(
                        editCommand(UUID.randomUUID(), "Title", 100, 50.0)
                ));
        assertEquals("Event not found", ex.getMessage());
    }

    @Test
    void rejectsNullCommand() {
        assertThrows(ValidationException.class, () -> editEventUseCase.execute(null));
    }

    @Test
    void rejectsBlankTitle() {
        Event original = savePublishedEvent(100);
        ValidationException ex = assertThrows(ValidationException.class,
                () -> editEventUseCase.execute(
                        editCommand(original.id(), "", 100, 50.0)
                ));
        assertEquals("Title must not be blank", ex.getMessage());
    }

    @Test
    void rejectsZeroCapacity() {
        Event original = savePublishedEvent(100);
        ValidationException ex = assertThrows(ValidationException.class,
                () -> editEventUseCase.execute(
                        editCommand(original.id(), "Title", 0, 50.0)
                ));
        assertEquals("Available seats must be greater than zero", ex.getMessage());
    }

    @Test
    void rejectsNegativePrice() {
        Event original = savePublishedEvent(100);
        ValidationException ex = assertThrows(ValidationException.class,
                () -> editEventUseCase.execute(
                        editCommand(original.id(), "Title", 100, -5.0)
                ));
        assertEquals("Price must not be negative", ex.getMessage());
    }

    @Test
    void rejectsEndTimeBeforeStartTime() {
        Event original = savePublishedEvent(100);
        EditEventCommand cmd = new EditEventCommand(
                original.id(), "EVT-001", "Title", "Cat",
                "Desc", "Venue", end, start, 100, 50.0
        );
        ValidationException ex = assertThrows(ValidationException.class,
                () -> editEventUseCase.execute(cmd));
        assertEquals("End time must be after start time", ex.getMessage());
    }
}
