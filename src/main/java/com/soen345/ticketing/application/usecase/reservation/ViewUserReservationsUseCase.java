package com.soen345.ticketing.application.usecase.reservation;

import com.soen345.ticketing.application.reservation.ReservationConfirmation;
import com.soen345.ticketing.application.reservation.ReservationConfirmationService;
import com.soen345.ticketing.domain.reservation.Reservation;
import com.soen345.ticketing.domain.reservation.ReservationRepository;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class ViewUserReservationsUseCase {
    private final ReservationRepository reservationRepository;
    private final ReservationConfirmationService confirmationService;

    public ViewUserReservationsUseCase(
            ReservationRepository reservationRepository,
            ReservationConfirmationService confirmationService
    ) {
        this.reservationRepository = reservationRepository;
        this.confirmationService = confirmationService;
    }

    public List<ReservationConfirmation> getReservationsForUser(UUID userId) {
        List<Reservation> reservations = reservationRepository.findByCustomerId(userId);

        return reservations.stream()
                .map(reservation -> confirmationService.getConfirmation(reservation.id()))
                .filter(java.util.Optional::isPresent)
                .map(java.util.Optional::get)
                .collect(Collectors.toList());
    }
}
