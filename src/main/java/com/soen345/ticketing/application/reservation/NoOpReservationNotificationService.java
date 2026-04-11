package com.soen345.ticketing.application.reservation;

public class NoOpReservationNotificationService implements ReservationNotificationService {
    @Override
    public void sendReservationConfirmation(String recipientEmail, ReservationConfirmation confirmation) {
        // Intentionally no-op.
    }
}