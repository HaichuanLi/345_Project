package com.soen345.ticketing.application.event;

import com.soen345.ticketing.domain.event.Event;
import com.soen345.ticketing.domain.event.EventStatus;

import java.util.Objects;
import java.util.UUID;

public class AdminEventAccessPolicy {
    public boolean canView(Event event) {
        return event != null;
    }

    public boolean canEditOrCancel(UUID adminUserId, Event event) {
        if (adminUserId == null || event == null) {
            return false;
        }

        return event.status() != EventStatus.CANCELLED
                && Objects.equals(event.organizerId(), adminUserId);
    }
}