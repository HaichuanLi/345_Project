package com.soen345.ticketing.application.usecase.event;

import com.soen345.ticketing.domain.event.Event;
import com.soen345.ticketing.domain.event.EventRepository;
import com.soen345.ticketing.infrastructure.persistence.inmemory.InMemoryEventRepository;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EventListAndFilterUseCaseTest {

    @Test
    void EventList_DisplayEvents_ListVisible() {
        ListEventsUseCase useCase = new ListEventsUseCase(new InMemoryEventRepository());

        List<Event> events = useCase.listAvailableEvents();

        assertFalse(events.isEmpty());
    }

    @Test
    void EventList_DisplayEvents_EmptyListHandled() {
        ListEventsUseCase useCase = new ListEventsUseCase(new EmptyEventRepository());

        List<Event> events = useCase.listAvailableEvents();

        assertTrue(events.isEmpty());
    }

    @Test
    void EventList_EventDetails_EventNameVisible() {
        List<Event> events = new ListEventsUseCase(new InMemoryEventRepository()).listAvailableEvents();

        assertTrue(events.stream().allMatch(event -> !event.title().isBlank()));
    }

    @Test
    void EventList_EventDetails_EventCodeVisible() {
        List<Event> events = new ListEventsUseCase(new InMemoryEventRepository()).listAvailableEvents();

        assertTrue(events.stream().allMatch(event -> !event.eventCode().isBlank()));
    }

    @Test
    void EventList_EventDetails_StartTimeVisible() {
        List<Event> events = new ListEventsUseCase(new InMemoryEventRepository()).listAvailableEvents();

        assertTrue(events.stream().allMatch(event -> event.startDateTime() != null));
    }

    @Test
    void EventList_EventDetails_EndTimeVisible() {
        List<Event> events = new ListEventsUseCase(new InMemoryEventRepository()).listAvailableEvents();

        assertTrue(events.stream().allMatch(event -> event.endDateTime() != null));
    }

    @Test
    void EventList_EventDetails_LocationVisible() {
        List<Event> events = new ListEventsUseCase(new InMemoryEventRepository()).listAvailableEvents();

        assertTrue(events.stream().allMatch(event -> !event.venue().isBlank()));
    }

    @Test
    void EventList_EventDetails_CategoryVisible() {
        List<Event> events = new ListEventsUseCase(new InMemoryEventRepository()).listAvailableEvents();

        assertTrue(events.stream().allMatch(event -> !event.category().isBlank()));
    }

    @Test
    void EventList_EventDetails_AvailableSeatsVisible() {
        List<Event> events = new ListEventsUseCase(new InMemoryEventRepository()).listAvailableEvents();

        assertTrue(events.stream().allMatch(event -> event.availableTickets() >= 0));
    }

    @Test
    void EventFilter_ByDate_ValidDateShowsMatchingEvents() {
        FilterEventsUseCase useCase = new FilterEventsUseCase(new InMemoryEventRepository());

        List<Event> events = useCase.filterAvailableEvents(LocalDate.of(2026, 5, 10), null, null);

        assertFalse(events.isEmpty());
        assertTrue(events.stream().allMatch(event -> event.startDateTime().toLocalDate().equals(LocalDate.of(2026, 5, 10))));
    }

    @Test
    void EventFilter_ByDate_NoMatchingEvents() {
        FilterEventsUseCase useCase = new FilterEventsUseCase(new InMemoryEventRepository());

        List<Event> events = useCase.filterAvailableEvents(LocalDate.of(2026, 12, 31), null, null);

        assertTrue(events.isEmpty());
    }

    @Test
    void EventFilter_ByLocation_ValidLocationShowsMatchingEvents() {
        FilterEventsUseCase useCase = new FilterEventsUseCase(new InMemoryEventRepository());

        List<Event> events = useCase.filterAvailableEvents(null, "Security Institute", null);

        assertFalse(events.isEmpty());
        assertTrue(events.stream().allMatch(event -> event.venue().equalsIgnoreCase("Security Institute")));
    }

    @Test
    void EventFilter_ByLocation_NoMatchingEvents() {
        FilterEventsUseCase useCase = new FilterEventsUseCase(new InMemoryEventRepository());

        List<Event> events = useCase.filterAvailableEvents(null, "No Such Venue", null);

        assertTrue(events.isEmpty());
    }

    @Test
    void EventFilter_ByCategory_ValidCategoryShowsMatchingEvents() {
        FilterEventsUseCase useCase = new FilterEventsUseCase(new InMemoryEventRepository());

        List<Event> events = useCase.filterAvailableEvents(null, null, "Security");

        assertFalse(events.isEmpty());
        assertTrue(events.stream().allMatch(event -> event.category().equalsIgnoreCase("Security")));
    }

    @Test
    void EventFilter_ByCategory_NoMatchingEvents() {
        FilterEventsUseCase useCase = new FilterEventsUseCase(new InMemoryEventRepository());

        List<Event> events = useCase.filterAvailableEvents(null, null, "No Such Category");

        assertTrue(events.isEmpty());
    }

    @Test
    void EventFilter_MultipleFilters_DateAndLocation() {
        FilterEventsUseCase useCase = new FilterEventsUseCase(new InMemoryEventRepository());

        List<Event> events = useCase.filterAvailableEvents(LocalDate.of(2026, 5, 10), "Downtown Tech Hub", null);

        assertEquals(1, events.size());
        assertTrue(events.stream().allMatch(event -> event.startDateTime().toLocalDate().equals(LocalDate.of(2026, 5, 10))));
        assertTrue(events.stream().allMatch(event -> event.venue().equalsIgnoreCase("Downtown Tech Hub")));
    }

    @Test
    void EventFilter_MultipleFilters_AllFiltersApplied() {
        FilterEventsUseCase useCase = new FilterEventsUseCase(new InMemoryEventRepository());

        List<Event> events = useCase.filterAvailableEvents(
                LocalDate.of(2026, 5, 25),
                "Security Institute",
                "Security"
        );

        assertEquals(1, events.size());
        Event event = events.get(0);
        assertEquals(LocalDate.of(2026, 5, 25), event.startDateTime().toLocalDate());
        assertEquals("Security Institute", event.venue());
        assertEquals("Security", event.category());
    }

    @Test
    void EventFilter_MultipleFilters_NoMatchingResults() {
        FilterEventsUseCase useCase = new FilterEventsUseCase(new InMemoryEventRepository());

        List<Event> events = useCase.filterAvailableEvents(
                LocalDate.of(2026, 5, 25),
                "Security Institute",
                "Technology"
        );

        assertTrue(events.isEmpty());
    }

    private static final class EmptyEventRepository implements EventRepository {
        @Override
        public Optional<Event> findById(UUID id) {
            return Optional.empty();
        }

        @Override
        public Event save(Event event) {
            return event;
        }

        @Override
        public List<Event> listAvailable() {
            return Collections.emptyList();
        }
    }
}
