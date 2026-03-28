package com.soen345.ticketing.application.usecase.event;

import com.soen345.ticketing.domain.event.Event;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FilterEventsUseCase {

    public List<Event> filter(List<Event> events, LocalDate date, String location, String category) {
        String normalizedLocation = normalize(location);
        String normalizedCategory = normalize(category);

        List<Event> filteredEvents = new ArrayList<>();
        for (Event event : events) {
            boolean dateMatches = date == null || event.startDateTime().toLocalDate().equals(date);
            boolean locationMatches = normalizedLocation.isEmpty()
                    || normalize(event.venue()).equals(normalizedLocation);
            boolean categoryMatches = normalizedCategory.isEmpty()
                    || normalize(event.category()).equals(normalizedCategory);

            if (dateMatches && locationMatches && categoryMatches) {
                filteredEvents.add(event);
            }
        }

        return filteredEvents;
    }

    private String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().toLowerCase(Locale.ROOT);
    }
}