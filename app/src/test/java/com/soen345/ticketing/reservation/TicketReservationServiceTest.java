package com.soen345.ticketing.reservation;

import com.soen345.ticketing.application.reservation.ReserveTicketsCommand;
import com.soen345.ticketing.application.reservation.ReservationConfirmation;
import com.soen345.ticketing.application.reservation.ReservationConfirmationService;
import com.soen345.ticketing.application.reservation.ReserveTicketsValidator;
import com.soen345.ticketing.application.reservation.SeatAvailabilityService;
import com.soen345.ticketing.application.usecase.reservation.ReserveTicketsUseCase;
import com.soen345.ticketing.domain.event.Event;
import com.soen345.ticketing.domain.event.EventStatus;
import com.soen345.ticketing.domain.reservation.Reservation;
import com.soen345.ticketing.infrastructure.email.NoOpReservationEmailService;
import com.soen345.ticketing.infrastructure.persistence.inmemory.InMemoryEventRepository;
import com.soen345.ticketing.infrastructure.persistence.inmemory.InMemoryReservationRepository;
import com.soen345.ticketing.infrastructure.persistence.inmemory.InMemoryUserRepository;
import org.junit.Test;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.Assert.*;

public class TicketReservationServiceTest {

    @Test
    public void TicketReservationService_ReserveSingleTicket_Success() {
        InMemoryEventRepository eventRepository = new InMemoryEventRepository();
        InMemoryReservationRepository reservationRepository = new InMemoryReservationRepository();
        TestConfirmationService confirmationService = new TestConfirmationService();
        ReserveTicketsUseCase useCase = new ReserveTicketsUseCase(
                eventRepository,
                reservationRepository,
                confirmationService,
                new ReserveTicketsValidator(),
                new InMemoryUserRepository(),
                new NoOpReservationEmailService()
        );

        Event event = createEvent(10, 10, 75.0);
        eventRepository.save(event);

        ReservationConfirmation confirmation = useCase.reserve(
                new ReserveTicketsCommand(UUID.randomUUID(), event.id(), 1)
        );

        assertNotNull(confirmation);
        assertEquals(1, confirmation.quantityReserved());
        assertEquals(event.id(), confirmation.eventDetails().eventId());
        Event updated = eventRepository.findById(event.id()).orElseThrow();
        assertEquals(9, updated.availableTickets());
    }

    @Test
    public void TicketReservationService_ReserveMultipleTickets_Success() {
        InMemoryEventRepository eventRepository = new InMemoryEventRepository();
        InMemoryReservationRepository reservationRepository = new InMemoryReservationRepository();
        TestConfirmationService confirmationService = new TestConfirmationService();
        ReserveTicketsUseCase useCase = new ReserveTicketsUseCase(
                eventRepository,
                reservationRepository,
                confirmationService,
                new ReserveTicketsValidator(),
                new InMemoryUserRepository(),
                new NoOpReservationEmailService()
        );

        Event event = createEvent(25, 25, 100.0);
        eventRepository.save(event);

        ReservationConfirmation confirmation = useCase.reserve(
                new ReserveTicketsCommand(UUID.randomUUID(), event.id(), 5)
        );

        assertEquals(5, confirmation.quantityReserved());
        Event updated = eventRepository.findById(event.id()).orElseThrow();
        assertEquals(20, updated.availableTickets());
    }

    @Test(expected = NullPointerException.class)
    public void TicketReservationService_RejectReservation_WhenUserNotLoggedIn() {
        InMemoryEventRepository eventRepository = new InMemoryEventRepository();
        InMemoryReservationRepository reservationRepository = new InMemoryReservationRepository();
        TestConfirmationService confirmationService = new TestConfirmationService();
        ReserveTicketsUseCase useCase = new ReserveTicketsUseCase(
                eventRepository,
                reservationRepository,
                confirmationService,
                new ReserveTicketsValidator(),
                new InMemoryUserRepository(),
                new NoOpReservationEmailService()
        );

        Event event = createEvent(20, 20, 60.0);
        eventRepository.save(event);

        useCase.reserve(new ReserveTicketsCommand(null, event.id(), 2));
    }

    @Test
    public void ReservationRepository_SaveReservation_Success() {
        InMemoryReservationRepository reservationRepository = new InMemoryReservationRepository();

        Reservation reservation = new Reservation(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                2,
                Instant.now(),
                com.soen345.ticketing.domain.reservation.ReservationStatus.CONFIRMED
        );

        Reservation saved = reservationRepository.save(reservation);
        Optional<Reservation> found = reservationRepository.findById(saved.id());

        assertTrue(found.isPresent());
        assertEquals(saved, found.get());
    }

    @Test
    public void ReservationRepository_SaveReservation_CorrectDataStored() {
        InMemoryEventRepository eventRepository = new InMemoryEventRepository();
        InMemoryReservationRepository reservationRepository = new InMemoryReservationRepository();
        TestConfirmationService confirmationService = new TestConfirmationService();
        ReserveTicketsUseCase useCase = new ReserveTicketsUseCase(
                eventRepository,
                reservationRepository,
                confirmationService,
                new ReserveTicketsValidator(),
                new InMemoryUserRepository(),
                new NoOpReservationEmailService()
        );

        Event event = createEvent(30, 30, 120.0);
        eventRepository.save(event);

        ReservationConfirmation confirmation = useCase.reserve(
                new ReserveTicketsCommand(UUID.randomUUID(), event.id(), 3)
        );
        Reservation storedReservation = reservationRepository.findById(confirmation.reservationId()).orElseThrow();

        assertEquals(confirmation.reservationId(), storedReservation.id());
        assertEquals(event.id(), storedReservation.eventId());
        assertEquals(3, storedReservation.quantity());
        assertNotNull(storedReservation.reservedAt());
        assertEquals(360.0, confirmation.quantityReserved() * confirmation.eventDetails().price(), 0.001);
    }

