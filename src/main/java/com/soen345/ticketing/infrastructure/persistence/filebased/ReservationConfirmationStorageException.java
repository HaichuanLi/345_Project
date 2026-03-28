package com.soen345.ticketing.infrastructure.persistence.filebased;

public class ReservationConfirmationStorageException extends RuntimeException {
    public ReservationConfirmationStorageException(String message) {
        super(message);
    }

    public ReservationConfirmationStorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
