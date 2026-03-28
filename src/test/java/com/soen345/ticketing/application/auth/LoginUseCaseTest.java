package com.soen345.ticketing.application.auth;

import com.soen345.ticketing.application.usecase.auth.LoginUseCase;
import com.soen345.ticketing.domain.user.Role;
import com.soen345.ticketing.domain.user.User;
import com.soen345.ticketing.domain.user.UserStatus;
import com.soen345.ticketing.infrastructure.persistence.inmemory.InMemoryUserRepository;
import com.soen345.ticketing.support.FakePasswordHasher;
import com.soen345.ticketing.support.UserFixtures;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LoginUseCaseTest {
    private final InMemoryUserRepository userRepository = new InMemoryUserRepository();
    private final FakePasswordHasher passwordHasher = new FakePasswordHasher();
    private final LoginRequestValidator validator = new LoginRequestValidator();
    private final LoginUseCase loginUseCase =
            new LoginUseCase(userRepository, passwordHasher, validator);

    @Test
    void logsInCustomerWithEmail() {
        userRepository.save(UserFixtures.customer("customer@site.com", "HASH_secret123"));
        passwordHasher.stubMatch("secret123", "HASH_secret123", true);

        LoginResult result = loginUseCase.login(new LoginCommand("customer@site.com", "secret123"));

        assertEquals("customer@site.com", result.email());
        assertEquals(Role.CUSTOMER, result.role());
    }

    @Test
    void logsInCustomerWithPhone() {
        userRepository.save(UserFixtures.customerWithPhone("5141234567", "HASH_secret123"));
        passwordHasher.stubMatch("secret123", "HASH_secret123", true);

        LoginResult result = loginUseCase.login(new LoginCommand("5141234567", "secret123"));

        assertEquals("5141234567", result.phone());
        assertEquals(Role.CUSTOMER, result.role());
    }

    @Test
    void logsInAdminWithEmail() {
        userRepository.save(UserFixtures.admin("admin@site.com", "HASH_adminpass"));
        passwordHasher.stubMatch("adminpass", "HASH_adminpass", true);

        LoginResult result = loginUseCase.login(new LoginCommand("admin@site.com", "adminpass"));

        assertEquals(Role.ADMIN, result.role());
    }

    @Test
    void rejectsUnknownUser() {
        AuthenticationException exception = assertThrows(
                AuthenticationException.class,
                () -> loginUseCase.login(new LoginCommand("missing@site.com", "secret123"))
        );

        assertEquals("Invalid credentials", exception.getMessage());
    }

    @Test
    void rejectsWrongPassword() {
        userRepository.save(UserFixtures.customer("customer@site.com", "HASH_secret123"));
        passwordHasher.stubMatch("wrongpass", "HASH_secret123", false);

        AuthenticationException exception = assertThrows(
                AuthenticationException.class,
                () -> loginUseCase.login(new LoginCommand("customer@site.com", "wrongpass"))
        );

        assertEquals("Invalid credentials", exception.getMessage());
    }

    @Test
    void rejectsSuspendedUser() {
        User suspended = new User(
                UUID.randomUUID(), "Suspended", "suspended@site.com", null,
                "HASH_pass", Role.CUSTOMER, UserStatus.SUSPENDED
        );
        userRepository.save(suspended);
        passwordHasher.stubMatch("pass", "HASH_pass", true);

        AuthenticationException exception = assertThrows(
                AuthenticationException.class,
                () -> loginUseCase.login(new LoginCommand("suspended@site.com", "pass"))
        );

        assertEquals("User account is not active", exception.getMessage());
    }

    @Test
    void loginEmailIsCaseInsensitive() {
        userRepository.save(UserFixtures.customer("user@site.com", "HASH_pass"));
        passwordHasher.stubMatch("pass", "HASH_pass", true);

        LoginResult result = loginUseCase.login(new LoginCommand("USER@SITE.COM", "pass"));

        assertEquals("user@site.com", result.email());
    }

    @Test
    void rejectsNullCommand() {
        assertThrows(
                ValidationException.class,
                () -> loginUseCase.login(null)
        );
    }
}
