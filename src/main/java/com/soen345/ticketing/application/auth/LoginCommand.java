package com.soen345.ticketing.application.auth;

/**
 * Login command accepting either email or phone as the identifier.
 */
public record LoginCommand(String identifier, String password) {
}
