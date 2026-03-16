package com.soen345.ticketing.application.auth;

import com.soen345.ticketing.domain.user.Role;

import java.util.UUID;

public record LoginResult(
        UUID userId,
        String name,
        String email,
        Role role
) {
}
