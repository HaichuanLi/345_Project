package com.soen345.ticketing.application.auth;

public interface PasswordHasher {
    boolean matches(String rawPassword, String passwordHash);
}
