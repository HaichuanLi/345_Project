package com.soen345.ticketing.application.event;

import com.soen345.ticketing.application.usecase.event.FilterEventsUseCase;
import com.soen345.ticketing.application.usecase.event.ListEventsUseCase;
import com.soen345.ticketing.domain.event.Event;
import com.soen345.ticketing.domain.event.EventRepository;
import com.soen345.ticketing.domain.event.EventStatus;
import com.soen345.ticketing.infrastructure.persistence.inmemory.InMemoryEventRepository;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EventListAndFilterUseCaseTest {
    private final FilterEventsUseCase filterEventsUseCase = new FilterEventsUseCase();

    @Test
    void EventList_DisplayEvents_ListVisible() {
        ListEventsUseCase useCase = new ListEventsUseCase(new InMemoryEventRepository());

        List<Event> events = useCase.listAvailableEvents();

        assertFalse(events.isEmpty());
    }

    @Test
    void EventList_DisplayEvents_EmptyListHandled() {
        ListEventsUseCase useCase = new ListEventsUseCase(new EmptyAvailableEventsRepository());

        List<Event> events = useCase.listAvailableEvents();

        assertTrue(events.isEmpty());
    }

    @Test
    void EventList_EventDetails_EventNameVisible() {
        Event event = firstAvailableEvent();

        assertNotNull(event.title());
        assertFalse(event.title().isBlank());
    }

    @Test
    void EventList_EventDetails_EventCodeVisible() {
        Event event = firstAvailableEvent();

        assertNotNull(event.eventCode());
        assertFalse(event.eventCode().isBlank());
    }

    @Test
    void EventList_EventDetails_StartTimeVisible() {
        Event event = firstAvailableEvent();

        assertNotNull(event.startDateTime());
    }

    @Test
    void EventList_EventDetails_EndTimeVisible() {
        Event event = firstAvailableEvent();

        assertNotNull(event.endDateTime());
    }

    @Test
    void EventList_EventDetails_LocationVisible() {
        Event event = firstAvailableEvent();

        assertNotNull(event.venue());
        assertFalse(event.venue().isBlank());
    }

    @Test
    void EventList_EventDetails_CategoryVisible() {
        Event event = firstAvailableEvent();

        assertNotNull(event.category());
        assertFalse(event.category().isBlank());
    }

    @Test
    void EventList_EventDetails_AvailableSeatsVisible() {
        Event event = firstAvailableEvent();

        assertTrue(event.availableTickets() >= 0);
    }

    @Test
    void EventFilter_ByDate_ValidDateShowsMatchingEvents() {
        List<Event> events = availableEvents();

        List<Event> filtered = filterEventsUseCase.filter(
                events,
                LocalDate.of(2026, 5, 10),
                "",
                ""
        );

        assertFalse(filtered.isEmpty());
        assertTrue(filtered.stream().allMatch(e -> e.startDateTime().toLocalDate().equals(LocalDate.of(2026, 5, 10))));
    }

    @Test
    void EventFilter_ByDate_NoMatchingEvents() {
        List<Event> events = availableEvents();

        List<Event> filtered = filterEventsUseCase.filter(
                events,
                LocalDate.of(2030, 1, 1),
                "",
                ""
        );

        assertTrue(filtered.isEmpty());
    }

    @Test
    void EventFilter_ByLocation_ValidLocationShowsMatchingEvents() {
        List<Event> events = availableEvents();

        List<Event> filtered = filterEventsUseCase.filter(
                events,
                null,
                "Downtown Tech Hub",
                ""
        );

        assertFalse(filtered.isEmpty());
        assertTrue(filtered.stream().allMatch(e -> "Downtown Tech Hub".equals(e.venue())));
    }

    @Test
    void EventFilter_ByLocation_NoMatchingEvents() {
        List<Event> events = availableEvents();

        List<Event> filtered = filterEventsUseCase.filter(
                events,
                null,
                "No Such Venue",
                ""
        );

        assertTrue(filtered.isEmpty());
    }

    @Test
    void EventFilter_ByCategory_ValidCategoryShowsMatchingEvents() {
        List<Event> events = availableEvents();

        List<Event> filtered = filterEventsUseCase.filter(
                events,
                null,
                "",
                "Security"
        );

        assertFalse(filtered.isEmpty());
        assertTrue(filtered.stream().allMatch(e -> "Security".equals(e.category())));
    }

    @Test
    void EventFilter_ByCategory_NoMatchingEvents() {
        List<Event> events = availableEvents();

        List<Event> filtered = filterEventsUseCase.filter(
                events,
                null,
                "",
                "Music"
        );

        assertTrue(filtered.isEmpty());
    }

    @Test
    void EventFilter_MultipleFilters_DateAndLocation() {
        List<Event> events = availableEvents();

        List<Event> filtered = filterEventsUseCase.filter(
                events,
                LocalDate.of(2026, 5, 10),
                "Downtown Tech Hub",
                ""
        );

        assertEquals(1, filtered.size());
        assertEquals("EVT-2026002", filtered.get(0).eventCode());
    }

    @Test
    void EventFilter_MultipleFilters_AllFiltersApplied() {
        List<Event> events = availableEvents();

        List<Event> filtered = filterEventsUseCase.filter(
                events,
                LocalDate.of(2026, 5, 25),
                "Security Institute",
                "Security"
        );

        assertEquals(1, filtered.size());
        assertEquals("EVT-2026006", filtered.get(0).eventCode());
    }

    @Test
    void EventFilter_MultipleFilters_NoMatchingResults() {
        List<Event> events = availableEvents();

        List<Event> filtered = filterEventsUseCase.filter(
                events,
                LocalDate.of(2026, 5, 10),
                "Downtown Tech Hub",
                "Security"
        );

        assertTrue(filtered.isEmpty());
    }

    private List<Event> availableEvents() {
        return new ListEventsUseCase(new InMemoryEventRepository()).listAvailableEvents();
    }

    private Event firstAvailableEvent() {
        return availableEvents().get(0);
    }

    private static final class EmptyAvailableEventsRepository implements EventRepository {
        private final Map<UUID, Event> events = new HashMap<>();

        private EmptyAvailableEventsRepository() {
            UUID organizerId = UUID.randomUUID();
            Event draftEvent = new Event(
                    UUID.randomUUID(),
                    "EVT-EMPTY-001",
                    "Draft Event",
                    "Technology",
                    "Hidden draft event",
                    "Nowhere",
                    LocalDateTime.of(2026, 1, 1, 10, 0),
                    LocalDateTime.of(2026, 1, 1, 12, 0),
                    100,
                    100,
                    organizerId,
                    EventStatus.DRAFT,
                    0.0
            );
            Event soldOutEvent = new Event(
                    UUID.randomUUID(),
                    "EVT-EMPTY-002",
                    "Sold Out Event",
                    "Technology",
                    "Sold out event",
                    "Nowhere",
                    LocalDateTime.of(2026, 1, 2, 10, 0),
                    LocalDateTime.of(2026, 1, 2, 12, 0),
                    100,
                    0,
                    organizerId,
                    EventStatus.PUBLISHED,
                    0.0
            );

            events.put(draftEvent.id(), draftEvent);
            events.put(soldOutEvent.id(), soldOutEvent);
        }

        @Override
        public Optional<Event> findById(UUID id) {
            return Optional.ofNullable(events.get(id));
        }

        @Override
        public Event save(Event event) {
            events.put(event.id(), event);
            return event;
        }

        @Override
        public List<Event> listAvailable() {
            return events.values().stream()
                    .filter(event -> event.status() == EventStatus.PUBLISHED)
                    .filter(event -> event.availableTickets() > 0)
                    .toList();
        }
    }
}