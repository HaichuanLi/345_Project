package com.soen345.ticketing.android;

import android.content.Context;

import com.soen345.ticketing.application.reservation.ReservationEmailService;
import com.soen345.ticketing.application.reservation.ReservationConfirmationService;
import com.soen345.ticketing.domain.event.EventRepository;
import com.soen345.ticketing.domain.reservation.ReservationRepository;
import com.soen345.ticketing.infrastructure.persistence.filebased.FileBasedReservationConfirmationService;
import com.soen345.ticketing.infrastructure.persistence.inmemory.InMemoryEventRepository;
import com.soen345.ticketing.infrastructure.persistence.inmemory.InMemoryReservationRepository;

import java.io.File;

public final class TicketingDataProvider {
    private static final String CONFIRMATION_DIR = "reservation-confirmations";

    private static EventRepository eventRepository;
    private static ReservationRepository reservationRepository;
    private static ReservationConfirmationService confirmationService;
    private static ReservationEmailService reservationEmailService;

    private TicketingDataProvider() {
    }

    public static synchronized EventRepository eventRepository(Context context) {
        ensureInitialized(context);
        return eventRepository;
    }

    public static synchronized ReservationRepository reservationRepository(Context context) {
        ensureInitialized(context);
        return reservationRepository;
    }

    public static synchronized ReservationConfirmationService confirmationService(Context context) {
        ensureInitialized(context);
        return confirmationService;
    }

    public static synchronized ReservationEmailService reservationEmailService(Context context) {
        ensureInitialized(context);
        return reservationEmailService;
    }

    private static void ensureInitialized(Context context) {
        if (eventRepository != null
                && reservationRepository != null
                && confirmationService != null
                && reservationEmailService != null) {
            return;
        }

        eventRepository = new InMemoryEventRepository();
        reservationRepository = new InMemoryReservationRepository();

        File storageDir = new File(context.getFilesDir(), CONFIRMATION_DIR);
        confirmationService = new FileBasedReservationConfirmationService(storageDir.getAbsolutePath());

        reservationEmailService = new SmtpReservationEmailService(
            BuildConfig.SMTP_HOST,
            BuildConfig.SMTP_PORT,
            BuildConfig.SMTP_USERNAME,
            BuildConfig.SMTP_PASSWORD,
            BuildConfig.SMTP_FROM,
            BuildConfig.SMTP_TLS
        );
    }
}
