package com.soen345.ticketing.domain.event;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EventRepository {
    Optional<Event> findById(UUID id);

    Event save(Event event);

    List<Event> listAvailable();

    List<Event> listAll();
}
