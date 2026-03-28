package com.soen345.ticketing.application.reservation;

import java.util.Optional;
import java.util.UUID;

public interface ReservationConfirmationService {
    /**
     * Save a confirmation record
     */
    void saveConfirmation(ReservationConfirmation confirmation);

    /**
     * Retrieve a confirmation by reservation ID
     */
    Optional<ReservationConfirmation> getConfirmation(UUID reservationId);
}
