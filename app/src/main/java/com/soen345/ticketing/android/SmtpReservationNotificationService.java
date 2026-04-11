package com.soen345.ticketing.android;

import com.soen345.ticketing.application.reservation.ReservationConfirmation;
import com.soen345.ticketing.application.reservation.ReservationNotificationService;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class SmtpReservationNotificationService implements ReservationNotificationService {
    private static final DateTimeFormatter DISPLAY_DATE_TIME =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm", Locale.CANADA)
                    .withZone(ZoneId.systemDefault());

    @Override
    public void sendReservationConfirmation(String recipientEmail, ReservationConfirmation confirmation) {
        if (recipientEmail == null || recipientEmail.isBlank() || !isSmtpConfigured()) {
            return;
        }

        try {
            Session session = Session.getInstance(smtpProperties(), new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(BuildConfig.SMTP_USERNAME, BuildConfig.SMTP_PASSWORD);
                }
            });

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(BuildConfig.SMTP_FROM));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail, false));
            message.setSubject("Event Subscription Confirmation - " + confirmation.eventDetails().title());
            message.setText(buildEmailBody(confirmation));

            Transport.send(message);
        } catch (MessagingException ex) {
            throw new RuntimeException("Failed to send reservation confirmation email", ex);
        }
    }

    private boolean isSmtpConfigured() {
        return notBlank(BuildConfig.SMTP_HOST)
                && notBlank(BuildConfig.SMTP_USERNAME)
                && notBlank(BuildConfig.SMTP_PASSWORD)
                && notBlank(BuildConfig.SMTP_FROM)
                && BuildConfig.SMTP_PORT > 0;
    }

    private Properties smtpProperties() {
        Properties props = new Properties();
        props.put("mail.smtp.host", BuildConfig.SMTP_HOST);
        props.put("mail.smtp.port", String.valueOf(BuildConfig.SMTP_PORT));
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", String.valueOf(BuildConfig.SMTP_TLS));
        return props;
    }

    private String buildEmailBody(ReservationConfirmation confirmation) {
        double orderTotal = confirmation.quantityReserved() * confirmation.eventDetails().price();

        return "Reservation Confirmation\n\n"
                + "Reservation ID: " + confirmation.reservationId() + "\n"
                + "Event Name: " + confirmation.eventDetails().title() + "\n"
                + "Event Code: " + confirmation.eventDetails().eventCode() + "\n"
                + "Category: " + confirmation.eventDetails().category() + "\n"
                + "Location: " + confirmation.eventDetails().venue() + "\n"
                + "Start Time: " + confirmation.eventDetails().startDateTime() + "\n"
                + "End Time: " + confirmation.eventDetails().endDateTime() + "\n"
                + "Number of Tickets Reserved: " + confirmation.quantityReserved() + "\n"
                + "Order Total: " + String.format(Locale.CANADA, "$%.2f", orderTotal) + "\n"
                + "Reserved At: " + DISPLAY_DATE_TIME.format(confirmation.reservedAt()) + "\n";
    }

    private boolean notBlank(String value) {
        return value != null && !value.isBlank();
    }
}
