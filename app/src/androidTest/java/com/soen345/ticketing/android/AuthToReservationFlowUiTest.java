package com.soen345.ticketing.android;

import android.content.Context;
import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.soen345.ticketing.domain.event.Event;
import com.soen345.ticketing.domain.event.EventStatus;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.time.LocalDateTime;
import java.util.UUID;

import static androidx.test.espresso.Espresso.closeSoftKeyboard;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

@RunWith(AndroidJUnit4.class)
public class AuthToReservationFlowUiTest {

    @Test
    public void registerLoginReserveAndSeeReservationInRegisteredEvents() {
        Context context = ApplicationProvider.getApplicationContext();
        String uniqueToken = UUID.randomUUID().toString().substring(0, 8);
        String email = "flow+" + uniqueToken + "@test.com";
        String password = "secret123";
        String name = "Flow User " + uniqueToken;
        Event event = saveEvent(context, uniqueToken);

        try (ActivityScenario<RegisterActivity> ignored = ActivityScenario.launch(RegisterActivity.class)) {
            onView(withId(R.id.nameInput)).perform(replaceText(name), closeSoftKeyboard());
            onView(withId(R.id.roleCustomer)).perform(click());
            onView(withId(R.id.radioEmail)).perform(click());
            onView(withId(R.id.emailInput)).perform(replaceText(email), closeSoftKeyboard());
            onView(withId(R.id.passwordInput)).perform(replaceText(password), closeSoftKeyboard());
            onView(withId(R.id.registerButton)).perform(click());

            AndroidTestSupport.waitForAssertion(() ->
                    onView(withId(R.id.successText)).check(matches(withText("Registration successful! You can now log in.")))
            );
        }

        try (ActivityScenario<MainActivity> ignored = ActivityScenario.launch(MainActivity.class)) {
            onView(withId(R.id.identifierInput)).perform(replaceText(email), closeSoftKeyboard());
            onView(withId(R.id.passwordInput)).perform(replaceText(password), closeSoftKeyboard());
            onView(withId(R.id.loginButton)).perform(click());

            AndroidTestSupport.waitForAssertion(() ->
                    onView(withId(R.id.resultTitle)).check(matches(withText("Login successful")))
            );

            onView(withId(R.id.viewEventsButton)).perform(click());

            AndroidTestSupport.waitForAssertion(() ->
                    onView(withId(R.id.eventsRecyclerView)).perform(
                            RecyclerViewActions.scrollTo(hasDescendant(withText(event.title())))
                    )
            );
            onView(withText(event.title())).perform(click());

            AndroidTestSupport.waitForAssertion(() ->
                    onView(withId(R.id.ticketQuantityInput)).check(matches(isDisplayed()))
            );
            onView(withId(R.id.ticketQuantityInput)).perform(replaceText("2"), closeSoftKeyboard());
            onView(withId(R.id.reserveTicketButton)).perform(click());

            AndroidTestSupport.waitForAssertion(() ->
                    onView(withId(R.id.confirmationTitle)).check(matches(withText("Reservation Confirmation")))
            );
        }

        UUID userId = new FirestoreUserRepository().findByEmail(email).orElseThrow().id();
        Intent registeredEventsIntent = new Intent(context, RegisteredEventsActivity.class);
        registeredEventsIntent.putExtra(RegisteredEventsActivity.EXTRA_USER_ID, userId.toString());

        try (ActivityScenario<RegisteredEventsActivity> ignored = ActivityScenario.launch(registeredEventsIntent)) {
            AndroidTestSupport.waitForAssertion(() ->
                    onView(withText(event.title())).check(matches(isDisplayed()))
            );
        }
    }

    private Event saveEvent(Context context, String token) {
        Event event = new Event(
                UUID.randomUUID(),
                "EVT-AUTH-" + token,
                "Auth Flow Event " + token,
                "Technology",
                "Event used by full auth to reservation flow",
                "Auth Hall",
                LocalDateTime.of(2026, 12, 27, 10, 0),
                LocalDateTime.of(2026, 12, 27, 12, 0),
                20,
                20,
                UUID.randomUUID(),
                EventStatus.PUBLISHED,
                35.0
        );
        TicketingDataProvider.eventRepository(context).save(event);
        return event;
    }
}
