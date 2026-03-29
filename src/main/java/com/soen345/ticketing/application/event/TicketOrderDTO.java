package com.soen345.ticketing.application.event;

import java.util.Objects;

public record TicketOrderDTO(
        int quantityRequested,
        double pricePerTicket,
        double orderTotal,
        int availableSeats,
        boolean isValid
) {
    public TicketOrderDTO {
        if (quantityRequested < 0) {
            throw new IllegalArgumentException("quantityRequested must not be negative");
        }
        if (pricePerTicket < 0) {
            throw new IllegalArgumentException("pricePerTicket must not be negative");
        }
        if (orderTotal < 0) {
            throw new IllegalArgumentException("orderTotal must not be negative");
        }
        if (availableSeats < 0) {
            throw new IllegalArgumentException("availableSeats must not be negative");
        }
    }

    /**
     * Create a ticket order with validation
     */
    public static TicketOrderDTO create(
            int quantityRequested,
            double pricePerTicket,
            int availableSeats
    ) {
        Objects.requireNonNull(pricePerTicket, "pricePerTicket must not be null");

        double orderTotal = pricePerTicket * quantityRequested;
        boolean isValid = quantityRequested > 0 && quantityRequested <= availableSeats;

        return new TicketOrderDTO(
                quantityRequested,
                pricePerTicket,
                orderTotal,
                availableSeats,
                isValid
        );
    }

    /**
     * Get validation error message if order is invalid
     */
    public String getValidationError() {
        if (quantityRequested <= 0) {
            return "Quantity must be greater than zero";
        }
        if (quantityRequested > availableSeats) {
            return "Not enough seats available. Only " + availableSeats + " seats left.";
        }
        return null;
    }

    /**
     * Get a summary of the order
     */
    public String getOrderSummary() {
        return String.format(
                "Order Summary: %d ticket(s) @ $%.2f each = $%.2f total",
                quantityRequested,
                pricePerTicket,
                orderTotal
        );
    }
}
