package com.soen345.ticketing.infrastructure.email;

import com.soen345.ticketing.application.reservation.ReservationConfirmation;
import com.soen345.ticketing.application.reservation.ReservationEmailService;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Objects;
import java.util.Properties;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class SmtpReservationEmailService implements ReservationEmailService {

    private static final DateTimeFormatter DISPLAY_DATE_TIME =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm", Locale.CANADA)
                    .withZone(ZoneId.systemDefault());

    private final SmtpConfig config;

    public SmtpReservationEmailService(SmtpConfig config) {
        this.config = Objects.requireNonNull(config, "config must not be null");
    }

    @Override
    public void sendReservationConfirmation(String recipientEmail, ReservationConfirmation confirmation) {
        Objects.requireNonNull(recipientEmail, "recipientEmail must not be null");
        if (recipientEmail.isBlank()) {
            throw new IllegalArgumentException("recipientEmail must not be blank");
        }
        Objects.requireNonNull(confirmation, "confirmation must not be null");

        String subject = "Reservation Confirmation – " + confirmation.eventDetails().title();
        String body = buildEmailBody(confirmation);

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", config.host());
        props.put("mail.smtp.port", String.valueOf(config.port()));

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(config.username(), config.password());
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(config.fromAddress()));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
            message.setSubject(subject);
            message.setText(body);
            Transport.send(message);
        } catch (MessagingException e) {
            throw new EmailSendException("Failed to send reservation confirmation email", e);
        }
    }

    private String buildEmailBody(ReservationConfirmation c) {
        double orderTotal = c.quantityReserved() * c.eventDetails().price();

        return "=== RESERVATION CONFIRMATION ===\n\n"
                + "Reservation ID: " + c.reservationId() + "\n\n"
                + "--- Event Details ---\n"
                + "Event Title: " + c.eventDetails().title() + "\n"
                + "Event Code: " + c.eventDetails().eventCode() + "\n"
                + "Category: " + c.eventDetails().category() + "\n"
                + "Venue: " + c.eventDetails().venue() + "\n"
                + "Start: " + c.eventDetails().startDateTime() + "\n"
                + "End: " + c.eventDetails().endDateTime() + "\n\n"
                + "--- Reservation Details ---\n"
                + "Number of Tickets: " + c.quantityReserved() + "\n"
                + "Order Total: " + String.format(Locale.CANADA, "$%.2f", orderTotal) + "\n"
                + "Reserved At: " + DISPLAY_DATE_TIME.format(c.reservedAt()) + "\n\n"
                + "==============================\n";
    }
}
