package com.soen345.ticketing.application.performance;

import com.soen345.ticketing.application.reservation.ReservationConfirmation;
import com.soen345.ticketing.application.reservation.ReservationConfirmationService;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
public class FakeConfirmationService implements ReservationConfirmationService {
    private final Map<UUID, ReservationConfirmation> store = new HashMap<>();

    @Override
    public void saveConfirmation(ReservationConfirmation confirmation) {
        store.put(confirmation.reservationId(), confirmation);
    }

    @Override
    public Optional<ReservationConfirmation> getConfirmation(UUID reservationId) {
        return Optional.ofNullable(store.get(reservationId));
    }
}
