package com.soen345.ticketing.infrastructure.notification.filebased;

public class EmailDeliveryException extends RuntimeException {
    public EmailDeliveryException(String message, Throwable cause) {
        super(message, cause);
    }
}
