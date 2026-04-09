package com.soen345.ticketing.domain;

import com.soen345.ticketing.domain.user.Role;
import com.soen345.ticketing.domain.user.User;
import com.soen345.ticketing.domain.user.UserStatus;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UserTest {
    @Test
    void rejectsUserWithoutEmailOrPhone() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new User(
                        UUID.randomUUID(),
                        "No Contact",
                        null,
                        null,
                        "HASH_secret",
                        Role.CUSTOMER,
                        UserStatus.ACTIVE
                )
        );

        assertEquals("User must have at least an email or a phone number", exception.getMessage());
    }

    @Test
    void requiresPasswordHash() {
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new User(
                        UUID.randomUUID(),
                        "Missing Password",
                        "user@test.com",
                        null,
                        null,
                        Role.CUSTOMER,
                        UserStatus.ACTIVE
                )
        );

        assertEquals("passwordHash must not be null", exception.getMessage());
    }

    @Test
    void acceptsEmailOnlyUser() {
        assertDoesNotThrow(() -> new User(
                UUID.randomUUID(),
                "Email User",
                "user@test.com",
                null,
                "HASH_secret",
                Role.CUSTOMER,
                UserStatus.ACTIVE
        ));
    }

    @Test
    void acceptsPhoneOnlyUser() {
        assertDoesNotThrow(() -> new User(
                UUID.randomUUID(),
                "Phone User",
                null,
                "5145550000",
                "HASH_secret",
                Role.ADMIN,
                UserStatus.ACTIVE
        ));
    }
}
