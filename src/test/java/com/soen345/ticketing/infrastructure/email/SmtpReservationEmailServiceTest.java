package com.soen345.ticketing.infrastructure.email;

import com.soen345.ticketing.application.reservation.EventDetailsDTO;
import com.soen345.ticketing.application.reservation.ReservationConfirmation;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;

class SmtpReservationEmailServiceTest {

    private final SmtpConfig completeConfig = new SmtpConfig(
            "smtp.example.com", 587, "user@example.com", "password", "noreply@example.com"
    );

    private ReservationConfirmation sampleConfirmation() {
        EventDetailsDTO eventDetails = new EventDetailsDTO(
                UUID.randomUUID(), "EVT-001", "Test Event", "Technology",
                "Description", "Venue",
                LocalDateTime.of(2026, 8, 1, 9, 0),
                LocalDateTime.of(2026, 8, 1, 17, 0),
                100, 80, 49.99
        );
        return new ReservationConfirmation(
                UUID.randomUUID(), UUID.randomUUID(), 2, eventDetails, Instant.now(), "CONFIRMED"
        );
    }

    @Test
    void SmtpReservationEmailService_SendReservationConfirmation_RejectsNullRecipient() {
        SmtpReservationEmailService service = new SmtpReservationEmailService(completeConfig);

        assertThrows(NullPointerException.class,
                () -> service.sendReservationConfirmation(null, sampleConfirmation()));
    }

    @Test
    void SmtpReservationEmailService_SendReservationConfirmation_RejectsEmptyRecipient() {
        SmtpReservationEmailService service = new SmtpReservationEmailService(completeConfig);

        assertThrows(IllegalArgumentException.class,
                () -> service.sendReservationConfirmation("", sampleConfirmation()));
    }

    @Test
    void SmtpReservationEmailService_SendReservationConfirmation_RejectsBlankRecipient() {
        SmtpReservationEmailService service = new SmtpReservationEmailService(completeConfig);

        assertThrows(IllegalArgumentException.class,
                () -> service.sendReservationConfirmation("   ", sampleConfirmation()));
    }

    @Test
    void SmtpReservationEmailService_SendReservationConfirmation_RejectsIncompleteConfiguration() {
        assertThrows(IllegalArgumentException.class,
                () -> new SmtpConfig("", 587, "user", "pass", "from@example.com"));
    }
}
