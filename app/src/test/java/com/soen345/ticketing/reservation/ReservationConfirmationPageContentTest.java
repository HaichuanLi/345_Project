package com.soen345.ticketing.reservation;

import com.soen345.ticketing.application.reservation.EventDetailsDTO;
import com.soen345.ticketing.application.reservation.ReservationConfirmation;
import com.soen345.ticketing.application.reservation.ReservationConfirmationPageContent;
import org.junit.Test;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ReservationConfirmationPageContentTest {

    @Test
    public void ReservationConfirmationPageContent_ShouldIncludeReservationId() {
        ReservationConfirmation confirmation = sampleConfirmation(2, 59.50);

        ReservationConfirmationPageContent content = ReservationConfirmationPageContent.from(confirmation);

        assertEquals(confirmation.reservationId(), content.reservationId());
    }

    @Test
    public void ReservationConfirmationPageContent_ShouldIncludeEventDetails() {
        ReservationConfirmation confirmation = sampleConfirmation(2, 59.50);

        ReservationConfirmationPageContent content = ReservationConfirmationPageContent.from(confirmation);

        assertNotNull(content.eventDetails());
        assertEquals(confirmation.eventDetails().eventCode(), content.eventDetails().eventCode());
        assertEquals(confirmation.eventDetails().title(), content.eventDetails().title());
        assertEquals(confirmation.eventDetails().venue(), content.eventDetails().venue());
        assertEquals(confirmation.eventDetails().category(), content.eventDetails().category());
    }

    @Test
    public void ReservationConfirmationPageContent_ShouldIncludeNumberOfTicketsReserved() {
        ReservationConfirmation confirmation = sampleConfirmation(3, 59.50);

        ReservationConfirmationPageContent content = ReservationConfirmationPageContent.from(confirmation);

        assertEquals(3, content.ticketsReserved());
    }

    @Test
    public void ReservationConfirmationPageContent_ShouldIncludeTotalPrice() {
        ReservationConfirmation confirmation = sampleConfirmation(4, 45.25);

        ReservationConfirmationPageContent content = ReservationConfirmationPageContent.from(confirmation);

        assertEquals(181.00, content.totalPrice(), 0.001);
    }

    @Test
    public void ReservationConfirmationPageContent_ShouldIncludeReservationTime() {
        ReservationConfirmation confirmation = sampleConfirmation(1, 99.00);

        ReservationConfirmationPageContent content = ReservationConfirmationPageContent.from(confirmation);

        assertEquals(confirmation.reservedAt(), content.reservationTime());
    }

    private ReservationConfirmation sampleConfirmation(int quantity, double pricePerTicket) {
        EventDetailsDTO eventDetails = new EventDetailsDTO(
                UUID.randomUUID(),
                "EVT-TRFR015-001",
                "Confirmation Test Event",
                "Technology",
                "Event for TR-FR-015 unit tests",
                "Montreal Convention Center",
                LocalDateTime.of(2026, 6, 1, 10, 0),
                LocalDateTime.of(2026, 6, 1, 12, 0),
                300,
                296,
                pricePerTicket
        );

        return new ReservationConfirmation(
                UUID.randomUUID(),
                UUID.randomUUID(),
                quantity,
                eventDetails,
                Instant.parse("2026-03-28T18:30:00Z"),
                "CONFIRMED"
        );
    }
}
