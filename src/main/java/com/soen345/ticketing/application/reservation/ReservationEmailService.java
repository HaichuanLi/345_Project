package com.soen345.ticketing.application.reservation;

/**
 * Sends reservation confirmation emails.
 */
public interface ReservationEmailService {
    void sendReservationConfirmation(String recipientEmail, ReservationConfirmation confirmation);
}
