package com.soen345.ticketing.infrastructure.email;

import java.util.Objects;

public record SmtpConfig(
        String host,
        int port,
        String username,
        String password,
        String fromAddress
) {
    public SmtpConfig {
        Objects.requireNonNull(host, "host must not be null");
        Objects.requireNonNull(username, "username must not be null");
        Objects.requireNonNull(password, "password must not be null");
        Objects.requireNonNull(fromAddress, "fromAddress must not be null");

        if (host.isBlank()) {
            throw new IllegalArgumentException("host must not be blank");
        }
        if (port <= 0 || port > 65535) {
            throw new IllegalArgumentException("port must be between 1 and 65535");
        }
        if (fromAddress.isBlank()) {
            throw new IllegalArgumentException("fromAddress must not be blank");
        }
    }
}
