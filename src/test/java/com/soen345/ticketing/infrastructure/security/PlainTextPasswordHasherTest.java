package com.soen345.ticketing.infrastructure.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
public class PlainTextPasswordHasherTest {
    private final PlainTextPasswordHasher hasher = new PlainTextPasswordHasher();

    @Test
    void hashReturnsRawPassword() {
        assertEquals("myPassword123", hasher.hash("myPassword123"));
    }

    @Test
    void hashReturnsEmptyStringForEmptyInput() {
        assertEquals("", hasher.hash(""));
    }

    @Test
    void matchesReturnsTrueForSamePassword() {
        assertTrue(hasher.matches("myPassword123", "myPassword123"));
    }

    @Test
    void matchesReturnsFalseForDifferentPassword() {
        assertFalse(hasher.matches("wrongPassword", "myPassword123"));
    }

    @Test
    void matchesReturnsFalseForEmptyVsNonEmpty() {
        assertFalse(hasher.matches("", "myPassword123"));
    }

    @Test
    void matchesReturnsTrueForEmptyVsEmpty() {
        assertTrue(hasher.matches("", ""));
    }

    @Test
    void hashAndMatchRoundTrip() {
        String raw = "securePass!99";
        String hashed = hasher.hash(raw);
        assertTrue(hasher.matches(raw, hashed));
    }
}
