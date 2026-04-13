package com.soen345.ticketing.infrastructure.Notifications;

import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class EmailNotificationAdapterTest {

    @Test
    void sendConfirmation_SchedulesEmailWithCorrectDetails() throws MessagingException {
        String username = "test@example.com";
        String password = "password";
        String recipient = "recipient@example.com";
        String subject = "Test Subject";
        String body = "Test Body";

        EmailNotificationAdapter adapter = new EmailNotificationAdapter(username, password);

        try (MockedStatic<Transport> transportMockedStatic = mockStatic(Transport.class)) {
            adapter.sendConfirmation(recipient, subject, body);

            ArgumentCaptor<Message> messageCaptor = ArgumentCaptor.forClass(Message.class);
            transportMockedStatic.verify(() -> Transport.send(messageCaptor.capture()));

            MimeMessage sentMessage = (MimeMessage) messageCaptor.getValue();
            assertEquals(subject, sentMessage.getSubject());
            assertEquals(body, sentMessage.getContent());
            assertEquals(username, ((InternetAddress) sentMessage.getFrom()[0]).getAddress());
            assertEquals(recipient, ((InternetAddress) sentMessage.getRecipients(Message.RecipientType.TO)[0]).getAddress());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void sendConfirmation_DoesNotSendWhenCredentialsAreMissing() {
        EmailNotificationAdapter adapter = new EmailNotificationAdapter("", "");

        try (MockedStatic<Transport> transportMockedStatic = mockStatic(Transport.class)) {
            adapter.sendConfirmation("recipient@example.com", "Subject", "Body");

            transportMockedStatic.verifyNoInteractions();
        }
    }
}
