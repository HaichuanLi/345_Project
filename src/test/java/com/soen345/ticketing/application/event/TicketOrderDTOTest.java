package com.soen345.ticketing.application.event;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
public class TicketOrderDTOTest {
    @Test
    void createValidOrder() {
        TicketOrderDTO dto = TicketOrderDTO.create(3, 50.0, 100);

        assertEquals(3, dto.quantityRequested());
        assertEquals(50.0, dto.pricePerTicket());
        assertEquals(150.0, dto.orderTotal(), 0.001);
        assertEquals(100, dto.availableSeats());
        assertTrue(dto.isValid());
    }

    @Test
    void createInvalidOrderWhenQuantityExceedsSeats() {
        TicketOrderDTO dto = TicketOrderDTO.create(10, 50.0, 5);

        assertFalse(dto.isValid());
        assertNotNull(dto.getValidationError());
        assertTrue(dto.getValidationError().contains("Not enough seats"));
    }

    @Test
    void createInvalidOrderWhenQuantityIsZero() {
        TicketOrderDTO dto = TicketOrderDTO.create(0, 50.0, 100);

        assertFalse(dto.isValid());
        assertEquals("Quantity must be greater than zero", dto.getValidationError());
    }

    @Test
    void getValidationErrorReturnsNullForValidOrder() {
        TicketOrderDTO dto = TicketOrderDTO.create(2, 50.0, 100);

        assertNull(dto.getValidationError());
    }

    @Test
    void getOrderSummaryContainsKeyInfo() {
        TicketOrderDTO dto = TicketOrderDTO.create(2, 25.0, 100);
        String summary = dto.getOrderSummary();

        assertTrue(summary.contains("2"));
        assertTrue(summary.contains("25.00"));
        assertTrue(summary.contains("50.00"));
    }

    @Test
    void rejectsNegativeQuantity() {
        assertThrows(IllegalArgumentException.class,
                () -> new TicketOrderDTO(-1, 50.0, 0.0, 100, false));
    }

    @Test
    void rejectsNegativePrice() {
        assertThrows(IllegalArgumentException.class,
                () -> new TicketOrderDTO(1, -1.0, 0.0, 100, false));
    }

    @Test
    void rejectsNegativeOrderTotal() {
        assertThrows(IllegalArgumentException.class,
                () -> new TicketOrderDTO(1, 50.0, -1.0, 100, false));
    }

    @Test
    void rejectsNegativeAvailableSeats() {
        assertThrows(IllegalArgumentException.class,
                () -> new TicketOrderDTO(1, 50.0, 50.0, -1, false));
    }
}
