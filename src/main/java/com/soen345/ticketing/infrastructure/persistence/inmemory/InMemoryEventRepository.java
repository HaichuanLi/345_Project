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
                "Annual Java conference bringing together developers from around the world. Features keynote speeches, technical workshops, and networking opportunities.",
                "Montreal Convention Centre",
                LocalDateTime.of(2026, 4, 15, 9, 0),
                LocalDateTime.of(2026, 4, 15, 17, 0),
                500,
                500,
                organizerId,
                EventStatus.PUBLISHED,
                199.99
        ));

        save(new Event(
                UUID.randomUUID(),
                "EVT-2026002",
                "Spring Boot Workshop",
                "Technology",
                "Comprehensive hands-on workshop on Spring Boot best practices and modern development patterns.",
                "Downtown Tech Hub",
                LocalDateTime.of(2026, 5, 10, 10, 0),
                LocalDateTime.of(2026, 5, 10, 16, 0),
                100,
                100,
                organizerId,
                EventStatus.PUBLISHED,
                149.99
        ));

        save(new Event(
                UUID.randomUUID(),
                "EVT-2026003",
                "AI & Machine Learning Expo",
                "Technology",
                "Explore the latest advancements in artificial intelligence and machine learning. Network with industry experts and see cutting-edge demos.",
                "Innovation District Hall",
                LocalDateTime.of(2026, 5, 20, 14, 0),
                LocalDateTime.of(2026, 5, 20, 18, 0),
                300,
                300,
                organizerId,
                EventStatus.PUBLISHED,
                179.99
        ));

        save(new Event(
                UUID.randomUUID(),
                "EVT-2026004",
                "Cloud Computing Masterclass",
                "Technology",
                "Master the fundamentals and advanced concepts of cloud platforms. Learn from industry leaders.",
                "Tech Training Center",
                LocalDateTime.of(2026, 6, 1, 9, 0),
                LocalDateTime.of(2026, 6, 1, 12, 0),
                80,
                80,
                organizerId,
                EventStatus.PUBLISHED,
                129.99
        ));

        save(new Event(
                UUID.randomUUID(),
                "EVT-2026005",
                "DevOps Summit",
                "Technology",
                "Discover modern DevOps practices and tools for continuous integration and deployment.",
                "Enterprise Center",
                LocalDateTime.of(2026, 6, 10, 13, 0),
                LocalDateTime.of(2026, 6, 10, 17, 30),
                200,
                200,
                organizerId,
                EventStatus.PUBLISHED,
                159.99
        ));

        save(new Event(
                UUID.randomUUID(),
                "EVT-2026006",
                "Cybersecurity Seminar",
                "Security",
                "Stay informed about the latest security threats and best practices to protect your systems.",
                "Security Institute",
                LocalDateTime.of(2026, 5, 25, 10, 0),
                LocalDateTime.of(2026, 5, 25, 15, 0),
                150,
                150,
                organizerId,
                EventStatus.PUBLISHED,
                139.99
        ));

        save(new Event(
                UUID.randomUUID(),
                "EVT-2026007",
                "Web Development Summit",
                "Technology",
                "Learn about the latest web technologies and frameworks. Connect with fellow developers and share experiences.",
                "Creative Studios",
                LocalDateTime.of(2026, 7, 5, 9, 30),
                LocalDateTime.of(2026, 7, 5, 17, 0),
                250,
                250,
                organizerId,
                EventStatus.PUBLISHED,
                189.99
        ));

        save(new Event(
                UUID.randomUUID(),
                "EVT-2026008",
                "Mobile App Development",
                "Technology",
                "Build amazing mobile applications for iOS and Android. Learn best practices and modern tools.",
                "Digital Innovation Lab",
                LocalDateTime.of(2026, 6, 15, 11, 0),
                LocalDateTime.of(2026, 6, 15, 16, 0),
                120,
                120,
                organizerId,
                EventStatus.PUBLISHED,
                119.99
        ));
    }
}

