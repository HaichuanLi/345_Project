package com.soen345.ticketing.support;

import com.soen345.ticketing.application.auth.PasswordHasher;

import java.util.HashMap;
import java.util.Map;

public class FakePasswordHasher implements PasswordHasher {
    private final Map<String, Boolean> matchResults = new HashMap<>();

    @Override
    public boolean matches(String rawPassword, String passwordHash) {
        return matchResults.getOrDefault(key(rawPassword, passwordHash), false);
    }

    public void stubMatch(String rawPassword, String passwordHash, boolean result) {
        matchResults.put(key(rawPassword, passwordHash), result);
    }

    private String key(String rawPassword, String passwordHash) {
        return rawPassword + "::" + passwordHash;
    }
}
