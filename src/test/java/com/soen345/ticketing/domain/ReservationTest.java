package com.soen345.ticketing.domain;

import com.soen345.ticketing.domain.reservation.Reservation;
import com.soen345.ticketing.domain.reservation.ReservationStatus;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ReservationTest {
    @Test
    void rejectsZeroQuantity() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> reservation(0)
        );

        assertEquals("quantity must be greater than zero", exception.getMessage());
    }

    @Test
    void rejectsNegativeQuantity() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> reservation(-1)
        );

        assertEquals("quantity must be greater than zero", exception.getMessage());
    }

    @Test
    void requiresReservedAt() {
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new Reservation(
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        1,
                        null,
                        ReservationStatus.CONFIRMED
                )
        );

        assertEquals("reservedAt must not be null", exception.getMessage());
    }

    @Test
    void acceptsConfirmedReservation() {
        assertDoesNotThrow(() -> reservation(2));
    }

    private Reservation reservation(int quantity) {
        return new Reservation(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                quantity,
                Instant.parse("2026-04-08T12:00:00Z"),
                ReservationStatus.CONFIRMED
        );
    }
}
