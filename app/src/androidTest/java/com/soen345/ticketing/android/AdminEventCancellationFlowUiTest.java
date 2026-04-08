package com.soen345.ticketing.android;

import android.content.Context;
import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.soen345.ticketing.domain.event.Event;
import com.soen345.ticketing.domain.event.EventStatus;

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
public class AdminEventCancellationFlowUiTest {

    @Test
    public void adminCanCancelOwnEventFromEventDetails() throws Exception {
        Context context = ApplicationProvider.getApplicationContext();
        UUID adminId = UUID.randomUUID();
        Event event = saveAdminOwnedEvent(context, adminId);

        Intent intent = new Intent(context, EventDetailsActivity.class);
        intent.putExtra(EventDetailsActivity.EXTRA_EVENT_ID, event.id().toString());
        intent.putExtra(EventDetailsActivity.EXTRA_USER_ID, adminId.toString());
        intent.putExtra(EventDetailsActivity.EXTRA_ROLE, "ADMIN");

        try (ActivityScenario<EventDetailsActivity> ignored = ActivityScenario.launch(intent)) {
            onView(withId(R.id.cancelEventButton)).check(matches(isDisplayed()));
            onView(withId(R.id.cancelEventButton)).perform(click());
            onView(withText("Yes, Cancel")).perform(click());
        }

        waitForEventStatus(context, event.id(), EventStatus.CANCELLED);

        var updated = TicketingDataProvider.eventRepository(context).findById(event.id()).orElseThrow();
        assertEquals(EventStatus.CANCELLED, updated.status());
    }

    private Event saveAdminOwnedEvent(Context context, UUID organizerId) {
        Event event = new Event(
                UUID.randomUUID(),
                "EVT-ADMIN-" + UUID.randomUUID().toString().substring(0, 8),
                "Admin Cancellation Flow Event",
                "Technology",
                "Event used by admin cancellation UI test",
                "Admin Hall",
                LocalDateTime.of(2026, 12, 12, 9, 0),
                LocalDateTime.of(2026, 12, 12, 11, 0),
                50,
                50,
                organizerId,
                EventStatus.PUBLISHED,
                25.0
        );
        TicketingDataProvider.eventRepository(context).save(event);
        return event;
    }

    private void waitForEventStatus(Context context, UUID eventId, EventStatus expectedStatus) throws Exception {
        long deadline = System.currentTimeMillis() + 5000;
        while (System.currentTimeMillis() < deadline) {
            InstrumentationRegistry.getInstrumentation().waitForIdleSync();
            var event = TicketingDataProvider.eventRepository(context).findById(eventId);
            if (event.isPresent() && event.get().status() == expectedStatus) {
                return;
            }
            Thread.sleep(100);
        }
        throw new AssertionError("Timed out waiting for event status " + expectedStatus);
    }
}
