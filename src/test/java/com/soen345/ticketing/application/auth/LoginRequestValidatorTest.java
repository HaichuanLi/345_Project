package com.soen345.ticketing.application.auth;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LoginRequestValidatorTest {
    private final LoginRequestValidator validator = new LoginRequestValidator();

    @Test
    void rejectsBlankIdentifier() {
        LoginCommand command = new LoginCommand("", "secret123");

        ValidationException exception =
                assertThrows(ValidationException.class, () -> validator.validate(command));

        assertEquals("Email or phone number must not be blank", exception.getMessage());
    }

    @Test
    void rejectsInvalidIdentifier() {
        LoginCommand command = new LoginCommand("not-valid", "secret123");

        ValidationException exception =
                assertThrows(ValidationException.class, () -> validator.validate(command));

        assertEquals("Enter a valid email address or phone number", exception.getMessage());
    }

    @Test
    void rejectsBlankPassword() {
        LoginCommand command = new LoginCommand("customer@site.com", "");

        ValidationException exception =
                assertThrows(ValidationException.class, () -> validator.validate(command));

        assertEquals("Password must not be blank", exception.getMessage());
    }

    @Test
    void acceptsValidEmail() {
        LoginCommand command = new LoginCommand("customer@site.com", "secret123");
        validator.validate(command);
    }

    @Test
    void acceptsValidPhone() {
        LoginCommand command = new LoginCommand("5141234567", "secret123");
        validator.validate(command);
    }
}
