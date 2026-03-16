package com.soen345.ticketing.application.auth;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LoginRequestValidatorTest {
    private final LoginRequestValidator validator = new LoginRequestValidator();

    @Test
    void rejectsBlankEmail() {
        LoginCommand command = new LoginCommand("", "secret123");

        ValidationException exception =
                assertThrows(ValidationException.class, () -> validator.validate(command));

        assertEquals("Email must not be blank", exception.getMessage());
    }

    @Test
    void rejectsInvalidEmailFormat() {
        LoginCommand command = new LoginCommand("not-an-email", "secret123");

        ValidationException exception =
                assertThrows(ValidationException.class, () -> validator.validate(command));

        assertEquals("Email format is invalid", exception.getMessage());
    }

    @Test
    void rejectsBlankPassword() {
        LoginCommand command = new LoginCommand("customer@site.com", "");

        ValidationException exception =
                assertThrows(ValidationException.class, () -> validator.validate(command));

        assertEquals("Password must not be blank", exception.getMessage());
    }
}
