package com.soen345.ticketing.android;

import com.soen345.ticketing.application.reservation.EventDetailsDTO;
import com.soen345.ticketing.application.reservation.ReservationConfirmation;
import org.junit.Test;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class SmtpReservationEmailServiceTest {

    @Test
    public void SmtpReservationEmailService_SendReservationConfirmation_RejectsEmptyRecipient() {
        SmtpReservationEmailService service = new SmtpReservationEmailService(
                "smtp.example.com",
                "587",
                "sender@example.com",
                "secret",
                "sender@example.com",
                true
        );

        try {
            service.sendReservationConfirmation("", sampleConfirmation());
            fail("Expected IllegalArgumentException for empty recipient email");
        } catch (IllegalArgumentException ex) {
            assertTrue(ex.getMessage().contains("recipientEmail"));
        }
    }

    @Test
    public void SmtpReservationEmailService_SendReservationConfirmation_RejectsBlankRecipient() {
        SmtpReservationEmailService service = new SmtpReservationEmailService(
                "smtp.example.com",
                "587",
                "sender@example.com",
                "secret",
                "sender@example.com",
                true
        );

        try {
            service.sendReservationConfirmation("   ", sampleConfirmation());
            fail("Expected IllegalArgumentException for blank recipient email");
        } catch (IllegalArgumentException ex) {
            assertTrue(ex.getMessage().contains("recipientEmail"));
        }
    }

    @Test
    public void SmtpReservationEmailService_SendReservationConfirmation_RejectsIncompleteConfiguration() {
        SmtpReservationEmailService service = new SmtpReservationEmailService(
                "",
                "587",
                "",
                "",
                "",
                true
        );

        try {
            service.sendReservationConfirmation("customer@example.com", sampleConfirmation());
            fail("Expected IllegalStateException for incomplete SMTP configuration");
        } catch (IllegalStateException ex) {
            assertTrue(ex.getMessage().contains("SMTP configuration is incomplete"));
        }
    }

    @Test
    public void SmtpReservationEmailService_SendReservationConfirmation_RejectsNullRecipient() {
        SmtpReservationEmailService service = new SmtpReservationEmailService(
                "smtp.example.com",
                "587",
                "sender@example.com",
                "secret",
                "sender@example.com",
                true
        );

        try {
            service.sendReservationConfirmation(null, sampleConfirmation());
            fail("Expected NullPointerException for null recipient email");
        } catch (NullPointerException ex) {
            assertTrue(ex.getMessage().contains("recipientEmail"));
        }
    }

    private ReservationConfirmation sampleConfirmation() {
        return new ReservationConfirmation(
                UUID.randomUUID(),
                UUID.randomUUID(),
                2,
                new EventDetailsDTO(
                        UUID.randomUUID(),
                        "EVT-EMAIL-001",
                        "Email Test Event",
                        "Technology",
                        "Event used for SMTP service tests",
                        "Montreal Test Hall",
                        LocalDateTime.of(2026, 4, 10, 10, 0),
                        LocalDateTime.of(2026, 4, 10, 12, 0),
                        100,
                        98,
                        49.99
                ),
                Instant.parse("2026-03-28T15:00:00Z"),
                "CONFIRMED"
        );
    }
}
