package com.soen345.ticketing.infrastructure.persistence.inmemory;

import com.soen345.ticketing.domain.user.User;
import com.soen345.ticketing.domain.user.UserRepository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class InMemoryUserRepository implements UserRepository {
    private final Map<UUID, User> usersById = new HashMap<>();
    private final Map<String, User> usersByEmail = new HashMap<>();

    @Override
    public Optional<User> findById(UUID id) {
        return Optional.ofNullable(usersById.get(id));
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return Optional.ofNullable(usersByEmail.get(normalizeEmail(email)));
    }

    @Override
    public User save(User user) {
        usersById.put(user.id(), user);
        usersByEmail.put(normalizeEmail(user.email()), user);
        return user;
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase();
    }
}
