package com.soen345.ticketing.application.usecase.reservation;

import com.soen345.ticketing.application.auth.ValidationException;
import com.soen345.ticketing.domain.Notifications.NotificationService;
import com.soen345.ticketing.domain.event.Event;
import com.soen345.ticketing.domain.event.EventRepository;
import com.soen345.ticketing.domain.reservation.Reservation;
import com.soen345.ticketing.domain.reservation.ReservationRepository;
import com.soen345.ticketing.domain.reservation.ReservationStatus;
import com.soen345.ticketing.domain.user.User;
import com.soen345.ticketing.domain.user.UserRepository;

import java.util.UUID;

public class CancelReservationUseCase {
    private final ReservationRepository reservationRepository;
    private final EventRepository eventRepository;
    private final NotificationService notificationService;
    private final UserRepository userRepository;

    public CancelReservationUseCase(
            ReservationRepository reservationRepository,
            EventRepository eventRepository,
            NotificationService notificationService,
            UserRepository userRepository
    ) {
        this.reservationRepository = reservationRepository;
        this.eventRepository = eventRepository;
        this.notificationService = notificationService;
        this.userRepository = userRepository;
    }

    public CancelReservationUseCase(
            ReservationRepository reservationRepository,
            EventRepository eventRepository
    ) {
        this(reservationRepository, eventRepository, null, null);
    }

    public void execute(UUID reservationId) {
        if (reservationId == null) {
            throw new ValidationException("Reservation ID must not be null");
        }

        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ValidationException("Reservation not found"));

        if (reservation.status() == ReservationStatus.CANCELLED) {
            throw new ValidationException("Reservation is already cancelled");
        }

        Reservation cancelled = new Reservation(
                reservation.id(),
                reservation.eventId(),
                reservation.customerId(),
                reservation.quantity(),
                reservation.reservedAt(),
                ReservationStatus.CANCELLED
        );
        reservationRepository.save(cancelled);

        Event event = eventRepository.findById(reservation.eventId()).orElse(null);
        if (event != null) {
            int restoredTickets = Math.min(
                    event.availableTickets() + reservation.quantity(),
                    event.capacity()
            );
            Event updatedEvent = new Event(
                    event.id(),
                    event.eventCode(),
                    event.title(),
                    event.category(),
                    event.description(),
                    event.venue(),
                    event.startDateTime(),
                    event.endDateTime(),
                    event.capacity(),
                    restoredTickets,
                    event.organizerId(),
                    event.status(),
                    event.price()
            );
            eventRepository.save(updatedEvent);

            if (notificationService != null && userRepository != null) {
                userRepository.findById(reservation.customerId()).ifPresent(user -> {
                    if (user.email() != null && !user.email().isBlank()) {
                        String subject = "Reservation Cancelled";
                        String body = String.format(
                                "Your reservation for the event '%s' has been successfully cancelled. " +
                                "Number of tickets released: %d.",
                                event.title(),
                                reservation.quantity()
                        );
                        notificationService.sendConfirmation(user.email(), subject, body);
                    }
                });
            }
        }
    }
}
