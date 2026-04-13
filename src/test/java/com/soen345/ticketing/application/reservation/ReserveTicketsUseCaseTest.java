package com.soen345.ticketing.application.reservation;

import com.soen345.ticketing.application.auth.ValidationException;
import com.soen345.ticketing.application.usecase.reservation.ReserveTicketsUseCase;
import com.soen345.ticketing.domain.Notifications.NotificationService;
import com.soen345.ticketing.domain.event.Event;
import com.soen345.ticketing.domain.event.EventStatus;
import com.soen345.ticketing.domain.reservation.Reservation;
import com.soen345.ticketing.domain.reservation.ReservationStatus;
import com.soen345.ticketing.domain.user.Role;
import com.soen345.ticketing.domain.user.User;
import com.soen345.ticketing.domain.user.UserRepository;
import com.soen345.ticketing.domain.user.UserStatus;
import com.soen345.ticketing.infrastructure.persistence.inmemory.InMemoryEventRepository;
import com.soen345.ticketing.infrastructure.persistence.inmemory.InMemoryReservationRepository;
import com.soen345.ticketing.infrastructure.persistence.inmemory.InMemoryUserRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;

class ReserveTicketsUseCaseTest {
    private final InMemoryEventRepository eventRepository = new InMemoryEventRepository();
    private final InMemoryReservationRepository reservationRepository = new InMemoryReservationRepository();
    private final RecordingConfirmationService confirmationService = new RecordingConfirmationService();
    private final NotificationService notificationService = Mockito.mock(NotificationService.class);
    private final UserRepository userRepository = new InMemoryUserRepository();
    private final ReserveTicketsUseCase useCase = new ReserveTicketsUseCase(
            eventRepository,
            reservationRepository,
            confirmationService,
            new ReserveTicketsValidator(),
            notificationService,
            userRepository
    );

    @Test
    void reservesTicketsAndStoresConfirmation() {
        Event event = saveEvent(10, 10, EventStatus.PUBLISHED);
        UUID userId = UUID.randomUUID();
        User user = new User(userId, "Test User", "test@example.com", "1234567890", "hash", Role.CUSTOMER, UserStatus.ACTIVE);
        userRepository.save(user);

        ReservationConfirmation confirmation = useCase.reserve(
                new ReserveTicketsCommand(userId, event.id(), 3)
        );

        Event updatedEvent = eventRepository.findById(event.id()).orElseThrow();
        Reservation savedReservation = reservationRepository.findById(confirmation.reservationId()).orElseThrow();

        assertEquals(7, updatedEvent.availableTickets());
        assertEquals(ReservationStatus.CONFIRMED, savedReservation.status());
        assertEquals(userId, confirmation.customerId());
        assertEquals(3, confirmation.quantityReserved());
        assertEquals(event.id(), confirmation.eventDetails().eventId());
        assertEquals(7, confirmation.eventDetails().availableTickets());
        assertTrue(confirmationService.getConfirmation(confirmation.reservationId()).isPresent());
        
        verify(notificationService).sendConfirmation(anyString(), anyString(), anyString());
    }

    @Test
    void allowsReservingTheLastAvailableSeat() {
        Event event = saveEvent(5, 1, EventStatus.PUBLISHED);

        ReservationConfirmation confirmation = useCase.reserve(
                new ReserveTicketsCommand(UUID.randomUUID(), event.id(), 1)
        );

        Event updatedEvent = eventRepository.findById(event.id()).orElseThrow();

        assertNotNull(confirmation.reservationId());
        assertEquals(0, updatedEvent.availableTickets());
        assertEquals(0, confirmation.eventDetails().availableTickets());
    }

    @Test
    void rejectsUnknownEvent() {
        UUID missingEventId = UUID.randomUUID();

        EventNotFoundException exception = assertThrows(
                EventNotFoundException.class,
                () -> useCase.reserve(new ReserveTicketsCommand(UUID.randomUUID(), missingEventId, 1))
        );

        assertEquals(missingEventId, exception.getEventId());
    }

    @Test
    void rejectsCancelledEvent() {
        Event event = saveEvent(10, 10, EventStatus.CANCELLED);

        EventNotFoundException exception = assertThrows(
                EventNotFoundException.class,
                () -> useCase.reserve(new ReserveTicketsCommand(UUID.randomUUID(), event.id(), 1))
        );

        assertEquals(event.id(), exception.getEventId());
    }

    @Test
    void rejectsReservationAboveAllowedBatchSize() {
        Event event = saveEvent(500, 500, EventStatus.PUBLISHED);

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> useCase.reserve(new ReserveTicketsCommand(UUID.randomUUID(), event.id(), 101))
        );

        assertEquals("Cannot reserve more than 100 tickets at once", exception.getMessage());
    }

    @Test
    void rejectsWhenNotEnoughSeatsRemain() {
        Event event = saveEvent(10, 2, EventStatus.PUBLISHED);

        InsufficientSeatsException exception = assertThrows(
                InsufficientSeatsException.class,
                () -> useCase.reserve(new ReserveTicketsCommand(UUID.randomUUID(), event.id(), 3))
        );

        assertEquals(3, exception.getRequestedQuantity());
        assertEquals(2, exception.getAvailableSeats());
    }

    private Event saveEvent(int capacity, int availableTickets, EventStatus status) {
        Event event = new Event(
                UUID.randomUUID(),
                "EVT-RES-" + UUID.randomUUID().toString().substring(0, 8),
                "Reservation Test Event",
                "Technology",
                "Event used for reservation use case tests",
                "Test Venue",
                LocalDateTime.of(2026, 10, 1, 9, 0),
                LocalDateTime.of(2026, 10, 1, 12, 0),
                capacity,
                availableTickets,
                UUID.randomUUID(),
                status,
                42.50
        );
        return eventRepository.save(event);
    }

    private static class RecordingConfirmationService implements ReservationConfirmationService {
        private final Map<UUID, ReservationConfirmation> store = new HashMap<>();

        @Override
        public void saveConfirmation(ReservationConfirmation confirmation) {
            store.put(confirmation.reservationId(), confirmation);
        }

        @Override
        public Optional<ReservationConfirmation> getConfirmation(UUID reservationId) {
            return Optional.ofNullable(store.get(reservationId));
        }
    }
}
