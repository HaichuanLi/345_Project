package com.soen345.ticketing.application.reservation;

public class InsufficientSeatsException extends RuntimeException {
    private final int requestedQuantity;
    private final int availableSeats;

    public InsufficientSeatsException(int requestedQuantity, int availableSeats) {
        super(String.format("Insufficient seats. Requested: %d, Available: %d",
                requestedQuantity, availableSeats));
        this.requestedQuantity = requestedQuantity;
        this.availableSeats = availableSeats;
    }

    public int getRequestedQuantity() {
        return requestedQuantity;
    }

    public int getAvailableSeats() {
        return availableSeats;
    }
}
