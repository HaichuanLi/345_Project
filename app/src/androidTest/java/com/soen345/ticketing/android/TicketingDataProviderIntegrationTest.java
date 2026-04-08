package com.soen345.ticketing.android;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.soen345.ticketing.application.reservation.EventDetailsDTO;
import com.soen345.ticketing.application.reservation.ReservationConfirmation;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class TicketingDataProviderIntegrationTest {
    private Context context;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        AndroidTestSupport.resetTicketingDataProvider();
    }

    @Test
    public void providerReturnsSameSingletonInstancesAcrossCalls() {
        Object eventRepository1 = TicketingDataProvider.eventRepository(context);
        Object eventRepository2 = TicketingDataProvider.eventRepository(context);
        Object reservationRepository1 = TicketingDataProvider.reservationRepository(context);
        Object reservationRepository2 = TicketingDataProvider.reservationRepository(context);
        Object confirmationService1 = TicketingDataProvider.confirmationService(context);
        Object confirmationService2 = TicketingDataProvider.confirmationService(context);

        assertSame(eventRepository1, eventRepository2);
        assertSame(reservationRepository1, reservationRepository2);
        assertSame(confirmationService1, confirmationService2);
    }

    @Test
    public void confirmationServicePersistsRoundTripThroughProvider() {
        ReservationConfirmation confirmation = sampleConfirmation();

        TicketingDataProvider.confirmationService(context).saveConfirmation(confirmation);
        var loaded = TicketingDataProvider.confirmationService(context)
                .getConfirmation(confirmation.reservationId());

        File confirmationFile = new File(
                new File(context.getFilesDir(), "reservation-confirmations"),
                confirmation.reservationId() + ".confirmation"
        );

        assertTrue(loaded.isPresent());
        assertEquals(confirmation, loaded.get());
        assertTrue(confirmationFile.exists());
    }

    private ReservationConfirmation sampleConfirmation() {
        return new ReservationConfirmation(
                UUID.randomUUID(),
                UUID.randomUUID(),
                2,
                new EventDetailsDTO(
                        UUID.randomUUID(),
                        "EVT-PROVIDER-001",
                        "Provider Confirmation Test",
                        "Technology",
                        "Provider integration test event",
                        "Test Venue",
                        LocalDateTime.of(2026, 12, 25, 9, 0),
                        LocalDateTime.of(2026, 12, 25, 11, 0),
                        100,
                        98,
                        45.0
                ),
                Instant.parse("2026-04-08T18:30:00Z"),
                "CONFIRMED"
        );
    }
}
