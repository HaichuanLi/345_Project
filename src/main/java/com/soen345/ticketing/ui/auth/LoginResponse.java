package com.soen345.ticketing.ui.auth;

public record LoginResponse(String userId, String name, String email, String role) {
}
