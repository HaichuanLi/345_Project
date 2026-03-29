package com.soen345.ticketing.application.usecase.reservation;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserReservationDTO(
        UUID reservationId,
        UUID eventId,
        String eventName,
        String eventCode,
        String category,
        String venue,
        LocalDateTime startDateTime,
        LocalDateTime endDateTime,
        String description,
        int ticketsPurchased,
        double pricePerTicket,
        double totalPrice
) {
}
