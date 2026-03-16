package com.soen345.ticketing.android;

import com.soen345.ticketing.application.auth.LoginRequestValidator;
import com.soen345.ticketing.application.auth.PasswordHasher;
import com.soen345.ticketing.application.usecase.auth.LoginUseCase;
import com.soen345.ticketing.domain.user.Role;
import com.soen345.ticketing.domain.user.User;
import com.soen345.ticketing.domain.user.UserStatus;
import com.soen345.ticketing.infrastructure.persistence.inmemory.InMemoryUserRepository;
import com.soen345.ticketing.infrastructure.security.PlainTextPasswordHasher;

import java.util.UUID;

public final class FakeAuthFactory {
    private static final LoginUseCase LOGIN_USE_CASE = createLoginUseCase();

    private FakeAuthFactory() {
    }

    public static LoginUseCase loginUseCase() {
        return LOGIN_USE_CASE;
    }

    private static LoginUseCase createLoginUseCase() {
        InMemoryUserRepository userRepository = new InMemoryUserRepository();
        PasswordHasher passwordHasher = new PlainTextPasswordHasher();

        seedUser(userRepository, "Customer", "customer@gmail.com", "secret123", Role.CUSTOMER);
        seedUser(userRepository, "Organizer", "admin@gmail.com", "adminpass", Role.ADMIN);

        return new LoginUseCase(userRepository, passwordHasher, new LoginRequestValidator());
    }

    private static void seedUser(
            InMemoryUserRepository userRepository,
            String name,
            String email,
            String password,
            Role role
    ) {
        userRepository.save(new User(
                UUID.randomUUID(),
                name,
                email,
                password,
                role,
                UserStatus.ACTIVE
        ));
    }
}
