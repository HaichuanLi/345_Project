package com.soen345.ticketing.application.auth;

import com.soen345.ticketing.domain.user.Role;

public record RegisterCommand(
        String name,
        String email,
        String phone,
        String password,
        Role role
) {
}
