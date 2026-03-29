package com.soen345.ticketing.application.reservation;

import com.soen345.ticketing.application.auth.ValidationException;
import com.soen345.ticketing.application.usecase.reservation.CancelReservationUseCase;
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

class CancelReservationUseCaseTest {
    private final InMemoryEventRepository eventRepository = new InMemoryEventRepository();
    private final InMemoryReservationRepository reservationRepository = new InMemoryReservationRepository();
    private final CancelReservationUseCase useCase =
            new CancelReservationUseCase(reservationRepository, eventRepository);

    private Event saveEvent(int capacity, int availableTickets) {
        Event event = new Event(
                UUID.randomUUID(), "EVT-001", "Test Event", "Technology",
                "Description", "Venue",
                LocalDateTime.of(2026, 8, 1, 9, 0),
                LocalDateTime.of(2026, 8, 1, 17, 0),
                capacity, availableTickets, UUID.randomUUID(), EventStatus.PUBLISHED, 50.0
        );
        return eventRepository.save(event);
    }

    private Reservation saveConfirmedReservation(UUID eventId, int quantity) {
        Reservation reservation = new Reservation(
                UUID.randomUUID(), eventId, UUID.randomUUID(), quantity,
                Instant.now(), ReservationStatus.CONFIRMED
        );
        return reservationRepository.save(reservation);
    }

    @Test
    void cancelsReservationSuccessfully() {
        Event event = saveEvent(100, 90);
        Reservation reservation = saveConfirmedReservation(event.id(), 10);

        useCase.execute(reservation.id());

        Reservation fromRepo = reservationRepository.findById(reservation.id()).orElseThrow();
        assertEquals(ReservationStatus.CANCELLED, fromRepo.status());
    }

    @Test
    void restoresAvailableTicketsAfterCancellation() {
        Event event = saveEvent(100, 90);
        Reservation reservation = saveConfirmedReservation(event.id(), 10);

        useCase.execute(reservation.id());

        Event updatedEvent = eventRepository.findById(event.id()).orElseThrow();
        assertEquals(100, updatedEvent.availableTickets());
    }

    @Test
    void ticketRestorationDoesNotExceedCapacity() {
        Event event = saveEvent(100, 98);
        Reservation reservation = saveConfirmedReservation(event.id(), 10);

        useCase.execute(reservation.id());

        Event updatedEvent = eventRepository.findById(event.id()).orElseThrow();
        assertEquals(100, updatedEvent.availableTickets());
    }

    @Test
    void removesReservationFromUserList() {
        Event event = saveEvent(100, 90);
        UUID customerId = UUID.randomUUID();
        Reservation reservation = new Reservation(
                UUID.randomUUID(), event.id(), customerId, 5,
                Instant.now(), ReservationStatus.CONFIRMED
        );
        reservationRepository.save(reservation);

        useCase.execute(reservation.id());

        long confirmedCount = reservationRepository.findByCustomerId(customerId).stream()
                .filter(r -> r.status() == ReservationStatus.CONFIRMED)
                .count();
        assertEquals(0, confirmedCount);
    }

    @Test
    void rejectsNullReservationId() {
        ValidationException ex = assertThrows(ValidationException.class,
                () -> useCase.execute(null));
        assertEquals("Reservation ID must not be null", ex.getMessage());
    }

    @Test
    void rejectsNonExistentReservation() {
        ValidationException ex = assertThrows(ValidationException.class,
                () -> useCase.execute(UUID.randomUUID()));
        assertEquals("Reservation not found", ex.getMessage());
    }

    @Test
    void rejectsAlreadyCancelledReservation() {
        Event event = saveEvent(100, 90);
        Reservation reservation = new Reservation(
                UUID.randomUUID(), event.id(), UUID.randomUUID(), 5,
                Instant.now(), ReservationStatus.CANCELLED
        );
        reservationRepository.save(reservation);

        ValidationException ex = assertThrows(ValidationException.class,
                () -> useCase.execute(reservation.id()));
        assertEquals("Reservation is already cancelled", ex.getMessage());
    }

    @Test
    void handlesMultipleCancellationsForSameEvent() {
        Event event = saveEvent(100, 80);
        Reservation res1 = saveConfirmedReservation(event.id(), 10);
        Reservation res2 = saveConfirmedReservation(event.id(), 10);

        useCase.execute(res1.id());
        useCase.execute(res2.id());

        Event updatedEvent = eventRepository.findById(event.id()).orElseThrow();
        assertEquals(100, updatedEvent.availableTickets());
    }

    @Test
    void preservesOtherReservationFields() {
        Event event = saveEvent(100, 90);
        Reservation reservation = saveConfirmedReservation(event.id(), 5);

        useCase.execute(reservation.id());

        Reservation fromRepo = reservationRepository.findById(reservation.id()).orElseThrow();
        assertEquals(reservation.eventId(), fromRepo.eventId());
        assertEquals(reservation.customerId(), fromRepo.customerId());
        assertEquals(reservation.quantity(), fromRepo.quantity());
        assertEquals(reservation.reservedAt(), fromRepo.reservedAt());
    }
}
