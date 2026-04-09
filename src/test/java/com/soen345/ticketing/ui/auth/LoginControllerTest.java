package com.soen345.ticketing.ui.auth;

import com.soen345.ticketing.application.auth.AuthenticationException;
import com.soen345.ticketing.application.auth.LoginRequestValidator;
import com.soen345.ticketing.application.usecase.auth.LoginUseCase;
import com.soen345.ticketing.infrastructure.persistence.inmemory.InMemoryUserRepository;
import com.soen345.ticketing.support.FakePasswordHasher;
import com.soen345.ticketing.support.UserFixtures;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LoginControllerTest {
    private final InMemoryUserRepository userRepository = new InMemoryUserRepository();
    private final FakePasswordHasher passwordHasher = new FakePasswordHasher();
    private final LoginController controller = new LoginController(
            new LoginUseCase(userRepository, passwordHasher, new LoginRequestValidator())
    );

    @Test
    void mapsSuccessfulEmailLoginToResponse() {
        userRepository.save(UserFixtures.admin("admin@test.com", "HASH_secret123"));
        passwordHasher.stubMatch("secret123", "HASH_secret123", true);

        LoginResponse response = controller.login(new LoginRequest("admin@test.com", "secret123"));

        assertEquals("Admin User", response.name());
        assertEquals("admin@test.com", response.email());
        assertEquals("ADMIN", response.role());
    }

    @Test
    void mapsSuccessfulPhoneLoginToResponse() {
        userRepository.save(UserFixtures.customerWithPhone("5141234567", "HASH_secret123"));
        passwordHasher.stubMatch("secret123", "HASH_secret123", true);

        LoginResponse response = controller.login(new LoginRequest("5141234567", "secret123"));

        assertEquals("Customer User", response.name());
        assertEquals("5141234567", response.phone());
        assertEquals("CUSTOMER", response.role());
    }

    @Test
    void propagatesAuthenticationFailures() {
        AuthenticationException exception = assertThrows(
                AuthenticationException.class,
                () -> controller.login(new LoginRequest("missing@test.com", "secret123"))
        );

        assertEquals("Invalid credentials", exception.getMessage());
    }
}
