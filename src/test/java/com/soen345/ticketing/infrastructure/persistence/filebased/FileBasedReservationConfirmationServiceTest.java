package com.soen345.ticketing.infrastructure.persistence.filebased;

import com.soen345.ticketing.application.reservation.EventDetailsDTO;
import com.soen345.ticketing.application.reservation.ReservationConfirmation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class FileBasedReservationConfirmationServiceTest {
    @TempDir
    Path tempDir;

    private FileBasedReservationConfirmationService service;

    private final UUID eventId = UUID.randomUUID();
    private final LocalDateTime start = LocalDateTime.of(2026, 8, 1, 9, 0);
    private final LocalDateTime end = LocalDateTime.of(2026, 8, 1, 17, 0);

    @BeforeEach
    void setUp() {
        service = new FileBasedReservationConfirmationService(tempDir.toString());
    }

    private ReservationConfirmation sampleConfirmation() {
        EventDetailsDTO eventDetails = new EventDetailsDTO(
                eventId, "EVT-001", "Test Event", "Technology",
                "Description", "Venue", start, end, 100, 80, 49.99
        );
        return new ReservationConfirmation(
                UUID.randomUUID(), UUID.randomUUID(), 2, eventDetails, Instant.now(), "CONFIRMED"
        );
    }

    @Test
    void savesAndRetrievesConfirmation() {
        ReservationConfirmation confirmation = sampleConfirmation();

        service.saveConfirmation(confirmation);
        Optional<ReservationConfirmation> retrieved = service.getConfirmation(confirmation.reservationId());

        assertTrue(retrieved.isPresent());
        assertEquals(confirmation.reservationId(), retrieved.get().reservationId());
        assertEquals(confirmation.customerId(), retrieved.get().customerId());
        assertEquals(confirmation.quantityReserved(), retrieved.get().quantityReserved());
    }

    @Test
    void returnsEmptyWhenConfirmationNotFound() {
        Optional<ReservationConfirmation> result = service.getConfirmation(UUID.randomUUID());
        assertFalse(result.isPresent());
    }

    @Test
    void rejectsNullConfirmation() {
        assertThrows(NullPointerException.class, () -> service.saveConfirmation(null));
    }

    @Test
    void rejectsNullReservationId() {
        assertThrows(NullPointerException.class, () -> service.getConfirmation(null));
    }

    @Test
    void savesMultipleConfirmationsAndRetrievesEach() {
        ReservationConfirmation c1 = sampleConfirmation();
        ReservationConfirmation c2 = sampleConfirmation();

        service.saveConfirmation(c1);
        service.saveConfirmation(c2);

        assertTrue(service.getConfirmation(c1.reservationId()).isPresent());
        assertTrue(service.getConfirmation(c2.reservationId()).isPresent());
    }

    @Test
    void loadsConfirmationsFromDiskOnInit() {
        ReservationConfirmation confirmation = sampleConfirmation();
        service.saveConfirmation(confirmation);

        // Create a new instance pointing to the same directory
        FileBasedReservationConfirmationService newService =
                new FileBasedReservationConfirmationService(tempDir.toString());

        Optional<ReservationConfirmation> retrieved = newService.getConfirmation(confirmation.reservationId());
        assertTrue(retrieved.isPresent());
        assertEquals(confirmation.reservationId(), retrieved.get().reservationId());
    }

    @Test
    void overwritesExistingConfirmationOnSave() {
        ReservationConfirmation original = sampleConfirmation();
        service.saveConfirmation(original);

        // Save a new confirmation with the same reservationId but different status
        EventDetailsDTO eventDetails = new EventDetailsDTO(
                eventId, "EVT-001", "Test Event", "Technology",
                "Description", "Venue", start, end, 100, 80, 49.99
        );
        ReservationConfirmation updated = new ReservationConfirmation(
                original.reservationId(), original.customerId(), 2,
                eventDetails, Instant.now(), "CANCELLED"
        );
        service.saveConfirmation(updated);

        Optional<ReservationConfirmation> retrieved = service.getConfirmation(original.reservationId());
        assertTrue(retrieved.isPresent());
        assertEquals("CANCELLED", retrieved.get().reservationStatus());
    }

    //ReservationConfirmationStorageException

    @Test
    void storageExceptionWithMessageOnly() {
        ReservationConfirmationStorageException ex =
                new ReservationConfirmationStorageException("Storage failed");
        assertEquals("Storage failed", ex.getMessage());
        assertNull(ex.getCause());
    }

    @Test
    void storageExceptionWithMessageAndCause() {
        RuntimeException cause = new RuntimeException("root cause");
        ReservationConfirmationStorageException ex =
                new ReservationConfirmationStorageException("Storage failed", cause);
        assertEquals("Storage failed", ex.getMessage());
        assertEquals(cause, ex.getCause());
    }
}
