package com.soen345.ticketing.application.reservation;

import com.soen345.ticketing.application.auth.ValidationException;
import com.soen345.ticketing.application.usecase.reservation.GetUserReservationsUseCase;
import com.soen345.ticketing.application.usecase.reservation.UserReservationDTO;
import com.soen345.ticketing.domain.event.Event;
import com.soen345.ticketing.domain.event.EventStatus;
import com.soen345.ticketing.domain.reservation.Reservation;
import com.soen345.ticketing.domain.reservation.ReservationStatus;
import com.soen345.ticketing.infrastructure.persistence.inmemory.InMemoryEventRepository;
import com.soen345.ticketing.infrastructure.persistence.inmemory.InMemoryReservationRepository;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class GetUserReservationsUseCaseTest {
    private final InMemoryEventRepository eventRepository = new InMemoryEventRepository();
    private final InMemoryReservationRepository reservationRepository = new InMemoryReservationRepository();
    private final GetUserReservationsUseCase useCase =
            new GetUserReservationsUseCase(reservationRepository, eventRepository);

    private Event saveEvent(String title, double price, int capacity) {
        Event event = new Event(
                UUID.randomUUID(), "EVT-" + UUID.randomUUID().toString().substring(0, 6),
                title, "Technology", "Description", "Venue",
                LocalDateTime.of(2026, 8, 1, 9, 0),
                LocalDateTime.of(2026, 8, 1, 17, 0),
                capacity, capacity, UUID.randomUUID(), EventStatus.PUBLISHED, price
        );
        return eventRepository.save(event);
    }

    private Reservation saveReservation(UUID customerId, UUID eventId, int quantity, ReservationStatus status) {
        Reservation reservation = new Reservation(
                UUID.randomUUID(), eventId, customerId, quantity, Instant.now(), status
        );
        return reservationRepository.save(reservation);
    }

    @Test
    void returnsReservationsForUserWithMultipleReservations() {
        UUID userId = UUID.randomUUID();
        Event event1 = saveEvent("Event 1", 50.0, 100);
        Event event2 = saveEvent("Event 2", 75.0, 200);
        saveReservation(userId, event1.id(), 2, ReservationStatus.CONFIRMED);
        saveReservation(userId, event2.id(), 3, ReservationStatus.CONFIRMED);

        List<UserReservationDTO> result = useCase.execute(userId);

        assertEquals(2, result.size());
    }

    @Test
    void returnsEmptyListForUserWithNoReservations() {
        UUID userId = UUID.randomUUID();

        List<UserReservationDTO> result = useCase.execute(userId);

        assertTrue(result.isEmpty());
    }

    @Test
    void excludesCancelledReservations() {
        UUID userId = UUID.randomUUID();
        Event event = saveEvent("Event", 50.0, 100);
        saveReservation(userId, event.id(), 2, ReservationStatus.CONFIRMED);
        saveReservation(userId, event.id(), 1, ReservationStatus.CANCELLED);

        List<UserReservationDTO> result = useCase.execute(userId);

        assertEquals(1, result.size());
    }

    @Test
    void doesNotReturnReservationsFromOtherUsers() {
        UUID userId = UUID.randomUUID();
        UUID otherUserId = UUID.randomUUID();
        Event event = saveEvent("Event", 50.0, 100);
        saveReservation(otherUserId, event.id(), 2, ReservationStatus.CONFIRMED);

        List<UserReservationDTO> result = useCase.execute(userId);

        assertTrue(result.isEmpty());
    }

    @Test
    void returnsCorrectEventDetails() {
        UUID userId = UUID.randomUUID();
        Event event = saveEvent("Tech Conference", 99.99, 100);
        saveReservation(userId, event.id(), 2, ReservationStatus.CONFIRMED);

        List<UserReservationDTO> result = useCase.execute(userId);

        assertEquals(1, result.size());
        UserReservationDTO dto = result.get(0);
        assertEquals("Tech Conference", dto.eventName());
        assertEquals(event.eventCode(), dto.eventCode());
        assertEquals("Technology", dto.category());
        assertEquals("Venue", dto.venue());
        assertEquals(event.startDateTime(), dto.startDateTime());
        assertEquals(event.endDateTime(), dto.endDateTime());
    }

    @Test
    void calculatesTicketsPurchasedAndTotalPrice() {
        UUID userId = UUID.randomUUID();
        Event event = saveEvent("Event", 25.0, 100);
        saveReservation(userId, event.id(), 4, ReservationStatus.CONFIRMED);

        List<UserReservationDTO> result = useCase.execute(userId);

        assertEquals(1, result.size());
        UserReservationDTO dto = result.get(0);
        assertEquals(4, dto.ticketsPurchased());
        assertEquals(25.0, dto.pricePerTicket(), 0.001);
        assertEquals(100.0, dto.totalPrice(), 0.001);
    }

    @Test
    void rejectsNullUserId() {
        ValidationException ex = assertThrows(ValidationException.class,
                () -> useCase.execute(null));
        assertEquals("User ID must not be null", ex.getMessage());
    }

    @Test
    void excludesPendingReservations() {
        UUID userId = UUID.randomUUID();
        Event event = saveEvent("Event", 50.0, 100);
        saveReservation(userId, event.id(), 2, ReservationStatus.PENDING);

        List<UserReservationDTO> result = useCase.execute(userId);

        assertTrue(result.isEmpty());
    }

    @Test
    void handlesDeletedEventGracefully() {
        UUID userId = UUID.randomUUID();
        UUID fakeEventId = UUID.randomUUID();
        saveReservation(userId, fakeEventId, 2, ReservationStatus.CONFIRMED);

        List<UserReservationDTO> result = useCase.execute(userId);

        assertTrue(result.isEmpty());
    }
}
