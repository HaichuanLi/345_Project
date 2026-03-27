package com.soen345.ticketing.application.usecase.event;

import com.soen345.ticketing.domain.event.Event;
import com.soen345.ticketing.domain.event.EventRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

public class FilterEventsUseCase {
    private final EventRepository eventRepository;

    public FilterEventsUseCase(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    public List<Event> filterAvailableEvents(LocalDate date, String location, String category) {
        String normalizedLocation = normalize(location);
        String normalizedCategory = normalize(category);

        return eventRepository.listAvailable().stream()
                .filter(event -> date == null || event.startDateTime().toLocalDate().equals(date))
                .filter(event -> normalizedLocation.isEmpty()
                        || normalize(event.venue()).equals(normalizedLocation))
                .filter(event -> normalizedCategory.isEmpty()
                        || normalize(event.category()).equals(normalizedCategory))
                .toList();
    }

    private String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().toLowerCase(Locale.ROOT);
    }
}
