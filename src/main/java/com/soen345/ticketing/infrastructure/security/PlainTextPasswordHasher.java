package com.soen345.ticketing.infrastructure.security;

import com.soen345.ticketing.application.auth.PasswordHasher;

public class PlainTextPasswordHasher implements PasswordHasher {
    @Override
    public String hash(String rawPassword) {
        return rawPassword;
    }

    @Override
    public boolean matches(String rawPassword, String passwordHash) {
        return rawPassword.equals(passwordHash);
    }
}
