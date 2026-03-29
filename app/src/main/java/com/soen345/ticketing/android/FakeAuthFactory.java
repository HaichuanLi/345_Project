package com.soen345.ticketing.android;

import com.soen345.ticketing.application.auth.LoginRequestValidator;
import com.soen345.ticketing.application.auth.PasswordHasher;
import com.soen345.ticketing.application.usecase.auth.LoginUseCase;
import com.soen345.ticketing.application.usecase.auth.RegisterUseCase;
import com.soen345.ticketing.domain.user.Role;
import com.soen345.ticketing.domain.user.User;
import com.soen345.ticketing.domain.user.UserRepository;
import com.soen345.ticketing.domain.user.UserStatus;
import com.soen345.ticketing.infrastructure.persistence.inmemory.InMemoryUserRepository;
import com.soen345.ticketing.infrastructure.security.PlainTextPasswordHasher;

import java.util.UUID;

public final class FakeAuthFactory {
    private static final UserRepository USER_REPOSITORY = new InMemoryUserRepository();
    private static final PasswordHasher PASSWORD_HASHER = new PlainTextPasswordHasher();
    private static final LoginUseCase LOGIN_USE_CASE = createLoginUseCase();
    private static final RegisterUseCase REGISTER_USE_CASE = createRegisterUseCase();

    static {
        seedUser("Customer", "customer@gmail.com", null, "secret123", Role.CUSTOMER);
        seedUser("Admin", "admin@gmail.com", null, "adminpass", Role.ADMIN);
        seedUser("Phone User", null, "5141234567", "phone123", Role.CUSTOMER);
    }

    private FakeAuthFactory() {
    }

    public static LoginUseCase loginUseCase() {
        return LOGIN_USE_CASE;
    }

    public static RegisterUseCase registerUseCase() {
        return REGISTER_USE_CASE;
    }

    public static UserRepository userRepository() {
        return USER_REPOSITORY;
    }

    private static LoginUseCase createLoginUseCase() {
        return new LoginUseCase(USER_REPOSITORY, PASSWORD_HASHER, new LoginRequestValidator());
    }

    private static RegisterUseCase createRegisterUseCase() {
        return new RegisterUseCase(USER_REPOSITORY, PASSWORD_HASHER);
    }

    private static void seedUser(
            String name,
            String email,
            String phone,
            String password,
            Role role
    ) {
        USER_REPOSITORY.save(new User(
                UUID.randomUUID(),
                name,
                email,
                phone,
                password,
                role,
                UserStatus.ACTIVE
        ));
    }
}
