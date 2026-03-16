package com.soen345.ticketing.domain.reservation;

import java.util.Optional;
import java.util.UUID;

public interface ReservationRepository {
    Optional<Reservation> findById(UUID id);

    Reservation save(Reservation reservation);
}