    @Test
    public void SeatAvailabilityService_CheckSeats_ReturnsTrue_WhenSeatsAvailable() {
        SeatAvailabilityService service = new SeatAvailabilityService();
        assertTrue(service.checkSeats(4, 4));
        assertTrue(service.checkSeats(2, 8));
    }

    @Test
    public void SeatAvailabilityService_CheckSeats_ReturnsFalse_WhenInsufficientSeats() {
        SeatAvailabilityService service = new SeatAvailabilityService();
        assertFalse(service.checkSeats(9, 8));
    }

    @Test(expected = com.soen345.ticketing.application.reservation.InsufficientSeatsException.class)
    public void TicketReservationService_PreventReservation_WhenSeatsInsufficient() {
        InMemoryEventRepository eventRepository = new InMemoryEventRepository();
        InMemoryReservationRepository reservationRepository = new InMemoryReservationRepository();
        TestConfirmationService confirmationService = new TestConfirmationService();
        ReserveTicketsUseCase useCase = new ReserveTicketsUseCase(
                eventRepository,
                reservationRepository,
                confirmationService,
                new ReserveTicketsValidator(),
                new InMemoryUserRepository(),
                new NoOpReservationEmailService()
        );

        Event event = createEvent(5, 5, 90.0);
        eventRepository.save(event);

        useCase.reserve(new ReserveTicketsCommand(UUID.randomUUID(), event.id(), 6));
    }

    @Test
    public void TicketReservationService_DecreaseAvailableSeats_AfterReservation() {
        InMemoryEventRepository eventRepository = new InMemoryEventRepository();
        InMemoryReservationRepository reservationRepository = new InMemoryReservationRepository();
        TestConfirmationService confirmationService = new TestConfirmationService();
        ReserveTicketsUseCase useCase = new ReserveTicketsUseCase(
                eventRepository,
                reservationRepository,
                confirmationService,
                new ReserveTicketsValidator(),
                new InMemoryUserRepository(),
                new NoOpReservationEmailService()
        );

        Event event = createEvent(12, 12, 50.0);
        eventRepository.save(event);

        useCase.reserve(new ReserveTicketsCommand(UUID.randomUUID(), event.id(), 4));

        Event updated = eventRepository.findById(event.id()).orElseThrow();
        assertEquals(8, updated.availableTickets());
    }

    @Test
    public void ReservationService_GenerateReservationID_Unique() {
        InMemoryEventRepository eventRepository = new InMemoryEventRepository();
        InMemoryReservationRepository reservationRepository = new InMemoryReservationRepository();
        TestConfirmationService confirmationService = new TestConfirmationService();
        ReserveTicketsUseCase useCase = new ReserveTicketsUseCase(
                eventRepository,
                reservationRepository,
                confirmationService,
                new ReserveTicketsValidator(),
                new InMemoryUserRepository(),
                new NoOpReservationEmailService()
        );

        Event event = createEvent(20, 20, 80.0);
        eventRepository.save(event);

        ReservationConfirmation c1 = useCase.reserve(new ReserveTicketsCommand(UUID.randomUUID(), event.id(), 1));
        ReservationConfirmation c2 = useCase.reserve(new ReserveTicketsCommand(UUID.randomUUID(), event.id(), 1));

        assertNotEquals(c1.reservationId(), c2.reservationId());
    }

    @Test
    public void ReservationService_GenerateReservationID_NotNull() {
        InMemoryEventRepository eventRepository = new InMemoryEventRepository();
        InMemoryReservationRepository reservationRepository = new InMemoryReservationRepository();
        TestConfirmationService confirmationService = new TestConfirmationService();
        ReserveTicketsUseCase useCase = new ReserveTicketsUseCase(
                eventRepository,
                reservationRepository,
                confirmationService,
                new ReserveTicketsValidator(),
                new InMemoryUserRepository(),
                new NoOpReservationEmailService()
        );

        Event event = createEvent(8, 8, 45.0);
        eventRepository.save(event);

        ReservationConfirmation confirmation = useCase.reserve(
                new ReserveTicketsCommand(UUID.randomUUID(), event.id(), 1)
        );

        assertNotNull(confirmation.reservationId());
        assertFalse(confirmation.reservationId().toString().trim().isEmpty());
    }

    private Event createEvent(int capacity, int availableTickets, double price) {
        return new Event(
                UUID.randomUUID(),
                "EVT-TEST-" + UUID.randomUUID().toString().substring(0, 8),
                "Ticket Reservation Test Event",
                "Technology",
                "Event used to test reservation service",
                "Test Hall",
                LocalDateTime.of(2026, 8, 10, 9, 0),
                LocalDateTime.of(2026, 8, 10, 17, 0),
                capacity,
                availableTickets,
                UUID.randomUUID(),
                EventStatus.PUBLISHED,
                price
        );
    }

    private static class TestConfirmationService implements ReservationConfirmationService {
        private ReservationConfirmation lastSaved;

        @Override
        public void saveConfirmation(ReservationConfirmation confirmation) {
            lastSaved = confirmation;
        }

        @Override
        public Optional<ReservationConfirmation> getConfirmation(UUID reservationId) {
            if (lastSaved != null && lastSaved.reservationId().equals(reservationId)) {
                return Optional.of(lastSaved);
            }
            return Optional.empty();
        }
    }
}
