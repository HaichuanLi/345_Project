package com.soen345.ticketing.application.auth;

public class ValidationException extends RuntimeException {
    public ValidationException(String message) {
        super(message);
    }
}
