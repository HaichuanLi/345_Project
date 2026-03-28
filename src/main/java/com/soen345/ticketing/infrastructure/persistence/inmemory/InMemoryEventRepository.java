package com.soen345.ticketing.infrastructure.persistence.inmemory;

import com.soen345.ticketing.domain.event.Event;
import com.soen345.ticketing.domain.event.EventRepository;
import com.soen345.ticketing.domain.event.EventStatus;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class InMemoryEventRepository implements EventRepository {
    private final Map<UUID, Event> eventsById = new HashMap<>();

    public InMemoryEventRepository() {
        seedSampleEvents();
    }

    @Override
    public Optional<Event> findById(UUID id) {
        return Optional.ofNullable(eventsById.get(id));
    }

    @Override
    public Event save(Event event) {
        eventsById.put(event.id(), event);
        return event;
    }

    @Override
    public List<Event> listAvailable() {
        return eventsById.values().stream()
                .filter(event -> event.status() == EventStatus.PUBLISHED)
                .filter(event -> event.availableTickets() > 0)
                .collect(Collectors.toList());
    }

    private void seedSampleEvents() {
        UUID organizerId = UUID.randomUUID();

        save(new Event(
                UUID.randomUUID(),
                "EVT-2026001",
                "Java Conference 2026",
                "Technology",
                "Annual Java conference bringing together developers",
                "Montreal Convention Centre",
                LocalDateTime.of(2026, 4, 15, 9, 0),
                LocalDateTime.of(2026, 4, 15, 17, 0),
                500,
                120,
                organizerId,
                EventStatus.PUBLISHED
        ));

        save(new Event(
                UUID.randomUUID(),
                "EVT-2026002",
                "Spring Boot Workshop",
                "Technology",
                "Learn Spring Boot best practices",
                "Downtown Tech Hub",
                LocalDateTime.of(2026, 5, 10, 10, 0),
                LocalDateTime.of(2026, 5, 10, 16, 0),
                100,
                25,
                organizerId,
                EventStatus.PUBLISHED
        ));

        save(new Event(
                UUID.randomUUID(),
                "EVT-2026003",
                "AI & Machine Learning Expo",
                "Technology",
                "Explore latest AI advancements",
                "Innovation District Hall",
                LocalDateTime.of(2026, 5, 20, 14, 0),
                LocalDateTime.of(2026, 5, 20, 18, 0),
                300,
                85,
                organizerId,
                EventStatus.PUBLISHED
        ));

        save(new Event(
                UUID.randomUUID(),
                "EVT-2026004",
                "Cloud Computing Masterclass",
                "Technology",
                "Master cloud platforms",
                "Tech Training Center",
                LocalDateTime.of(2026, 6, 1, 9, 0),
                LocalDateTime.of(2026, 6, 1, 12, 0),
                80,
                15,
                organizerId,
                EventStatus.PUBLISHED
        ));

        save(new Event(
                UUID.randomUUID(),
                "EVT-2026005",
                "DevOps Summit",
                "Technology",
                "Modern DevOps practices",
                "Enterprise Center",
                LocalDateTime.of(2026, 6, 10, 13, 0),
                LocalDateTime.of(2026, 6, 10, 17, 30),
                200,
                45,
                organizerId,
                EventStatus.PUBLISHED
        ));

        save(new Event(
                UUID.randomUUID(),
                "EVT-2026006",
                "Cybersecurity Seminar",
                "Security",
                "Latest security threats",
                "Security Institute",
                LocalDateTime.of(2026, 5, 25, 10, 0),
                LocalDateTime.of(2026, 5, 25, 15, 0),
                150,
                60,
                organizerId,
                EventStatus.PUBLISHED
        ));

        save(new Event(
                UUID.randomUUID(),
                "EVT-2026007",
                "Web Development Summit",
                "Technology",
                "Latest web technologies",
                "Creative Studios",
                LocalDateTime.of(2026, 7, 5, 9, 30),
                LocalDateTime.of(2026, 7, 5, 17, 0),
                250,
                90,
                organizerId,
                EventStatus.PUBLISHED
        ));

        save(new Event(
                UUID.randomUUID(),
                "EVT-2026008",
                "Mobile App Development",
                "Technology",
                "Build mobile applications",
                "Digital Innovation Lab",
                LocalDateTime.of(2026, 6, 15, 11, 0),
                LocalDateTime.of(2026, 6, 15, 16, 0),
                120,
                20,
                organizerId,
                EventStatus.PUBLISHED
        ));
    }
}

