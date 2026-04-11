package com.soen345.ticketing.android;

import android.content.Context;
import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.soen345.ticketing.application.reservation.ReserveTicketsCommand;
import com.soen345.ticketing.application.reservation.ReserveTicketsValidator;
import com.soen345.ticketing.application.usecase.reservation.ReserveTicketsUseCase;
import com.soen345.ticketing.infrastructure.email.NoOpReservationEmailService;
import com.soen345.ticketing.infrastructure.persistence.inmemory.InMemoryUserRepository;
import com.soen345.ticketing.domain.event.Event;
import com.soen345.ticketing.domain.event.EventStatus;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.time.LocalDateTime;
import java.util.UUID;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

@RunWith(AndroidJUnit4.class)
public class PersistenceAcrossRelaunchUiTest {

    @Test
    public void reservationConfirmationPersistsAcrossRecreateAndRelaunch() {
        Context context = ApplicationProvider.getApplicationContext();
        Event event = saveEvent(context);
        var confirmation = reserveTickets(context, event.id(), 2);

        Intent intent = new Intent(context, ReservationConfirmationActivity.class);
        intent.putExtra(ReservationConfirmationActivity.EXTRA_RESERVATION_ID, confirmation.reservationId().toString());

        try (ActivityScenario<ReservationConfirmationActivity> scenario = ActivityScenario.launch(intent)) {
            AndroidTestSupport.waitForAssertion(() ->
                    onView(withId(R.id.eventTitleValue)).check(matches(withText(event.title())))
            );
            scenario.recreate();
            AndroidTestSupport.waitForAssertion(() ->
                    onView(withId(R.id.ticketsReservedValue)).check(matches(withText("2")))
            );
        }

        try (ActivityScenario<ReservationConfirmationActivity> ignored = ActivityScenario.launch(intent)) {
            AndroidTestSupport.waitForAssertion(() ->
                    onView(withId(R.id.eventCodeValue)).check(matches(withText(event.eventCode())))
            );
        }
    }

    private Event saveEvent(Context context) {
        Event event = new Event(
                UUID.randomUUID(),
                "EVT-PERSIST-" + UUID.randomUUID().toString().substring(0, 8),
                "Persistence Test Event",
                "Technology",
                "Event used to verify confirmation persistence",
                "Persistence Hall",
                LocalDateTime.of(2026, 12, 30, 10, 0),
                LocalDateTime.of(2026, 12, 30, 12, 0),
                25,
                25,
                UUID.randomUUID(),
                EventStatus.PUBLISHED,
                65.0
        );
        TicketingDataProvider.eventRepository(context).save(event);
        return event;
    }

    private com.soen345.ticketing.application.reservation.ReservationConfirmation reserveTickets(
            Context context,
            UUID eventId,
            int quantity
    ) {
        return new ReserveTicketsUseCase(
                TicketingDataProvider.eventRepository(context),
                TicketingDataProvider.reservationRepository(context),
                TicketingDataProvider.confirmationService(context),
                new ReserveTicketsValidator(),
                new InMemoryUserRepository(),
                new NoOpReservationEmailService()
        ).reserve(new ReserveTicketsCommand(UUID.randomUUID(), eventId, quantity));
    }
}
