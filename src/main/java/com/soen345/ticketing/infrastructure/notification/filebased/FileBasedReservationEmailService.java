package com.soen345.ticketing.infrastructure.notification.filebased;

import com.soen345.ticketing.application.reservation.ReservationConfirmation;
import com.soen345.ticketing.application.reservation.ReservationEmailService;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Objects;

public class FileBasedReservationEmailService implements ReservationEmailService {
    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZoneId.systemDefault());

    private final Path outboxDirectory;

    public FileBasedReservationEmailService(String outboxPath) {
        this.outboxDirectory = Paths.get(outboxPath);
        try {
            Files.createDirectories(outboxDirectory);
        } catch (IOException e) {
            throw new EmailDeliveryException("Failed to initialize email outbox directory", e);
        }
    }

    @Override
    public void sendReservationConfirmation(String recipientEmail, ReservationConfirmation confirmation) {
        Objects.requireNonNull(recipientEmail, "recipientEmail must not be null");
        Objects.requireNonNull(confirmation, "confirmation must not be null");

        String safeRecipient = recipientEmail.trim();
        if (safeRecipient.isEmpty()) {
            return;
        }

        String subject = "Reservation Confirmation - " + confirmation.reservationId();
        String body = buildEmailBody(confirmation);

        String fileName = "reservation-" + confirmation.reservationId() + "-to-" + sanitizeFilePart(safeRecipient) + ".email.txt";
        Path emailFile = outboxDirectory.resolve(fileName);
        String emailContent = "TO: " + safeRecipient + System.lineSeparator()
                + "SUBJECT: " + subject + System.lineSeparator()
                + System.lineSeparator()
                + body;

        try {
            Files.writeString(emailFile, emailContent, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new EmailDeliveryException("Failed to store reservation email", e);
        }
    }

    private String buildEmailBody(ReservationConfirmation confirmation) {
        double totalPrice = confirmation.quantityReserved() * confirmation.eventDetails().price();

        return "Hello," + System.lineSeparator()
                + System.lineSeparator()
                + "Your reservation is complete." + System.lineSeparator()
                + "Reservation ID: " + confirmation.reservationId() + System.lineSeparator()
                + System.lineSeparator()
                + "Event Details:" + System.lineSeparator()
                + "- Event Name: " + confirmation.eventDetails().title() + System.lineSeparator()
                + "- Event Code: " + confirmation.eventDetails().eventCode() + System.lineSeparator()
                + "- Category: " + confirmation.eventDetails().category() + System.lineSeparator()
                + "- Location: " + confirmation.eventDetails().venue() + System.lineSeparator()
                + "- Start Time: " + confirmation.eventDetails().startDateTime() + System.lineSeparator()
                + "- End Time: " + confirmation.eventDetails().endDateTime() + System.lineSeparator()
                + System.lineSeparator()
                + "Reservation Details:" + System.lineSeparator()
                + "- Number of Tickets Reserved: " + confirmation.quantityReserved() + System.lineSeparator()
                + "- Total Price: " + String.format(Locale.CANADA, "$%.2f", totalPrice) + System.lineSeparator()
                + "- Reservation Time: " + DATE_TIME_FORMATTER.format(confirmation.reservedAt()) + System.lineSeparator()
                + System.lineSeparator()
                + "Thank you.";
    }

    private String sanitizeFilePart(String input) {
        return input.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}
