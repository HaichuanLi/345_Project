package com.soen345.ticketing.android;

import android.content.Context;
import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.soen345.ticketing.domain.event.Event;
import com.soen345.ticketing.domain.event.EventRepository;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

@RunWith(AndroidJUnit4.class)
public class EventDetailsComponentTest {
    private static final DateTimeFormatter DISPLAY_DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm", Locale.CANADA);

    private Event selectedEvent;

    @Before
    public void setupEvent() {
        Context context = ApplicationProvider.getApplicationContext();
        EventRepository repository = TicketingDataProvider.eventRepository(context);
        List<Event> events = repository.listAvailable();
        if (events.isEmpty()) {
            throw new IllegalStateException("No available events found for UI test setup.");
        }
        selectedEvent = events.get(0);
    }

    @Test
    public void EventDetailsComponent_DisplayEventName() {
        try (ActivityScenario<EventDetailsActivity> ignored = launchEventDetails()) {
            onView(withId(R.id.eventTitle)).check(matches(withText(selectedEvent.title())));
        }
    }

    @Test
    public void EventDetailsComponent_DisplayEventCode() {
        try (ActivityScenario<EventDetailsActivity> ignored = launchEventDetails()) {
            onView(withId(R.id.eventCode)).check(matches(withText(selectedEvent.eventCode())));
        }
    }

    @Test
    public void EventDetailsComponent_DisplayStartTime() {
        try (ActivityScenario<EventDetailsActivity> ignored = launchEventDetails()) {
            String expectedStart = selectedEvent.startDateTime().format(DISPLAY_DATE_TIME);
            onView(withId(R.id.eventStartTime)).check(matches(withText(expectedStart)));
        }
    }

    @Test
    public void EventDetailsComponent_DisplayEndTime() {
        try (ActivityScenario<EventDetailsActivity> ignored = launchEventDetails()) {
            String expectedEnd = selectedEvent.endDateTime().format(DISPLAY_DATE_TIME);
            onView(withId(R.id.eventEndTime)).check(matches(withText(expectedEnd)));
        }
    }

    @Test
    public void EventDetailsComponent_DisplayLocation() {
        try (ActivityScenario<EventDetailsActivity> ignored = launchEventDetails()) {
            onView(withId(R.id.eventVenue)).check(matches(withText(selectedEvent.venue())));
        }
    }

    @Test
    public void EventDetailsComponent_DisplayCategory() {
        try (ActivityScenario<EventDetailsActivity> ignored = launchEventDetails()) {
            onView(withId(R.id.eventCategory)).check(matches(withText(selectedEvent.category())));
        }
    }

    @Test
    public void EventDetailsComponent_DisplayAvailableSeats() {
        try (ActivityScenario<EventDetailsActivity> ignored = launchEventDetails()) {
            onView(withId(R.id.availableSeats)).check(matches(withText(String.valueOf(selectedEvent.capacity()))));
        }
    }

    private ActivityScenario<EventDetailsActivity> launchEventDetails() {
        Context context = ApplicationProvider.getApplicationContext();
        Intent intent = new Intent(context, EventDetailsActivity.class);
        intent.putExtra(EventDetailsActivity.EXTRA_EVENT_ID, selectedEvent.id().toString());
        intent.putExtra(EventDetailsActivity.EXTRA_USER_ID, UUID.randomUUID().toString());
        return ActivityScenario.launch(intent);
    }
}
