package com.soen345.ticketing.application.reservation;

public interface ReservationNotificationService {
    void sendReservationConfirmation(String recipientEmail, ReservationConfirmation confirmation);
}