package com.soen345.ticketing.android;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.soen345.ticketing.domain.event.Event;
import com.soen345.ticketing.domain.event.EventStatus;
import com.soen345.ticketing.domain.reservation.Reservation;
import com.soen345.ticketing.domain.reservation.ReservationStatus;
import com.soen345.ticketing.domain.user.Role;
import com.soen345.ticketing.domain.user.User;
import com.soen345.ticketing.domain.user.UserStatus;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class FirestoreRepositoryIntegrationTest {

    @Test
    public void eventRepositoryRoundTripPersistsAndListsEvent() {
        FirestoreEventRepository repository = new FirestoreEventRepository();
        Event event = new Event(
                UUID.randomUUID(),
                "EVT-INT-" + UUID.randomUUID().toString().substring(0, 8),
                "Repository Integration Event",
                "Technology",
                "Firestore event repository integration test",
                "Integration Hall",
                LocalDateTime.of(2026, 12, 20, 9, 0),
                LocalDateTime.of(2026, 12, 20, 11, 0),
                40,
                40,
                UUID.randomUUID(),
                EventStatus.PUBLISHED,
                55.0
        );

        repository.save(event);

        Event found = repository.findById(event.id()).orElseThrow();

        assertEquals(event.id(), found.id());
        assertEquals(event.title(), found.title());
        assertTrue(repository.listAvailable().stream().anyMatch(e -> e.id().equals(event.id())));
        assertTrue(repository.listAll().stream().anyMatch(e -> e.id().equals(event.id())));
    }

    @Test
    public void userRepositoryRoundTripPersistsAndFindsByEmailAndPhone() {
        FirestoreUserRepository repository = new FirestoreUserRepository();
        User user = new User(
                UUID.randomUUID(),
                "Integration User",
                "integration+" + UUID.randomUUID().toString().substring(0, 8) + "@test.com",
                "514" + String.valueOf(System.currentTimeMillis()).substring(6),
                "plain-secret",
                Role.CUSTOMER,
                UserStatus.ACTIVE
        );

        repository.save(user);

        User foundByEmail = repository.findByEmail(user.email()).orElseThrow();
        User foundByPhone = repository.findByPhone(user.phone()).orElseThrow();

        assertEquals(user.id(), foundByEmail.id());
        assertEquals(user.id(), foundByPhone.id());
    }

    @Test
    public void reservationRepositoryRoundTripPersistsAndQueriesByCustomerAndEvent() {
        FirestoreReservationRepository repository = new FirestoreReservationRepository();
        UUID eventId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        Reservation reservation = new Reservation(
                UUID.randomUUID(),
                eventId,
                customerId,
                3,
                Instant.parse("2026-04-08T18:00:00Z"),
                ReservationStatus.CONFIRMED
        );

        repository.save(reservation);

        Reservation found = repository.findById(reservation.id()).orElseThrow();

        assertEquals(reservation.id(), found.id());
        assertTrue(repository.findByCustomerId(customerId).stream().anyMatch(r -> r.id().equals(reservation.id())));
        assertTrue(repository.findByEventId(eventId).stream().anyMatch(r -> r.id().equals(reservation.id())));
    }
}
