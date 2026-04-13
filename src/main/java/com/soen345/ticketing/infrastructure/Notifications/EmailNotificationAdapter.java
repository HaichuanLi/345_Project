package com.soen345.ticketing.infrastructure.Notifications;

import com.soen345.ticketing.domain.Notifications.NotificationService;
import jakarta.mail.*;
import jakarta.mail.internet.*;
import java.util.Properties;

public class EmailNotificationAdapter implements NotificationService {
    private final String username;
    private final String password;

    public EmailNotificationAdapter(String username, String password) {
        this.username = username;
        this.password = password;
    }

    @Override
    public void sendConfirmation(String recipientEmail, String subject, String body) {
        if (username == null || password == null || username.isEmpty() || password.isEmpty()) {
            System.err.println("Email credentials not provided. Skipping email sending.");
            return;
        }

        Properties prop = new Properties();
        prop.put("mail.smtp.host", "smtp.gmail.com");
        prop.put("mail.smtp.port", "587");
        prop.put("mail.smtp.auth", "true");
        prop.put("mail.smtp.starttls.enable", "true");

        Session session = Session.getInstance(prop, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
            message.setSubject(subject);
            message.setText(body);

            Transport.send(message);
            System.out.println("Email sent successfully to " + recipientEmail);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
}
