package com.soen345.ticketing.android;

import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.soen345.ticketing.domain.user.Role;
import com.soen345.ticketing.domain.user.User;
import com.soen345.ticketing.domain.user.UserRepository;
import com.soen345.ticketing.domain.user.UserStatus;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
public class FirestoreUserRepository implements UserRepository{
    private final FirebaseFirestore db;
    private static final String COLLECTION = "users";

    public FirestoreUserRepository() {
        this.db = FirebaseFirestore.getInstance();
    }

    @Override
    public User save(User user) {
        try {
            Tasks.await(
                    db.collection(COLLECTION)
                            .document(user.id().toString())
                            .set(toMap(user))
            );
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException("Failed to save user", e);
        }
        return user;
    }

    @Override
    public Optional<User> findById(UUID id) {
        try {
            DocumentSnapshot doc = Tasks.await(
                    db.collection(COLLECTION).document(id.toString()).get()
            );
            return doc.exists() ? Optional.of(fromDocument(doc)) : Optional.empty();
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException("Failed to find user by id", e);
        }
    }

    @Override
    public Optional<User> findByEmail(String email) {
        try {
            QuerySnapshot result = Tasks.await(
                    db.collection(COLLECTION)
                            .whereEqualTo("email", email.trim().toLowerCase())
                            .limit(1)
                            .get()
            );
            if (result.isEmpty()) return Optional.empty();
            return Optional.of(fromDocument(result.getDocuments().get(0)));
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException("Failed to find user by email", e);
        }
    }

    @Override
    public Optional<User> findByPhone(String phone) {
        try {
            QuerySnapshot result = Tasks.await(
                    db.collection(COLLECTION)
                            .whereEqualTo("phone", phone.trim())
                            .limit(1)
                            .get()
            );
            if (result.isEmpty()) return Optional.empty();
            return Optional.of(fromDocument(result.getDocuments().get(0)));
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException("Failed to find user by phone", e);
        }
    }

    private java.util.Map<String, Object> toMap(User user) {
        java.util.Map<String, Object> map = new java.util.HashMap<>();
        map.put("id", user.id().toString());
        map.put("name", user.name());
        map.put("email", user.email() != null ? user.email().trim().toLowerCase() : null);
        map.put("phone", user.phone() != null ? user.phone().trim() : null);
        map.put("passwordHash", user.passwordHash());
        map.put("role", user.role().name());
        map.put("status", user.status().name());
        return map;
    }

    private User fromDocument(DocumentSnapshot doc) {
        return new User(
                UUID.fromString(doc.getString("id")),
                doc.getString("name"),
                doc.getString("email"),
                doc.getString("phone"),
                doc.getString("passwordHash"),
                Role.valueOf(doc.getString("role")),
                UserStatus.valueOf(doc.getString("status"))
        );
    }
}
