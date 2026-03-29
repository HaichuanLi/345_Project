package com.soen345.ticketing.android;

import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.soen345.ticketing.domain.event.Event;
import com.soen345.ticketing.domain.event.EventRepository;
import com.soen345.ticketing.domain.event.EventStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
public class FirestoreEventRepository implements EventRepository{
    private final FirebaseFirestore db;
    private static final String COLLECTION = "events";

    public FirestoreEventRepository() {
        this.db = FirebaseFirestore.getInstance();
    }

    @Override
    public Event save(Event event) {
        try {
            Tasks.await(
                    db.collection(COLLECTION)
                            .document(event.id().toString())
                            .set(toMap(event))
            );
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException("Failed to save event", e);
        }
        return event;
    }

    @Override
    public Optional<Event> findById(UUID id) {
        try {
            DocumentSnapshot doc = Tasks.await(
                    db.collection(COLLECTION).document(id.toString()).get()
            );
            return doc.exists() ? Optional.of(fromDocument(doc)) : Optional.empty();
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException("Failed to find event by id", e);
        }
    }

    @Override
    public List<Event> listAvailable() {
        try {
            QuerySnapshot result = Tasks.await(
                    db.collection(COLLECTION)
                            .whereEqualTo("status", EventStatus.PUBLISHED.name())
                            .get()
            );
            List<Event> events = new ArrayList<>();
            for (DocumentSnapshot doc : result.getDocuments()) {
                Event e = fromDocument(doc);
                if (e.availableTickets() > 0) events.add(e);
            }
            return events;
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException("Failed to list available events", e);
        }
    }

    @Override
    public List<Event> listAll() {
        try {
            QuerySnapshot result = Tasks.await(
                    db.collection(COLLECTION).get()
            );
            List<Event> events = new ArrayList<>();
            for (DocumentSnapshot doc : result.getDocuments()) {
                Event e = fromDocument(doc);
                if (e.status() == EventStatus.PUBLISHED || e.status() == EventStatus.CANCELLED) {
                    events.add(e);
                }
            }
            return events;
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException("Failed to list all events", e);
        }
    }

    private Map<String, Object> toMap(Event e) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", e.id().toString());
        map.put("eventCode", e.eventCode());
        map.put("title", e.title());
        map.put("category", e.category());
        map.put("description", e.description());
        map.put("venue", e.venue());
        map.put("startDateTime", e.startDateTime().toString());
        map.put("endDateTime", e.endDateTime().toString());
        map.put("capacity", e.capacity());
        map.put("availableTickets", e.availableTickets());
        map.put("organizerId", e.organizerId().toString());
        map.put("status", e.status().name());
        map.put("price", e.price());
        return map;
    }

    private Event fromDocument(DocumentSnapshot doc) {
        return new Event(
                UUID.fromString(doc.getString("id")),
                doc.getString("eventCode"),
                doc.getString("title"),
                doc.getString("category"),
                doc.getString("description"),
                doc.getString("venue"),
                LocalDateTime.parse(doc.getString("startDateTime")),
                LocalDateTime.parse(doc.getString("endDateTime")),
                ((Long) doc.get("capacity")).intValue(),
                ((Long) doc.get("availableTickets")).intValue(),
                UUID.fromString(doc.getString("organizerId")),
                EventStatus.valueOf(doc.getString("status")),
                doc.getDouble("price")
        );
    }
}
