package com.soen345.ticketing.domain.Notifications;

public interface NotificationService {
    void sendConfirmation(String recipientEmail, String subject, String body);
}
