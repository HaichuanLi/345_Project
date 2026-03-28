package com.soen345.ticketing.application.auth;

import java.util.regex.Pattern;

public class LoginRequestValidator {
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    private static final Pattern PHONE_PATTERN =
            Pattern.compile("^\\+?[0-9]{7,15}$");

    public void validate(LoginCommand command) {
        if (command == null) {
            throw new ValidationException("Login request must not be null");
        }

        if (command.identifier() == null || command.identifier().isBlank()) {
            throw new ValidationException("Email or phone number must not be blank");
        }

        String identifier = command.identifier().trim();
        if (!isValidIdentifier(identifier)) {
            throw new ValidationException("Enter a valid email address or phone number");
        }

        if (command.password() == null || command.password().isBlank()) {
            throw new ValidationException("Password must not be blank");
        }
    }

    public static boolean isValidIdentifier(String identifier) {
        return isEmail(identifier) || isPhone(identifier);
    }

    public static boolean isEmail(String identifier) {
        return EMAIL_PATTERN.matcher(identifier).matches();
    }

    public static boolean isPhone(String identifier) {
        return PHONE_PATTERN.matcher(identifier).matches();
    }
}
