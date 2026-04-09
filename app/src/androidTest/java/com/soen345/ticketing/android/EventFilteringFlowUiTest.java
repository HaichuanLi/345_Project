package com.soen345.ticketing.android;

import android.content.Context;
import android.content.Intent;

import androidx.recyclerview.widget.RecyclerView;
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

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

@RunWith(AndroidJUnit4.class)
public class EventFilteringFlowUiTest {

    @Test
    public void filteringByCategoryShowsMatchingEventAndHidesOthers() {
        Context context = ApplicationProvider.getApplicationContext();
        Event securityEvent = saveEvent(context, "UI Filter Security Event", "Security", "Filter Hall A");
        saveEvent(context, "UI Filter Technology Event", "Technology", "Filter Hall B");

        Intent intent = new Intent(context, EventListActivity.class);
        intent.putExtra(EventListActivity.EXTRA_USER_ID, UUID.randomUUID().toString());
        intent.putExtra(EventListActivity.EXTRA_ROLE, "CUSTOMER");

        try (ActivityScenario<EventListActivity> ignored = ActivityScenario.launch(intent)) {
            AndroidTestSupport.waitForAssertion(() ->
                    onView(withId(R.id.eventsRecyclerView)).check(matches(isDisplayed()))
            );

            onView(withId(R.id.filterCategoryInput)).perform(click());
            onData(allOf(is(instanceOf(String.class)), is("Security"))).perform(click());

            AndroidTestSupport.waitForAssertion(() ->
                    onView(withId(R.id.eventsRecyclerView)).perform(
                            RecyclerViewActions.scrollTo(hasDescendant(withText(securityEvent.title())))
                    )
            );
            onView(withText(securityEvent.title())).check(matches(isDisplayed()));
            onView(withText("UI Filter Technology Event")).check(doesNotExist());
        }
    }

    @Test
    public void filteringByLocationAndClearingFiltersRestoresList() {
        Context context = ApplicationProvider.getApplicationContext();
        Event locationEvent = saveEvent(context, "UI Filter Location Event", "Technology", "Filter Unique Venue");
        saveEvent(context, "UI Filter Other Venue Event", "Technology", "Filter Other Venue");

        Intent intent = new Intent(context, EventListActivity.class);
        intent.putExtra(EventListActivity.EXTRA_USER_ID, UUID.randomUUID().toString());
        intent.putExtra(EventListActivity.EXTRA_ROLE, "CUSTOMER");

        try (ActivityScenario<EventListActivity> ignored = ActivityScenario.launch(intent)) {
            AndroidTestSupport.waitForAssertion(() ->
                    onView(withId(R.id.eventsRecyclerView)).check(matches(isDisplayed()))
            );

            onView(withId(R.id.filterLocationInput)).perform(click());
            onData(allOf(is(instanceOf(String.class)), is("Filter Unique Venue"))).perform(click());

            AndroidTestSupport.waitForAssertion(() ->
                    onView(withId(R.id.eventsRecyclerView)).perform(
                            RecyclerViewActions.scrollTo(hasDescendant(withText(locationEvent.title())))
                    )
            );
            onView(withText(locationEvent.title())).check(matches(isDisplayed()));
            onView(withText("UI Filter Other Venue Event")).check(doesNotExist());

            onView(withId(R.id.clearFiltersButton)).perform(click());
            AndroidTestSupport.waitForAssertion(() ->
                    onView(withId(R.id.eventsRecyclerView)).perform(
                            RecyclerViewActions.scrollTo(hasDescendant(withText("UI Filter Other Venue Event")))
                    )
            );
        }
    }

    private Event saveEvent(Context context, String title, String category, String venue) {
        Event event = new Event(
                UUID.randomUUID(),
                "EVT-FILTER-" + UUID.randomUUID().toString().substring(0, 8),
                title,
                category,
                "Event used by filter UI tests",
                venue,
                LocalDateTime.of(2026, 12, 28, 10, 0),
                LocalDateTime.of(2026, 12, 28, 12, 0),
                30,
                30,
                UUID.randomUUID(),
                EventStatus.PUBLISHED,
                20.0
        );
        TicketingDataProvider.eventRepository(context).save(event);
        return event;
    }
}
