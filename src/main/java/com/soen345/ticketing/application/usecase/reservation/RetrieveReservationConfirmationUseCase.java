package com.soen345.ticketing.application.usecase.reservation;

import com.soen345.ticketing.application.reservation.ReservationConfirmation;
import com.soen345.ticketing.application.reservation.ReservationConfirmationService;

import java.util.Optional;
import java.util.Objects;
import java.util.UUID;

public class RetrieveReservationConfirmationUseCase {
    private final ReservationConfirmationService confirmationService;

    public RetrieveReservationConfirmationUseCase(
            ReservationConfirmationService confirmationService
    ) {
        this.confirmationService = confirmationService;
    }

    public Optional<ReservationConfirmation> getConfirmationByReservationId(UUID reservationId) {
        Objects.requireNonNull(reservationId, "reservationId must not be null");
        return confirmationService.getConfirmation(reservationId);
    }

    public String getConfirmationDetails(UUID reservationId) {
        return getConfirmationByReservationId(reservationId)
                .map(ReservationConfirmation::toString)
                .orElse("Confirmation not found for reservation ID: " + reservationId);
    }
}
