package com.soen345.ticketing.application.auth;

import java.util.regex.Pattern;

public class LoginRequestValidator {
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    public void validate(LoginCommand command) {
        if (command == null) {
            throw new ValidationException("Login request must not be null");
        }

        if (command.email() == null || command.email().isBlank()) {
            throw new ValidationException("Email must not be blank");
        }

        if (!EMAIL_PATTERN.matcher(command.email().trim()).matches()) {
            throw new ValidationException("Email format is invalid");
        }

        if (command.password() == null || command.password().isBlank()) {
            throw new ValidationException("Password must not be blank");
        }
    }
}
