package com.soen345.ticketing.application.performance;

import com.soen345.ticketing.application.reservation.ReserveTicketsCommand;
import com.soen345.ticketing.application.usecase.reservation.ReserveTicketsUseCase;
import com.soen345.ticketing.application.reservation.ReserveTicketsValidator;
import com.soen345.ticketing.domain.event.Event;
import com.soen345.ticketing.domain.event.EventStatus;
import com.soen345.ticketing.infrastructure.persistence.inmemory.InMemoryEventRepository;
import com.soen345.ticketing.infrastructure.persistence.inmemory.InMemoryReservationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class ConcurrentReservationTest {
    private InMemoryEventRepository eventRepository;
    private InMemoryReservationRepository reservationRepository;
    private UUID eventId;
    private UUID organizerId;

    private static final int TOTAL_TICKETS = 10;
    private static final int CONCURRENT_USERS = 20;

    @BeforeEach
    void setUp() {
        eventRepository = new InMemoryEventRepository();
        reservationRepository = new InMemoryReservationRepository();
        organizerId = UUID.randomUUID();
        eventId = UUID.randomUUID();

        Event event = new Event(
                eventId, "EVT-TEST", "Concurrent Test Event", "Technology",
                "Test event for concurrency", "Test Venue",
                LocalDateTime.of(2026, 6, 1, 10, 0),
                LocalDateTime.of(2026, 6, 1, 18, 0),
                TOTAL_TICKETS, TOTAL_TICKETS,
                organizerId, EventStatus.PUBLISHED, 50.0
        );
        eventRepository.save(event);
    }

    @Test
    void concurrentReservationsDoNotOversellTickets() throws InterruptedException {
        ReserveTicketsUseCase sharedUseCase = new ReserveTicketsUseCase(
                eventRepository,
                reservationRepository,
                new FakeConfirmationService(),
                new ReserveTicketsValidator()
        );

        ExecutorService executor = Executors.newFixedThreadPool(CONCURRENT_USERS);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(CONCURRENT_USERS);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        for (int i = 0; i < CONCURRENT_USERS; i++) {
            UUID customerId = UUID.randomUUID();
            executor.submit(() -> {
                try {
                    startLatch.await(); // all threads start at the same time
                    sharedUseCase.reserve(new ReserveTicketsCommand(customerId, eventId, 1));
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failCount.incrementAndGet();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown(); // release all threads simultaneously
        doneLatch.await(10, TimeUnit.SECONDS);
        executor.shutdown();

        // No more than TOTAL_TICKETS reservations should succeed
        assertTrue(successCount.get() <= TOTAL_TICKETS,
                "Oversold! " + successCount.get() + " reservations succeeded but only "
                        + TOTAL_TICKETS + " tickets existed.");

        // Remaining tickets should never be negative
        Event updatedEvent = eventRepository.findById(eventId).orElseThrow();
        assertTrue(updatedEvent.availableTickets() >= 0,
                "Available tickets went negative: " + updatedEvent.availableTickets());

        System.out.println("Successes: " + successCount.get()
                + " | Failures: " + failCount.get()
                + " | Remaining tickets: " + updatedEvent.availableTickets());
    }

    @Test
    void concurrentReservationsCompleteWithinAcceptableTime() throws InterruptedException {
        int userCount = 50;

        // Single shared use case instance for all 50 threads
        ReserveTicketsUseCase sharedUseCase = new ReserveTicketsUseCase(
                eventRepository,
                reservationRepository,
                new FakeConfirmationService(),
                new ReserveTicketsValidator()
        );

        ExecutorService executor = Executors.newFixedThreadPool(userCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(userCount);
        List<Long> responseTimes = new CopyOnWriteArrayList<>();

        // Give enough tickets so all can succeed
        Event bigEvent = new Event(
                UUID.randomUUID(), "EVT-BIG", "Big Event", "Technology",
                "Performance test event", "Large Venue",
                LocalDateTime.of(2026, 7, 1, 10, 0),
                LocalDateTime.of(2026, 7, 1, 18, 0),
                userCount, userCount,
                organizerId, EventStatus.PUBLISHED, 50.0
        );
        UUID bigEventId = bigEvent.id();
        eventRepository.save(bigEvent);

        for (int i = 0; i < userCount; i++) {
            UUID customerId = UUID.randomUUID();
            executor.submit(() -> {
                try {
                    startLatch.await();
                    long start = System.currentTimeMillis();
                    sharedUseCase.reserve(new ReserveTicketsCommand(customerId, bigEventId, 1));
                    long elapsed = System.currentTimeMillis() - start;
                    responseTimes.add(elapsed);
                } catch (Exception e) {
                    responseTimes.add(-1L);
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        doneLatch.await(15, TimeUnit.SECONDS);
        executor.shutdown();

        long max = responseTimes.stream().filter(t -> t >= 0).mapToLong(Long::longValue).max().orElse(0);
        double avg = responseTimes.stream().filter(t -> t >= 0).mapToLong(Long::longValue).average().orElse(0);

        System.out.println("Max response time: " + max + "ms | Avg: " + avg + "ms");

        // All operations should complete within 2000ms
        assertTrue(max < 2000,
                "Max response time exceeded 2000ms: " + max + "ms");
    }
}
