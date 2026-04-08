package com.soen345.ticketing.application.reservation;

import com.soen345.ticketing.application.auth.ValidationException;
import com.soen345.ticketing.domain.event.Event;
import com.soen345.ticketing.domain.event.EventStatus;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
public class ReservationClassesTest {
    private final UUID eventId = UUID.randomUUID();
    private final LocalDateTime start = LocalDateTime.of(2026, 8, 1, 9, 0);
    private final LocalDateTime end = LocalDateTime.of(2026, 8, 1, 17, 0);

    //EventDetailsDTO

    @Test
    void eventDetailsDTOCreatesSuccessfully() {
        EventDetailsDTO dto = new EventDetailsDTO(
                eventId, "EVT-001", "Test Event", "Technology",
                "Description", "Venue", start, end, 100, 80, 49.99
        );
        assertEquals(eventId, dto.eventId());
        assertEquals("EVT-001", dto.eventCode());
        assertEquals(49.99, dto.price());
    }

    @Test
    void eventDetailsDTORejectsNullEventId() {
        assertThrows(NullPointerException.class, () -> new EventDetailsDTO(
                null, "EVT-001", "Title", "Cat", "Desc", "Venue",
                start, end, 100, 80, 49.99
        ));
    }

    @Test
    void eventDetailsDTOFromEventCreatesCorrectly() {
        Event event = new Event(
                eventId, "EVT-001", "Test Event", "Technology",
                "Description", "Venue", start, end,
                100, 80, UUID.randomUUID(), EventStatus.PUBLISHED, 49.99
        );

        EventDetailsDTO dto = EventDetailsDTO.fromEvent(event);

        assertEquals(eventId, dto.eventId());
        assertEquals("EVT-001", dto.eventCode());
        assertEquals("Test Event", dto.title());
        assertEquals(100, dto.capacity());
        assertEquals(80, dto.availableTickets());
    }

    //EventNotFoundException

    @Test
    void eventNotFoundExceptionContainsEventId() {
        UUID id = UUID.randomUUID();
        EventNotFoundException ex = new EventNotFoundException(id);
        assertEquals(id, ex.getEventId());
        assertTrue(ex.getMessage().contains(id.toString()));
    }

    //InsufficientSeatsException

    @Test
    void insufficientSeatsExceptionStoresValues() {
        InsufficientSeatsException ex = new InsufficientSeatsException(10, 3);
        assertEquals(10, ex.getRequestedQuantity());
        assertEquals(3, ex.getAvailableSeats());
        assertTrue(ex.getMessage().contains("10"));
        assertTrue(ex.getMessage().contains("3"));
    }

    //ReservationConfirmation

    private EventDetailsDTO sampleEventDetails() {
        return new EventDetailsDTO(
                eventId, "EVT-001", "Test Event", "Technology",
                "Description", "Venue", start, end, 100, 80, 49.99
        );
    }

    @Test
    void reservationConfirmationCreatesSuccessfully() {
        UUID reservationId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        Instant now = Instant.now();

        ReservationConfirmation confirmation = new ReservationConfirmation(
                reservationId, customerId, 2, sampleEventDetails(), now, "CONFIRMED"
        );

        assertEquals(reservationId, confirmation.reservationId());
        assertEquals(customerId, confirmation.customerId());
        assertEquals(2, confirmation.quantityReserved());
        assertEquals("CONFIRMED", confirmation.reservationStatus());
    }

    @Test
    void reservationConfirmationRejectsNullReservationId() {
        assertThrows(NullPointerException.class, () -> new ReservationConfirmation(
                null, UUID.randomUUID(), 2, sampleEventDetails(), Instant.now(), "CONFIRMED"
        ));
    }

    @Test
    void reservationConfirmationRejectsZeroQuantity() {
        assertThrows(IllegalArgumentException.class, () -> new ReservationConfirmation(
                UUID.randomUUID(), UUID.randomUUID(), 0, sampleEventDetails(), Instant.now(), "CONFIRMED"
        ));
    }

    @Test
    void reservationConfirmationToStringContainsKeyInfo() {
        ReservationConfirmation confirmation = new ReservationConfirmation(
                UUID.randomUUID(), UUID.randomUUID(), 3, sampleEventDetails(), Instant.now(), "CONFIRMED"
        );
        String str = confirmation.toString();
        assertTrue(str.contains("CONFIRMED"));
        assertTrue(str.contains("EVT-001"));
        assertTrue(str.contains("3"));
    }

    //ReserveTicketsCommand

