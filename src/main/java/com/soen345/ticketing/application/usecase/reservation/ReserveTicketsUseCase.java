package com.soen345.ticketing.application.usecase.reservation;

import com.soen345.ticketing.application.reservation.EventDetailsDTO;
import com.soen345.ticketing.application.reservation.EventNotFoundException;
import com.soen345.ticketing.application.reservation.InsufficientSeatsException;
import com.soen345.ticketing.application.reservation.ReserveTicketsCommand;
import com.soen345.ticketing.application.reservation.ReservationConfirmation;
import com.soen345.ticketing.application.reservation.ReservationConfirmationService;
import com.soen345.ticketing.application.reservation.ReserveTicketsValidator;
import com.soen345.ticketing.domain.Notifications.NotificationService;
import com.soen345.ticketing.domain.event.Event;
import com.soen345.ticketing.domain.event.EventRepository;
import com.soen345.ticketing.domain.reservation.Reservation;
import com.soen345.ticketing.domain.reservation.ReservationRepository;
import com.soen345.ticketing.domain.reservation.ReservationStatus;
import com.soen345.ticketing.domain.user.User;
import com.soen345.ticketing.domain.user.UserRepository;

import java.time.Instant;
import java.util.UUID;

public class ReserveTicketsUseCase {
    private final EventRepository eventRepository;
    private final ReservationRepository reservationRepository;
    private final ReservationConfirmationService confirmationService;
    private final ReserveTicketsValidator validator;
    private final NotificationService notificationService;
    private final UserRepository userRepository;

    public ReserveTicketsUseCase(
            EventRepository eventRepository,
            ReservationRepository reservationRepository,
            ReservationConfirmationService confirmationService,
            ReserveTicketsValidator validator,
            NotificationService notificationService,
            UserRepository userRepository
    ) {
        this.eventRepository = eventRepository;
        this.reservationRepository = reservationRepository;
        this.confirmationService = confirmationService;
        this.validator = validator;
        this.notificationService = notificationService;
        this.userRepository = userRepository;
    }

    public ReserveTicketsUseCase(
            EventRepository eventRepository,
            ReservationRepository reservationRepository,
            ReservationConfirmationService confirmationService,
            ReserveTicketsValidator validator
    ) {
        this(eventRepository, reservationRepository, confirmationService, validator, null, null);
    }

    public synchronized ReservationConfirmation reserve(ReserveTicketsCommand command) {
        //validate command
        validator.validate(command);

        //fetch event
        Event event = eventRepository.findById(command.eventId())
                .orElseThrow(() -> new EventNotFoundException(command.eventId()));

        //reject reservations for cancelled events
        if (event.status() == com.soen345.ticketing.domain.event.EventStatus.CANCELLED) {
            throw new EventNotFoundException(command.eventId());
        }

        //check if enough seats available
        if (command.quantity() > event.availableTickets()) {
            throw new InsufficientSeatsException(command.quantity(), event.availableTickets());
        }

        //create reservation
        Reservation reservation = new Reservation(
                UUID.randomUUID(),
                event.id(),
                command.userId(),
                command.quantity(),
                Instant.now(),
            ReservationStatus.CONFIRMED
        );

        //save reservation
        Reservation savedReservation = reservationRepository.save(reservation);

        //update event's available tickets
        int updatedAvailableTickets = event.availableTickets() - command.quantity();
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
                updatedAvailableTickets,
                event.organizerId(),
            event.status(),
            event.price()
        );
        eventRepository.save(updatedEvent);

        //create and save confirmation
        ReservationConfirmation confirmation = new ReservationConfirmation(
                savedReservation.id(),
                savedReservation.customerId(),
                savedReservation.quantity(),
                EventDetailsDTO.fromEvent(updatedEvent),
                savedReservation.reservedAt(),
                savedReservation.status().toString()
        );

        if (notificationService != null && userRepository != null) {
            userRepository.findById(command.userId()).ifPresent(user -> {
                if (user.email() != null && !user.email().isBlank()) {
                    notificationService.sendConfirmation(user.email(), "Reservation Confirmation", confirmation.toString());
                }
            });
        }


        confirmationService.saveConfirmation(confirmation);

        //return the confirmation
        return confirmation;
    }
}
