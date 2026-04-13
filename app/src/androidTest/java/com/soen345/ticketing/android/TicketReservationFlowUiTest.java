package com.soen345.ticketing.android;

import android.content.Context;
import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.soen345.ticketing.domain.event.Event;
import com.soen345.ticketing.domain.event.EventStatus;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.time.LocalDateTime;
import java.util.UUID;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasErrorText;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.not;

@RunWith(AndroidJUnit4.class)
public class TicketReservationFlowUiTest {

    @Test
    public void TicketReservationUI_UserCanEnterTicketQuantity() {
        Event event = saveUiTestEvent(40, 40, 55.0, "UI-ENTER-QUANTITY");

        try (ActivityScenario<EventDetailsActivity> ignored = launchEventDetails(event.id())) {
            onView(withId(R.id.ticketQuantityInput)).perform(replaceText("3"));
            onView(withId(R.id.ticketQuantityInput)).check(matches(withText("3")));
        }
    }

    @Test
    public void TicketReservationUI_UserCanSubmitReservation() {
        Event event = saveUiTestEvent(50, 50, 70.0, "UI-SUBMIT-RESERVATION");

        try (ActivityScenario<EventDetailsActivity> ignored = launchEventDetails(event.id())) {
            onView(withId(R.id.ticketQuantityInput)).perform(replaceText("2"));
            onView(withId(R.id.reserveTicketButton)).perform(click());

            AndroidTestSupport.waitForAssertion(() ->
                    onView(withId(R.id.confirmationTitle)).check(matches(isDisplayed()))
            );
        }
    }

    @Test
    public void TicketReservationUI_DisplayError_WhenSeatsInsufficient() {
        Event event = saveUiTestEvent(2, 2, 65.0, "UI-INSUFFICIENT-SEATS");

        try (ActivityScenario<EventDetailsActivity> ignored = launchEventDetails(event.id())) {
            onView(withId(R.id.ticketQuantityInput)).perform(replaceText("3"));
            onView(withId(R.id.reserveTicketButton)).perform(click());
            onView(withId(R.id.ticketQuantityInput)).check(matches(hasErrorText("Only 2 tickets are available.")));
        }
    }

    @Test
    public void ReservationConfirmationUI_DisplayReservationID() {
        Event event = saveUiTestEvent(40, 40, 80.0, "UI-CONFIRM-ID");

        try (ActivityScenario<EventDetailsActivity> ignored = launchEventDetails(event.id())) {
            onView(withId(R.id.ticketQuantityInput)).perform(replaceText("1"));
            onView(withId(R.id.reserveTicketButton)).perform(click());

            AndroidTestSupport.waitForAssertion(() ->
                    onView(withId(R.id.reservationIdValue)).check(matches(not(withText(""))))
            );
        }
    }

    @Test
    public void ReservationConfirmationUI_DisplayEventDetails() {
        Event event = saveUiTestEvent(35, 35, 99.0, "UI-CONFIRM-EVENT");

        try (ActivityScenario<EventDetailsActivity> ignored = launchEventDetails(event.id())) {
            onView(withId(R.id.ticketQuantityInput)).perform(replaceText("2"));
            onView(withId(R.id.reserveTicketButton)).perform(click());

            AndroidTestSupport.waitForAssertion(() -> {
                onView(withId(R.id.eventTitleValue)).check(matches(withText(event.title())));
                onView(withId(R.id.eventCodeValue)).check(matches(withText(event.eventCode())));
                onView(withId(R.id.eventCategoryValue)).check(matches(withText(event.category())));
                onView(withId(R.id.eventVenueValue)).check(matches(withText(event.venue())));
            });
        }
    }

    @Test
    public void ReservationConfirmationUI_DisplayNumberOfTicketsReserved() {
        Event event = saveUiTestEvent(30, 30, 110.0, "UI-CONFIRM-QTY");

        try (ActivityScenario<EventDetailsActivity> ignored = launchEventDetails(event.id())) {
            onView(withId(R.id.ticketQuantityInput)).perform(replaceText("4"));
            onView(withId(R.id.reserveTicketButton)).perform(click());

            AndroidTestSupport.waitForAssertion(() ->
                    onView(withId(R.id.ticketsReservedValue)).check(matches(withText("4")))
            );
        }
    }

    @Test
    public void ReservationConfirmationUI_DisplayTotalPrice() {
        Event event = saveUiTestEvent(25, 25, 123.45, "UI-CONFIRM-TOTAL");

        try (ActivityScenario<EventDetailsActivity> ignored = launchEventDetails(event.id())) {
            onView(withId(R.id.ticketQuantityInput)).perform(replaceText("2"));
            onView(withId(R.id.reserveTicketButton)).perform(click());

            AndroidTestSupport.waitForAssertion(() ->
                    onView(withId(R.id.orderTotalValue)).check(matches(withText("$246.90")))
            );
        }
    }

    @Test
    public void ReservationConfirmationUI_DisplayReservationTime() {
        Event event = saveUiTestEvent(20, 20, 88.0, "UI-CONFIRM-TIME");

        try (ActivityScenario<EventDetailsActivity> ignored = launchEventDetails(event.id())) {
            onView(withId(R.id.ticketQuantityInput)).perform(replaceText("1"));
            onView(withId(R.id.reserveTicketButton)).perform(click());

            AndroidTestSupport.waitForAssertion(() ->
                    onView(withId(R.id.reservedAtValue)).check(matches(not(withText(""))))
            );
        }
    }

    @Test
    public void ReservationConfirmationUI_DisplayConfirmationPageAfterReservation() {
        Event event = saveUiTestEvent(15, 15, 52.0, "UI-CONFIRM-PAGE");

        try (ActivityScenario<EventDetailsActivity> ignored = launchEventDetails(event.id())) {
            onView(withId(R.id.ticketQuantityInput)).perform(replaceText("1"));
            onView(withId(R.id.reserveTicketButton)).perform(click());

            AndroidTestSupport.waitForAssertion(() -> {
                onView(withId(R.id.confirmationTitle)).check(matches(withText("Reservation Confirmation")));
                onView(withId(R.id.reservationIdValue)).check(matches(isDisplayed()));
            });
        }
    }

    private Event saveUiTestEvent(int capacity, int availableTickets, double price, String codePrefix) {
        Context context = ApplicationProvider.getApplicationContext();
        Event event = new Event(
                UUID.randomUUID(),
                "EVT-" + codePrefix,
                "UI Test Event " + codePrefix,
                "Technology",
                "Event used by UI instrumentation tests",
                "UI Test Venue",
                LocalDateTime.of(2026, 9, 1, 10, 0),
                LocalDateTime.of(2026, 9, 1, 12, 0),
                capacity,
                availableTickets,
                UUID.randomUUID(),
                EventStatus.PUBLISHED,
                price
        );
        TicketingDataProvider.eventRepository(context).save(event);
        return event;
    }

    private ActivityScenario<EventDetailsActivity> launchEventDetails(UUID eventId) {
        Context context = ApplicationProvider.getApplicationContext();
        Intent intent = new Intent(context, EventDetailsActivity.class);
        intent.putExtra(EventDetailsActivity.EXTRA_EVENT_ID, eventId.toString());
        intent.putExtra(EventDetailsActivity.EXTRA_USER_ID, UUID.randomUUID().toString());
        return ActivityScenario.launch(intent);
    }
}
