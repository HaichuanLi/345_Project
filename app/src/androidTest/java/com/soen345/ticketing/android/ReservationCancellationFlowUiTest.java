package com.soen345.ticketing.android;

import android.content.Context;
import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.soen345.ticketing.application.reservation.ReserveTicketsCommand;
import com.soen345.ticketing.application.reservation.ReserveTicketsValidator;
import com.soen345.ticketing.application.usecase.reservation.ReserveTicketsUseCase;
import com.soen345.ticketing.infrastructure.email.NoOpReservationEmailService;
import com.soen345.ticketing.infrastructure.persistence.inmemory.InMemoryUserRepository;
import com.soen345.ticketing.domain.event.Event;
import com.soen345.ticketing.domain.event.EventStatus;
import com.soen345.ticketing.domain.reservation.ReservationStatus;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.time.LocalDateTime;
import java.util.UUID;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class ReservationCancellationFlowUiTest {

    @Test
    public void cancellingReservationRestoresTicketsAndMarksReservationCancelled() {
        Context context = ApplicationProvider.getApplicationContext();
        UUID userId = UUID.randomUUID();
        Event event = saveEvent(context, 20, 20, UUID.randomUUID());
        var confirmation = reserveTickets(context, userId, event.id(), 4);

        Intent intent = new Intent(context, RegisteredEventDetailsActivity.class);
        intent.putExtra(RegisteredEventDetailsActivity.EXTRA_RESERVATION_ID, confirmation.reservationId().toString());
        intent.putExtra(RegisteredEventDetailsActivity.EXTRA_USER_ID, userId.toString());

        try (ActivityScenario<RegisteredEventDetailsActivity> ignored = ActivityScenario.launch(intent)) {
            onView(withId(R.id.cancelReservationButton)).check(matches(isDisplayed()));
            onView(withId(R.id.cancelReservationButton)).perform(click());
            onView(withText("Yes, Cancel")).perform(click());
        }

        InstrumentationRegistry.getInstrumentation().waitForIdleSync();

        var reservation = TicketingDataProvider.reservationRepository(context)
                .findById(confirmation.reservationId())
                .orElseThrow();
        var updatedEvent = TicketingDataProvider.eventRepository(context)
                .findById(event.id())
                .orElseThrow();

        assertEquals(ReservationStatus.CANCELLED, reservation.status());
        assertEquals(20, updatedEvent.availableTickets());
    }

    private Event saveEvent(Context context, int capacity, int availableTickets, UUID organizerId) {
        Event event = new Event(
                UUID.randomUUID(),
                "EVT-CANCEL-" + UUID.randomUUID().toString().substring(0, 8),
                "Cancellation Flow Event",
                "Technology",
                "Event used by reservation cancellation UI test",
                "Test Venue",
                LocalDateTime.of(2026, 12, 10, 10, 0),
                LocalDateTime.of(2026, 12, 10, 12, 0),
                capacity,
                availableTickets,
                organizerId,
                EventStatus.PUBLISHED,
                70.0
        );
        TicketingDataProvider.eventRepository(context).save(event);
        return event;
    }

    private com.soen345.ticketing.application.reservation.ReservationConfirmation reserveTickets(
            Context context,
            UUID userId,
            UUID eventId,
            int quantity
    ) {
        ReserveTicketsUseCase useCase = new ReserveTicketsUseCase(
                TicketingDataProvider.eventRepository(context),
                TicketingDataProvider.reservationRepository(context),
                TicketingDataProvider.confirmationService(context),
                new ReserveTicketsValidator(),
                new InMemoryUserRepository(),
                new NoOpReservationEmailService()
        );
        return useCase.reserve(new ReserveTicketsCommand(userId, eventId, quantity));
    }
}