    @Test
    void reserveTicketsCommandCreatesSuccessfully() {
        UUID userId = UUID.randomUUID();
        ReserveTicketsCommand cmd = new ReserveTicketsCommand(userId, eventId, 2);
        assertEquals(userId, cmd.userId());
        assertEquals(eventId, cmd.eventId());
        assertEquals(2, cmd.quantity());
    }

    @Test
    void reserveTicketsCommandRejectsNullUserId() {
        assertThrows(NullPointerException.class,
                () -> new ReserveTicketsCommand(null, eventId, 2));
    }

    @Test
    void reserveTicketsCommandRejectsNullEventId() {
        assertThrows(NullPointerException.class,
                () -> new ReserveTicketsCommand(UUID.randomUUID(), null, 2));
    }

    //ReserveTicketsResult

    @Test
    void reserveTicketsResultCreatesSuccessfully() {
        UUID reservationId = UUID.randomUUID();
        Instant now = Instant.now();
        ReserveTicketsResult result = new ReserveTicketsResult(
                reservationId, eventId, 3, "CONFIRMED", now
        );
        assertEquals(reservationId, result.reservationId());
        assertEquals(eventId, result.eventId());
        assertEquals(3, result.quantity());
        assertEquals("CONFIRMED", result.status());
        assertEquals(now, result.reservedAt());
    }

    @Test
    void reserveTicketsResultRejectsNullReservationId() {
        assertThrows(NullPointerException.class,
                () -> new ReserveTicketsResult(null, eventId, 3, "CONFIRMED", Instant.now()));
    }

    //ReserveTicketsValidator

    @Test
    void validatorAcceptsValidQuantity() {
        ReserveTicketsValidator validator = new ReserveTicketsValidator();
        ReserveTicketsCommand cmd = new ReserveTicketsCommand(UUID.randomUUID(), eventId, 5);
        assertDoesNotThrow(() -> validator.validate(cmd));
    }

    @Test
    void validatorRejectsZeroQuantity() {
        ReserveTicketsValidator validator = new ReserveTicketsValidator();
        ReserveTicketsCommand cmd = new ReserveTicketsCommand(UUID.randomUUID(), eventId, 0);
        ValidationException ex = assertThrows(ValidationException.class,
                () -> validator.validate(cmd));
        assertEquals("Quantity must be greater than zero", ex.getMessage());
    }

    @Test
    void validatorRejectsNegativeQuantity() {
        ReserveTicketsValidator validator = new ReserveTicketsValidator();
        ReserveTicketsCommand cmd = new ReserveTicketsCommand(UUID.randomUUID(), eventId, -1);
        assertThrows(ValidationException.class, () -> validator.validate(cmd));
    }

    @Test
    void validatorRejectsMoreThan100Tickets() {
        ReserveTicketsValidator validator = new ReserveTicketsValidator();
        ReserveTicketsCommand cmd = new ReserveTicketsCommand(UUID.randomUUID(), eventId, 101);
        ValidationException ex = assertThrows(ValidationException.class,
                () -> validator.validate(cmd));
        assertEquals("Cannot reserve more than 100 tickets at once", ex.getMessage());
    }

    @Test
    void validatorAccepts100Tickets() {
        ReserveTicketsValidator validator = new ReserveTicketsValidator();
        ReserveTicketsCommand cmd = new ReserveTicketsCommand(UUID.randomUUID(), eventId, 100);
        assertDoesNotThrow(() -> validator.validate(cmd));
    }

    //SeatAvailabilityService

    @Test
    void checkSeatsReturnsTrueWhenAvailable() {
        SeatAvailabilityService service = new SeatAvailabilityService();
        assertTrue(service.checkSeats(5, 10));
    }

    @Test
    void checkSeatsReturnsTrueWhenExactlyAvailable() {
        SeatAvailabilityService service = new SeatAvailabilityService();
        assertTrue(service.checkSeats(10, 10));
    }

    @Test
    void checkSeatsReturnsFalseWhenInsufficient() {
        SeatAvailabilityService service = new SeatAvailabilityService();
        assertFalse(service.checkSeats(11, 10));
    }

    @Test
    void checkSeatsReturnsFalseWhenZeroRequested() {
        SeatAvailabilityService service = new SeatAvailabilityService();
        assertFalse(service.checkSeats(0, 10));
    }

    @Test
    void checkSeatsReturnsFalseWhenNegativeRequested() {
        SeatAvailabilityService service = new SeatAvailabilityService();
        assertFalse(service.checkSeats(-1, 10));
    }
}
