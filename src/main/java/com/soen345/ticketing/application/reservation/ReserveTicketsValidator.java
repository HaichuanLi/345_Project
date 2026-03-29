package com.soen345.ticketing.application.reservation;

import com.soen345.ticketing.application.auth.ValidationException;

public class ReserveTicketsValidator {
    public void validate(ReserveTicketsCommand command) {
        if (command.quantity() <= 0) {
            throw new ValidationException("Quantity must be greater than zero");
        }

        if (command.quantity() > 100) {
            throw new ValidationException("Cannot reserve more than 100 tickets at once");
        }
    }
}
