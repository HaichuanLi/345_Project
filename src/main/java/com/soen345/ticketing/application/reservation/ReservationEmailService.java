package com.soen345.ticketing.application.reservation;

public interface ReservationEmailService {
    void sendReservationConfirmation(String recipientEmail, ReservationConfirmation confirmation);
}
