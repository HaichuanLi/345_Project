package com.soen345.ticketing.application.event;

import java.time.LocalDateTime;
import java.util.UUID;

public record AddEventCommand(
        String eventCode,
        String title,
        String category,
        String description,
        String venue,
        LocalDateTime startDateTime,
        LocalDateTime endDateTime,
        int capacity,
        double price,
        UUID organizerId
) {
}
