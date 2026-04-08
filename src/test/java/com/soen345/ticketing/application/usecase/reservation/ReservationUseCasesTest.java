package com.soen345.ticketing.application.usecase.reservation;

import com.soen345.ticketing.application.reservation.EventDetailsDTO;
import com.soen345.ticketing.application.reservation.ReservationConfirmation;
import com.soen345.ticketing.application.reservation.ReservationConfirmationService;
import com.soen345.ticketing.domain.event.Event;
import com.soen345.ticketing.domain.event.EventStatus;
import com.soen345.ticketing.domain.reservation.Reservation;
import com.soen345.ticketing.domain.reservation.ReservationRepository;
import com.soen345.ticketing.domain.reservation.ReservationStatus;
import com.soen345.ticketing.infrastructure.persistence.inmemory.InMemoryReservationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
public class ReservationUseCasesTest {
    private final LocalDateTime start = LocalDateTime.of(2026, 8, 1, 9, 0);
    private final LocalDateTime end = LocalDateTime.of(2026, 8, 1, 17, 0);

    // Simple in-memory confirmation service for testing
    private static class FakeConfirmationService implements ReservationConfirmationService {
        private final Map<UUID, ReservationConfirmation> store = new HashMap<>();

        @Override
        public void saveConfirmation(ReservationConfirmation confirmation) {
            store.put(confirmation.reservationId(), confirmation);
        }

        @Override
        public Optional<ReservationConfirmation> getConfirmation(UUID reservationId) {
            return Optional.ofNullable(store.get(reservationId));
        }
    }

    private ReservationConfirmation sampleConfirmation(UUID reservationId, UUID customerId) {
        EventDetailsDTO eventDetails = new EventDetailsDTO(
                UUID.randomUUID(), "EVT-001", "Test Event", "Technology",
                "Description", "Venue", start, end, 100, 80, 49.99
        );
        return new ReservationConfirmation(
                reservationId, customerId, 2, eventDetails, Instant.now(), "CONFIRMED"
        );
    }

    //RetrieveReservationConfirmationUseCase

    @Test
    void getConfirmationByReservationIdReturnsConfirmation() {
        FakeConfirmationService confirmationService = new FakeConfirmationService();
        UUID reservationId = UUID.randomUUID();
        ReservationConfirmation confirmation = sampleConfirmation(reservationId, UUID.randomUUID());
        confirmationService.saveConfirmation(confirmation);

        RetrieveReservationConfirmationUseCase useCase =
                new RetrieveReservationConfirmationUseCase(confirmationService);

        Optional<ReservationConfirmation> result =
                useCase.getConfirmationByReservationId(reservationId);

        assertTrue(result.isPresent());
        assertEquals(reservationId, result.get().reservationId());
    }

    @Test
    void getConfirmationByReservationIdReturnsEmptyWhenNotFound() {
        FakeConfirmationService confirmationService = new FakeConfirmationService();
        RetrieveReservationConfirmationUseCase useCase =
                new RetrieveReservationConfirmationUseCase(confirmationService);

        Optional<ReservationConfirmation> result =
                useCase.getConfirmationByReservationId(UUID.randomUUID());

        assertFalse(result.isPresent());
    }

    @Test
    void getConfirmationByReservationIdRejectsNull() {
        RetrieveReservationConfirmationUseCase useCase =
                new RetrieveReservationConfirmationUseCase(new FakeConfirmationService());

        assertThrows(NullPointerException.class,
                () -> useCase.getConfirmationByReservationId(null));
    }

    @Test
    void getConfirmationDetailsReturnsFormattedString() {
        FakeConfirmationService confirmationService = new FakeConfirmationService();
        UUID reservationId = UUID.randomUUID();
        confirmationService.saveConfirmation(sampleConfirmation(reservationId, UUID.randomUUID()));

        RetrieveReservationConfirmationUseCase useCase =
                new RetrieveReservationConfirmationUseCase(confirmationService);

        String details = useCase.getConfirmationDetails(reservationId);

        assertNotNull(details);
        assertTrue(details.contains("CONFIRMED"));
    }

    @Test
    void getConfirmationDetailsReturnsNotFoundMessageWhenMissing() {
        RetrieveReservationConfirmationUseCase useCase =
                new RetrieveReservationConfirmationUseCase(new FakeConfirmationService());

        UUID reservationId = UUID.randomUUID();
        String details = useCase.getConfirmationDetails(reservationId);

        assertTrue(details.contains("not found"));
        assertTrue(details.contains(reservationId.toString()));
    }

    //ViewUserReservationsUseCase

    @Test
    void getReservationsForUserReturnsConfirmedReservations() {
        FakeConfirmationService confirmationService = new FakeConfirmationService();
        InMemoryReservationRepository reservationRepository = new InMemoryReservationRepository();

        UUID userId = UUID.randomUUID();
        UUID reservationId = UUID.randomUUID();

        Reservation reservation = new Reservation(
                reservationId, UUID.randomUUID(), userId, 2, Instant.now(), ReservationStatus.CONFIRMED
        );
        reservationRepository.save(reservation);

        ReservationConfirmation confirmation = sampleConfirmation(reservationId, userId);
        confirmationService.saveConfirmation(confirmation);

        ViewUserReservationsUseCase useCase =
                new ViewUserReservationsUseCase(reservationRepository, confirmationService);

        List<ReservationConfirmation> result = useCase.getReservationsForUser(userId);

        assertEquals(1, result.size());
        assertEquals(reservationId, result.get(0).reservationId());
    }

    @Test
    void getReservationsForUserReturnsEmptyWhenNoReservations() {
        ViewUserReservationsUseCase useCase = new ViewUserReservationsUseCase(
                new InMemoryReservationRepository(), new FakeConfirmationService()
        );

        List<ReservationConfirmation> result = useCase.getReservationsForUser(UUID.randomUUID());

        assertTrue(result.isEmpty());
    }

    @Test
    void getReservationsForUserSkipsReservationsWithoutConfirmation() {
        FakeConfirmationService confirmationService = new FakeConfirmationService();
        InMemoryReservationRepository reservationRepository = new InMemoryReservationRepository();

        UUID userId = UUID.randomUUID();
        // Save reservation but no matching confirmation
        Reservation reservation = new Reservation(
                UUID.randomUUID(), UUID.randomUUID(), userId, 2, Instant.now(), ReservationStatus.CONFIRMED
        );
        reservationRepository.save(reservation);

        ViewUserReservationsUseCase useCase =
                new ViewUserReservationsUseCase(reservationRepository, confirmationService);

        List<ReservationConfirmation> result = useCase.getReservationsForUser(userId);

        assertTrue(result.isEmpty());
    }

    @Test
    void getReservationsForUserReturnsMultipleConfirmations() {
        FakeConfirmationService confirmationService = new FakeConfirmationService();
        InMemoryReservationRepository reservationRepository = new InMemoryReservationRepository();

        UUID userId = UUID.randomUUID();

        for (int i = 0; i < 3; i++) {
            UUID reservationId = UUID.randomUUID();
            reservationRepository.save(new Reservation(
                    reservationId, UUID.randomUUID(), userId, 1, Instant.now(), ReservationStatus.CONFIRMED
            ));
            confirmationService.saveConfirmation(sampleConfirmation(reservationId, userId));
        }

        ViewUserReservationsUseCase useCase =
                new ViewUserReservationsUseCase(reservationRepository, confirmationService);

        List<ReservationConfirmation> result = useCase.getReservationsForUser(userId);

        assertEquals(3, result.size());
    }
}
