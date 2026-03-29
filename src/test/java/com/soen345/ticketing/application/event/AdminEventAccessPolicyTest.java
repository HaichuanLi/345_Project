package com.soen345.ticketing.application.event;

import com.soen345.ticketing.domain.event.Event;
import com.soen345.ticketing.domain.event.EventStatus;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AdminEventAccessPolicyTest {
    private final AdminEventAccessPolicy policy = new AdminEventAccessPolicy();

    @Test
    void adminCanViewPublishedEventsCreatedByOtherUsers() {
        Event event = eventForOrganizer(UUID.randomUUID(), EventStatus.PUBLISHED);

        assertTrue(policy.canView(event));
    }

    @Test
    void adminCanViewCancelledEventsCreatedByOtherUsers() {
        Event event = eventForOrganizer(UUID.randomUUID(), EventStatus.CANCELLED);

        assertTrue(policy.canView(event));
    }

    @Test
    void adminCanEditOrCancelEventsTheyCreated() {
        UUID adminId = UUID.randomUUID();
        Event ownEvent = eventForOrganizer(adminId, EventStatus.PUBLISHED);

        assertTrue(policy.canEditOrCancel(adminId, ownEvent));
    }

    @Test
    void adminCannotEditOrCancelEventsCreatedByAnotherUser() {
        UUID adminId = UUID.randomUUID();
        Event othersEvent = eventForOrganizer(UUID.randomUUID(), EventStatus.PUBLISHED);

        assertFalse(policy.canEditOrCancel(adminId, othersEvent));
    }

    @Test
    void adminCannotEditOrCancelCancelledEventsEvenIfTheyCreatedThem() {
        UUID adminId = UUID.randomUUID();
        Event cancelledOwnEvent = eventForOrganizer(adminId, EventStatus.CANCELLED);

        assertFalse(policy.canEditOrCancel(adminId, cancelledOwnEvent));
    }

    private Event eventForOrganizer(UUID organizerId, EventStatus status) {
        return new Event(
                UUID.randomUUID(),
                "EVT-ADMIN-001",
                "Admin Access Event",
                "Technology",
                "Event used for admin access policy testing",
                "Montreal",
                LocalDateTime.of(2026, 6, 1, 10, 0),
                LocalDateTime.of(2026, 6, 1, 12, 0),
                100,
                100,
                organizerId,
                status,
                25.0
        );
    }
}