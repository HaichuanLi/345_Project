package com.soen345.ticketing.application.event;

import com.soen345.ticketing.application.auth.ValidationException;
import com.soen345.ticketing.application.usecase.event.CancelEventUseCase;
import com.soen345.ticketing.domain.event.Event;
import com.soen345.ticketing.domain.event.EventStatus;
import com.soen345.ticketing.infrastructure.persistence.inmemory.InMemoryEventRepository;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class CancelEventUseCaseTest {
    private final InMemoryEventRepository eventRepository = new InMemoryEventRepository();
    private final CancelEventUseCase cancelEventUseCase = new CancelEventUseCase(eventRepository);

    private Event savePublishedEvent() {
        Event event = new Event(
                UUID.randomUUID(), "EVT-001", "Test Event", "Technology",
                "Description", "Venue", LocalDateTime.of(2026, 8, 1, 9, 0),
                LocalDateTime.of(2026, 8, 1, 17, 0), 100, 100,
                UUID.randomUUID(), EventStatus.PUBLISHED, 50.0
        );
        return eventRepository.save(event);
    }

    @Test
    void cancelsEventSuccessfully() {
        Event original = savePublishedEvent();

        Event cancelled = cancelEventUseCase.execute(original.id());

        assertEquals(EventStatus.CANCELLED, cancelled.status());
        assertEquals(original.id(), cancelled.id());
        assertEquals(original.title(), cancelled.title());
    }

    @Test
    void cancelledEventIsPersistedInRepository() {
        Event original = savePublishedEvent();

        cancelEventUseCase.execute(original.id());

        Event fromRepo = eventRepository.findById(original.id()).orElseThrow();
        assertEquals(EventStatus.CANCELLED, fromRepo.status());
    }

    @Test
    void cancelledEventNotInAvailableList() {
        Event original = savePublishedEvent();
        int availableBefore = eventRepository.listAvailable().size();

        cancelEventUseCase.execute(original.id());

        assertTrue(eventRepository.listAvailable().size() < availableBefore
                || !eventRepository.listAvailable().stream()
                        .anyMatch(e -> e.id().equals(original.id())));
    }

    @Test
    void cancelledEventStillInAllList() {
        Event original = savePublishedEvent();

        cancelEventUseCase.execute(original.id());

        assertTrue(eventRepository.listAll().stream()
                .anyMatch(e -> e.id().equals(original.id())));
    }

    @Test
    void rejectsNullEventId() {
        ValidationException ex = assertThrows(ValidationException.class,
                () -> cancelEventUseCase.execute(null));
        assertEquals("Event ID must not be null", ex.getMessage());
    }

    @Test
    void rejectsNonExistentEvent() {
        ValidationException ex = assertThrows(ValidationException.class,
                () -> cancelEventUseCase.execute(UUID.randomUUID()));
        assertEquals("Event not found", ex.getMessage());
    }

    @Test
    void rejectsAlreadyCancelledEvent() {
        Event original = savePublishedEvent();
        cancelEventUseCase.execute(original.id());

        ValidationException ex = assertThrows(ValidationException.class,
                () -> cancelEventUseCase.execute(original.id()));
        assertEquals("Event is already cancelled", ex.getMessage());
    }
}
