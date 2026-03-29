package com.soen345.ticketing.application.usecase.reservation;

import com.soen345.ticketing.application.auth.ValidationException;
import com.soen345.ticketing.domain.event.Event;
import com.soen345.ticketing.domain.event.EventRepository;
import com.soen345.ticketing.domain.reservation.Reservation;
import com.soen345.ticketing.domain.reservation.ReservationRepository;
import com.soen345.ticketing.domain.reservation.ReservationStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class GetUserReservationsUseCase {
    private final ReservationRepository reservationRepository;
    private final EventRepository eventRepository;

    public GetUserReservationsUseCase(
            ReservationRepository reservationRepository,
            EventRepository eventRepository
    ) {
        this.reservationRepository = reservationRepository;
        this.eventRepository = eventRepository;
    }

    public List<UserReservationDTO> execute(UUID userId) {
        if (userId == null) {
            throw new ValidationException("User ID must not be null");
        }

        List<Reservation> reservations = reservationRepository.findByCustomerId(userId);

        return reservations.stream()
                .filter(r -> r.status() == ReservationStatus.CONFIRMED)
                .map(reservation -> {
                    Optional<Event> event = eventRepository.findById(reservation.eventId());
                    return event.map(e -> new UserReservationDTO(
                            reservation.id(),
                            e.id(),
                            e.title(),
                            e.eventCode(),
                            e.category(),
                            e.venue(),
                            e.startDateTime(),
                            e.endDateTime(),
                            e.description(),
                            reservation.quantity(),
                            e.price(),
                            reservation.quantity() * e.price()
                    ));
                })
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }
}
