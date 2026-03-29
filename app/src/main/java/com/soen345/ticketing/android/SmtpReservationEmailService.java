package com.soen345.ticketing.android;

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
    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm", Locale.CANADA)
                    .withZone(ZoneId.systemDefault());

    private final String host;
    private final String port;
    private final String username;
    private final String password;
    private final String from;
    private final boolean tlsEnabled;

    public SmtpReservationEmailService(
            String host,
            String port,
            String username,
            String password,
            String from,
            boolean tlsEnabled
    ) {
        this.host = safeTrim(host);
        this.port = safeTrim(port);
        this.username = safeTrim(username);
        this.password = password == null ? "" : password;
        this.from = safeTrim(from);
        this.tlsEnabled = tlsEnabled;
    }

    @Override
    public void sendReservationConfirmation(String recipientEmail, ReservationConfirmation confirmation) {
        Objects.requireNonNull(recipientEmail, "recipientEmail must not be null");
        Objects.requireNonNull(confirmation, "confirmation must not be null");

        String recipient = safeTrim(recipientEmail);
        if (recipient.isEmpty()) {
            throw new IllegalArgumentException("recipientEmail must not be empty");
        }

        validateConfiguration();

        Properties props = new Properties();
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", port);
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", String.valueOf(tlsEnabled));
        props.put("mail.smtp.starttls.required", String.valueOf(tlsEnabled));

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipient));
            message.setSubject("Reservation Confirmation - " + confirmation.reservationId());
            message.setText(buildEmailBody(confirmation));
            Transport.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send reservation confirmation email", e);
        }
    }

    private void validateConfiguration() {
        if (host.isEmpty() || port.isEmpty() || username.isEmpty() || password.isEmpty() || from.isEmpty()) {
            throw new IllegalStateException(
                    "SMTP configuration is incomplete. Set smtp.host, smtp.port, smtp.username, smtp.password, smtp.from in local.properties"
            );
        }
    }

    private String buildEmailBody(ReservationConfirmation confirmation) {
        double totalPrice = confirmation.quantityReserved() * confirmation.eventDetails().price();

        return "Hello,\n\n"
                + "Your reservation is complete.\n\n"
                + "Reservation ID: " + confirmation.reservationId() + "\n"
                + "Event Name: " + confirmation.eventDetails().title() + "\n"
                + "Event Code: " + confirmation.eventDetails().eventCode() + "\n"
                + "Category: " + confirmation.eventDetails().category() + "\n"
                + "Location: " + confirmation.eventDetails().venue() + "\n"
                + "Start Time: " + confirmation.eventDetails().startDateTime() + "\n"
                + "End Time: " + confirmation.eventDetails().endDateTime() + "\n"
                + "Number of Tickets Reserved: " + confirmation.quantityReserved() + "\n"
                + "Total Price: " + String.format(Locale.CANADA, "$%.2f", totalPrice) + "\n"
                + "Reservation Time: " + DATE_TIME_FORMATTER.format(confirmation.reservedAt()) + "\n\n"
                + "Thank you.";
    }

    private String safeTrim(String value) {
        return value == null ? "" : value.trim();
    }
}
