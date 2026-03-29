package com.soen345.ticketing.android;

import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.soen345.ticketing.domain.reservation.Reservation;
import com.soen345.ticketing.domain.reservation.ReservationRepository;
import com.soen345.ticketing.domain.reservation.ReservationStatus;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
public class FirestoreReservationRepository implements ReservationRepository{
    private final FirebaseFirestore db;
    private static final String COLLECTION = "reservations";

    public FirestoreReservationRepository() {
        this.db = FirebaseFirestore.getInstance();
    }

    @Override
    public Reservation save(Reservation reservation) {
        try {
            Tasks.await(
                    db.collection(COLLECTION)
                            .document(reservation.id().toString())
                            .set(toMap(reservation))
            );
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException("Failed to save reservation", e);
        }
        return reservation;
    }

    @Override
    public Optional<Reservation> findById(UUID id) {
        try {
            DocumentSnapshot doc = Tasks.await(
                    db.collection(COLLECTION).document(id.toString()).get()
            );
            return doc.exists() ? Optional.of(fromDocument(doc)) : Optional.empty();
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException("Failed to find reservation", e);
        }
    }

    @Override
    public List<Reservation> findByCustomerId(UUID customerId) {
        try {
            QuerySnapshot result = Tasks.await(
                    db.collection(COLLECTION)
                            .whereEqualTo("customerId", customerId.toString())
                            .get()
            );
            List<Reservation> list = new ArrayList<>();
            for (DocumentSnapshot doc : result.getDocuments()) {
                list.add(fromDocument(doc));
            }
            return list;
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException("Failed to find reservations by customer", e);
        }
    }

    @Override
    public List<Reservation> findByEventId(UUID eventId) {
        try {
            QuerySnapshot result = Tasks.await(
                    db.collection(COLLECTION)
                            .whereEqualTo("eventId", eventId.toString())
                            .get()
            );
            List<Reservation> list = new ArrayList<>();
            for (DocumentSnapshot doc : result.getDocuments()) {
                list.add(fromDocument(doc));
            }
            return list;
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException("Failed to find reservations by event", e);
        }
    }

    private Map<String, Object> toMap(Reservation r) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", r.id().toString());
        map.put("eventId", r.eventId().toString());
        map.put("customerId", r.customerId().toString());
        map.put("quantity", r.quantity());
        map.put("reservedAt", r.reservedAt().toString());
        map.put("status", r.status().name());
        return map;
    }

    private Reservation fromDocument(DocumentSnapshot doc) {
        return new Reservation(
                UUID.fromString(doc.getString("id")),
                UUID.fromString(doc.getString("eventId")),
                UUID.fromString(doc.getString("customerId")),
                ((Long) doc.get("quantity")).intValue(),
                Instant.parse(doc.getString("reservedAt")),
                ReservationStatus.valueOf(doc.getString("status"))
        );
    }
}
