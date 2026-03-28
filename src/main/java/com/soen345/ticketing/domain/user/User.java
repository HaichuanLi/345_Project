package com.soen345.ticketing.domain.user;

import java.util.Objects;
import java.util.UUID;

public record User(
        UUID id,
        String name,
        String email,
        String phone,
        String passwordHash,
        Role role,
        UserStatus status
) {
    public User {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(name, "name must not be null");
        if ((email == null || email.isBlank()) && (phone == null || phone.isBlank())) {
            throw new IllegalArgumentException("User must have at least an email or a phone number");
        }
        Objects.requireNonNull(passwordHash, "passwordHash must not be null");
        Objects.requireNonNull(role, "role must not be null");
        Objects.requireNonNull(status, "status must not be null");
    }
}
