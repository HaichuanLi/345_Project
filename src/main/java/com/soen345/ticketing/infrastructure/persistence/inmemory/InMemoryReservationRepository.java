package com.soen345.ticketing.infrastructure.persistence.inmemory;

import com.soen345.ticketing.domain.reservation.Reservation;
import com.soen345.ticketing.domain.reservation.ReservationRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class InMemoryReservationRepository implements ReservationRepository {
    private final Map<UUID, Reservation> reservationsById = new HashMap<>();
    private final Object lock = new Object();

    @Override
    public Optional<Reservation> findById(UUID id) {
        synchronized (lock) {
            return Optional.ofNullable(reservationsById.get(id));
        }
    }

    @Override
    public Reservation save(Reservation reservation) {
        synchronized (lock) {
            reservationsById.put(reservation.id(), reservation);
            return reservation;
        }
    }

    @Override
    public List<Reservation> findByCustomerId(UUID customerId) {
        synchronized (lock) {
            return reservationsById.values().stream()
                    .filter(reservation -> reservation.customerId().equals(customerId))
                    .collect(Collectors.toList());
        }
    }

    @Override
    public List<Reservation> findByEventId(UUID eventId) {
        synchronized (lock) {
            return reservationsById.values().stream()
                    .filter(reservation -> reservation.eventId().equals(eventId))
                    .collect(Collectors.toList());
        }
    }
}
