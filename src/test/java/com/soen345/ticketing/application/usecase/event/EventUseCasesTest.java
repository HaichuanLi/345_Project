package com.soen345.ticketing.application.usecase.event;

import com.soen345.ticketing.application.event.EventDetailsViewDTO;
import com.soen345.ticketing.application.event.TicketOrderDTO;
import com.soen345.ticketing.domain.event.Event;
import com.soen345.ticketing.domain.event.EventStatus;
import com.soen345.ticketing.infrastructure.persistence.inmemory.InMemoryEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
public class EventUseCasesTest {
    private InMemoryEventRepository eventRepository;
    private final LocalDateTime start = LocalDateTime.of(2026, 8, 1, 9, 0);
    private final LocalDateTime end = LocalDateTime.of(2026, 8, 1, 17, 0);

    @BeforeEach
    void setUp() {
        eventRepository = new InMemoryEventRepository();
    }

    private Event saveEvent(int availableTickets, double price) {
        Event event = new Event(
                UUID.randomUUID(), "EVT-001", "Tech Conference", "Technology",
                "A great event", "Main Hall", start, end,
                100, availableTickets, UUID.randomUUID(), EventStatus.PUBLISHED, price
        );
        return eventRepository.save(event);
    }

    //CalculateEventTicketOrderUseCase

    @Test
    void calculateOrderReturnsCorrectDTO() {
        Event event = saveEvent(50, 25.0);
        CalculateEventTicketOrderUseCase useCase = new CalculateEventTicketOrderUseCase(eventRepository);

        Optional<TicketOrderDTO> result = useCase.calculateOrder(event.id(), 3);

        assertTrue(result.isPresent());
        assertEquals(3, result.get().quantityRequested());
        assertEquals(25.0, result.get().pricePerTicket());
        assertEquals(75.0, result.get().orderTotal(), 0.001);
        assertTrue(result.get().isValid());
    }

    @Test
    void calculateOrderReturnsEmptyForUnknownEvent() {
        CalculateEventTicketOrderUseCase useCase = new CalculateEventTicketOrderUseCase(eventRepository);

        Optional<TicketOrderDTO> result = useCase.calculateOrder(UUID.randomUUID(), 3);

        assertFalse(result.isPresent());
    }

    @Test
    void calculateOrderRejectsNullEventId() {
        CalculateEventTicketOrderUseCase useCase = new CalculateEventTicketOrderUseCase(eventRepository);

        assertThrows(NullPointerException.class, () -> useCase.calculateOrder(null, 3));
    }

    @Test
    void calculateOrderOrThrowThrowsForUnknownEvent() {
        CalculateEventTicketOrderUseCase useCase = new CalculateEventTicketOrderUseCase(eventRepository);

        assertThrows(RuntimeException.class,
                () -> useCase.calculateOrderOrThrow(UUID.randomUUID(), 3));
    }

    @Test
    void calculateOrderOrThrowReturnsForKnownEvent() {
        Event event = saveEvent(50, 25.0);
        CalculateEventTicketOrderUseCase useCase = new CalculateEventTicketOrderUseCase(eventRepository);

        TicketOrderDTO result = useCase.calculateOrderOrThrow(event.id(), 2);

        assertNotNull(result);
        assertEquals(2, result.quantityRequested());
    }

    @Test
    void validateOrderReturnsNullErrorForValidOrder() {
        Event event = saveEvent(50, 25.0);
        CalculateEventTicketOrderUseCase useCase = new CalculateEventTicketOrderUseCase(eventRepository);

        Optional<String> error = useCase.validateOrder(event.id(), 3);

        assertFalse(error.isPresent());
    }

    @Test
    void validateOrderReturnsErrorWhenQuantityExceedsSeats() {
        Event event = saveEvent(2, 25.0);
        CalculateEventTicketOrderUseCase useCase = new CalculateEventTicketOrderUseCase(eventRepository);

        Optional<String> error = useCase.validateOrder(event.id(), 5);

        assertTrue(error.isPresent());
        assertNotNull(error.get());
        assertTrue(error.get().contains("seats"));
    }

    @Test
    void isOrderValidReturnsTrueForValidOrder() {
        Event event = saveEvent(50, 25.0);
        CalculateEventTicketOrderUseCase useCase = new CalculateEventTicketOrderUseCase(eventRepository);

        assertTrue(useCase.isOrderValid(event.id(), 3));
    }

    @Test
    void isOrderValidReturnsFalseForUnknownEvent() {
        CalculateEventTicketOrderUseCase useCase = new CalculateEventTicketOrderUseCase(eventRepository);

        assertFalse(useCase.isOrderValid(UUID.randomUUID(), 3));
    }

    @Test
    void isOrderValidReturnsFalseWhenNotEnoughSeats() {
        Event event = saveEvent(2, 25.0);
        CalculateEventTicketOrderUseCase useCase = new CalculateEventTicketOrderUseCase(eventRepository);

        assertFalse(useCase.isOrderValid(event.id(), 5));
    }

    //ViewEventDetailsUseCase

    @Test
    void getEventDetailsReturnsCorrectDTO() {
        Event event = saveEvent(80, 49.99);
        ViewEventDetailsUseCase useCase = new ViewEventDetailsUseCase(eventRepository);

        Optional<EventDetailsViewDTO> result = useCase.getEventDetails(event.id());

        assertTrue(result.isPresent());
        assertEquals(event.id(), result.get().eventId());
        assertEquals("Tech Conference", result.get().eventName());
        assertEquals(49.99, result.get().pricePerTicket());
        assertEquals(80, result.get().availableSeats());
    }

    @Test
    void getEventDetailsReturnsEmptyForUnknownEvent() {
        ViewEventDetailsUseCase useCase = new ViewEventDetailsUseCase(eventRepository);

        Optional<EventDetailsViewDTO> result = useCase.getEventDetails(UUID.randomUUID());

        assertFalse(result.isPresent());
    }

    @Test
    void getEventDetailsOrThrowThrowsForUnknownEvent() {
        ViewEventDetailsUseCase useCase = new ViewEventDetailsUseCase(eventRepository);

        assertThrows(RuntimeException.class,
                () -> useCase.getEventDetailsOrThrow(UUID.randomUUID()));
    }

    @Test
    void getEventDetailsOrThrowReturnsForKnownEvent() {
        Event event = saveEvent(80, 49.99);
        ViewEventDetailsUseCase useCase = new ViewEventDetailsUseCase(eventRepository);

        EventDetailsViewDTO result = useCase.getEventDetailsOrThrow(event.id());

        assertNotNull(result);
        assertEquals(event.id(), result.eventId());
    }
}
