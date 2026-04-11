package com.soen345.ticketing.infrastructure.email;

import com.soen345.ticketing.application.reservation.ReservationConfirmation;
import com.soen345.ticketing.application.reservation.ReservationEmailService;

public class NoOpReservationEmailService implements ReservationEmailService {
    @Override
    public void sendReservationConfirmation(String recipientEmail, ReservationConfirmation confirmation) {
        // intentionally empty – used where email sending is not needed
    }
}
