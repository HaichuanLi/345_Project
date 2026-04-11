package com.soen345.ticketing.android;

import android.content.Context;

import com.soen345.ticketing.application.reservation.ReservationConfirmationService;
import com.soen345.ticketing.application.reservation.ReservationNotificationService;
import com.soen345.ticketing.domain.event.EventRepository;
import com.soen345.ticketing.domain.reservation.ReservationRepository;
import com.soen345.ticketing.domain.user.UserRepository;
import com.soen345.ticketing.infrastructure.persistence.filebased.FileBasedReservationConfirmationService;

import java.io.File;
import java.util.UUID;

public final class TicketingDataProvider {
	private static final String CONFIRMATION_DIR = "reservation-confirmations";

	private static EventRepository eventRepository;
	private static ReservationRepository reservationRepository;
	private static ReservationConfirmationService confirmationService;
	private static UserRepository userRepository;
	private static ReservationNotificationService notificationService;

	private TicketingDataProvider() {}

	public static synchronized EventRepository eventRepository(Context context) {
		ensureInitialized(context);
		return eventRepository;
	}

	public static synchronized ReservationRepository reservationRepository(Context context) {
		ensureInitialized(context);
		return reservationRepository;
	}

	public static synchronized ReservationConfirmationService confirmationService(Context context) {
		ensureInitialized(context);
		return confirmationService;
	}

	public static synchronized UserRepository userRepository(Context context) {
		ensureInitialized(context);
		return userRepository;
	}

	public static synchronized ReservationNotificationService notificationService(Context context) {
		ensureInitialized(context);
		return notificationService;
	}

	private static void ensureInitialized(Context context) {
		if (eventRepository != null
				&& reservationRepository != null
				&& confirmationService != null
				&& userRepository != null
				&& notificationService != null) {
			return;
		}

		eventRepository = new FirestoreEventRepository();
		reservationRepository = new FirestoreReservationRepository();
		userRepository = new FirestoreUserRepository();
		notificationService = new SmtpReservationNotificationService();

		File storageDir = new File(context.getFilesDir(), CONFIRMATION_DIR);
		confirmationService = new FileBasedReservationConfirmationService(storageDir.getAbsolutePath());
	}

	public static synchronized void seedEventsIfEmpty(Context context) {
		new Thread(() -> {
			EventRepository repo = eventRepository(context);
			if (!repo.listAll().isEmpty()) {
				return;
			}

			UUID organizerId = UUID.randomUUID();
			repo.save(new com.soen345.ticketing.domain.event.Event(
					UUID.randomUUID(), "EVT-2026001", "Java Conference 2026", "Technology",
					"Annual Java conference bringing together developers from around the world.",
					"Montreal Convention Centre",
					java.time.LocalDateTime.of(2026, 4, 15, 9, 0),
					java.time.LocalDateTime.of(2026, 4, 15, 17, 0),
					500, 500, organizerId, com.soen345.ticketing.domain.event.EventStatus.PUBLISHED, 199.99));

			repo.save(new com.soen345.ticketing.domain.event.Event(
					UUID.randomUUID(), "EVT-2026002", "Spring Boot Workshop", "Technology",
					"Comprehensive hands-on workshop on Spring Boot best practices.",
					"Downtown Tech Hub",
					java.time.LocalDateTime.of(2026, 5, 10, 10, 0),
					java.time.LocalDateTime.of(2026, 5, 10, 16, 0),
					100, 100, organizerId, com.soen345.ticketing.domain.event.EventStatus.PUBLISHED, 149.99));

			repo.save(new com.soen345.ticketing.domain.event.Event(
					UUID.randomUUID(), "EVT-2026003", "AI & Machine Learning Expo", "Technology",
					"Explore the latest advancements in artificial intelligence and machine learning.",
					"Innovation District Hall",
					java.time.LocalDateTime.of(2026, 5, 20, 14, 0),
					java.time.LocalDateTime.of(2026, 5, 20, 18, 0),
					300, 300, organizerId, com.soen345.ticketing.domain.event.EventStatus.PUBLISHED, 179.99));

			repo.save(new com.soen345.ticketing.domain.event.Event(
					UUID.randomUUID(), "EVT-2026004", "Cloud Computing Masterclass", "Technology",
					"Master the fundamentals and advanced concepts of cloud platforms.",
					"Tech Training Center",
					java.time.LocalDateTime.of(2026, 6, 1, 9, 0),
					java.time.LocalDateTime.of(2026, 6, 1, 12, 0),
					80, 80, organizerId, com.soen345.ticketing.domain.event.EventStatus.PUBLISHED, 129.99));

			repo.save(new com.soen345.ticketing.domain.event.Event(
					UUID.randomUUID(), "EVT-2026005", "DevOps Summit", "Technology",
					"Discover modern DevOps practices and tools for continuous integration.",
					"Enterprise Center",
					java.time.LocalDateTime.of(2026, 6, 10, 13, 0),
					java.time.LocalDateTime.of(2026, 6, 10, 17, 30),
					200, 200, organizerId, com.soen345.ticketing.domain.event.EventStatus.PUBLISHED, 159.99));

			repo.save(new com.soen345.ticketing.domain.event.Event(
					UUID.randomUUID(), "EVT-2026006", "Cybersecurity Seminar", "Security",
					"Stay informed about the latest security threats and best practices.",
					"Security Institute",
					java.time.LocalDateTime.of(2026, 5, 25, 10, 0),
					java.time.LocalDateTime.of(2026, 5, 25, 15, 0),
					150, 150, organizerId, com.soen345.ticketing.domain.event.EventStatus.PUBLISHED, 139.99));

			repo.save(new com.soen345.ticketing.domain.event.Event(
					UUID.randomUUID(), "EVT-2026007", "Web Development Summit", "Technology",
					"Learn about the latest web technologies and frameworks.",
					"Creative Studios",
					java.time.LocalDateTime.of(2026, 7, 5, 9, 30),
					java.time.LocalDateTime.of(2026, 7, 5, 17, 0),
					250, 250, organizerId, com.soen345.ticketing.domain.event.EventStatus.PUBLISHED, 189.99));

			repo.save(new com.soen345.ticketing.domain.event.Event(
					UUID.randomUUID(), "EVT-2026008", "Mobile App Development", "Technology",
					"Build amazing mobile applications for iOS and Android.",
					"Digital Innovation Lab",
					java.time.LocalDateTime.of(2026, 6, 15, 11, 0),
					java.time.LocalDateTime.of(2026, 6, 15, 16, 0),
					120, 120, organizerId, com.soen345.ticketing.domain.event.EventStatus.PUBLISHED, 119.99));

		}).start();
	}
}
