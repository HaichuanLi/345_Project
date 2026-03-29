package com.soen345.ticketing.application.usecase.reservation;

import com.soen345.ticketing.application.reservation.EventDetailsDTO;
import com.soen345.ticketing.application.reservation.EventNotFoundException;
import com.soen345.ticketing.application.reservation.InsufficientSeatsException;
import com.soen345.ticketing.application.reservation.ReserveTicketsCommand;
import com.soen345.ticketing.application.reservation.ReservationConfirmation;
import com.soen345.ticketing.application.reservation.ReservationConfirmationService;
import com.soen345.ticketing.application.reservation.ReserveTicketsValidator;
import com.soen345.ticketing.domain.event.Event;
import com.soen345.ticketing.domain.event.EventRepository;
import com.soen345.ticketing.domain.reservation.Reservation;
import com.soen345.ticketing.domain.reservation.ReservationRepository;
import com.soen345.ticketing.domain.reservation.ReservationStatus;

import java.time.Instant;
import java.util.UUID;

public class ReserveTicketsUseCase {
    private final EventRepository eventRepository;
    private final ReservationRepository reservationRepository;
    private final ReservationConfirmationService confirmationService;
    private final ReserveTicketsValidator validator;

    public ReserveTicketsUseCase(
            EventRepository eventRepository,
            ReservationRepository reservationRepository,
            ReservationConfirmationService confirmationService,
            ReserveTicketsValidator validator
    ) {
        this.eventRepository = eventRepository;
        this.reservationRepository = reservationRepository;
        this.confirmationService = confirmationService;
        this.validator = validator;
    }

    public ReservationConfirmation reserve(ReserveTicketsCommand command) {
        // Validate the command
        validator.validate(command);

        // Fetch the event
        Event event = eventRepository.findById(command.eventId())
                .orElseThrow(() -> new EventNotFoundException(command.eventId()));

        // Check if sufficient seats are available
        if (command.quantity() > event.availableTickets()) {
            throw new InsufficientSeatsException(command.quantity(), event.availableTickets());
        }

        // Create the reservation
        Reservation reservation = new Reservation(
                UUID.randomUUID(),
                event.id(),
                command.userId(),
                command.quantity(),
                Instant.now(),
            ReservationStatus.CONFIRMED
        );

        // Save the reservation
        Reservation savedReservation = reservationRepository.save(reservation);

        // Update event's available tickets
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

        // Create and save the confirmation
        ReservationConfirmation confirmation = new ReservationConfirmation(
                savedReservation.id(),
                savedReservation.customerId(),
                savedReservation.quantity(),
                EventDetailsDTO.fromEvent(updatedEvent),
                savedReservation.reservedAt(),
                savedReservation.status().toString()
        );

        confirmationService.saveConfirmation(confirmation);

        // Return the confirmation
        return confirmation;
    }
}
