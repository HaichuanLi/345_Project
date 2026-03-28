package com.soen345.ticketing.application.reservation;

public class SeatAvailabilityService {
    public boolean checkSeats(int requestedTickets, int availableSeats) {
        if (requestedTickets <= 0) {
            return false;
        }
        return requestedTickets <= availableSeats;
    }
}
