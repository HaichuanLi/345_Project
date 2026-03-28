package com.soen345.ticketing.application.auth;

import com.soen345.ticketing.application.usecase.auth.RegisterUseCase;
import com.soen345.ticketing.domain.user.Role;
import com.soen345.ticketing.domain.user.User;
import com.soen345.ticketing.domain.user.UserStatus;
import com.soen345.ticketing.infrastructure.persistence.inmemory.InMemoryUserRepository;
import com.soen345.ticketing.support.FakePasswordHasher;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RegisterUseCaseTest {
    private final InMemoryUserRepository userRepository = new InMemoryUserRepository();
    private final FakePasswordHasher passwordHasher = new FakePasswordHasher();
    private final RegisterUseCase registerUseCase = new RegisterUseCase(userRepository, passwordHasher);

    @Test
    void registersCustomerWithEmail() {
        RegisterCommand command = new RegisterCommand("Alice", "alice@test.com", null, "password123", Role.CUSTOMER);

        User user = registerUseCase.register(command);

        assertEquals("Alice", user.name());
        assertEquals("alice@test.com", user.email());
        assertNull(user.phone());
        assertEquals(Role.CUSTOMER, user.role());
        assertEquals(UserStatus.ACTIVE, user.status());
        assertTrue(userRepository.findByEmail("alice@test.com").isPresent());
    }

    @Test
    void registersOrganizerWithPhone() {
        RegisterCommand command = new RegisterCommand("Bob", null, "5149876543", "password123", Role.ADMIN);

        User user = registerUseCase.register(command);

        assertEquals("Bob", user.name());
        assertNull(user.email());
        assertEquals("5149876543", user.phone());
        assertEquals(Role.ADMIN, user.role());
        assertTrue(userRepository.findByPhone("5149876543").isPresent());
    }

    @Test
    void rejectsBlankName() {
        RegisterCommand command = new RegisterCommand("", "a@b.com", null, "password123", Role.CUSTOMER);

        ValidationException ex = assertThrows(ValidationException.class, () -> registerUseCase.register(command));
        assertEquals("Name must not be blank", ex.getMessage());
    }

    @Test
    void rejectsNoContact() {
        RegisterCommand command = new RegisterCommand("Alice", null, null, "password123", Role.CUSTOMER);

        ValidationException ex = assertThrows(ValidationException.class, () -> registerUseCase.register(command));
        assertEquals("Email or phone number is required", ex.getMessage());
    }

    @Test
    void rejectsShortPassword() {
        RegisterCommand command = new RegisterCommand("Alice", "a@b.com", null, "12345", Role.CUSTOMER);

        ValidationException ex = assertThrows(ValidationException.class, () -> registerUseCase.register(command));
        assertEquals("Password must be at least 6 characters", ex.getMessage());
    }

    @Test
    void rejectsDuplicateEmail() {
        registerUseCase.register(new RegisterCommand("Alice", "alice@test.com", null, "password123", Role.CUSTOMER));

        RegisterCommand duplicate = new RegisterCommand("Bob", "alice@test.com", null, "password123", Role.CUSTOMER);
        ValidationException ex = assertThrows(ValidationException.class, () -> registerUseCase.register(duplicate));
        assertEquals("An account with this email already exists", ex.getMessage());
    }

    @Test
    void rejectsDuplicatePhone() {
        registerUseCase.register(new RegisterCommand("Alice", null, "5141234567", "password123", Role.CUSTOMER));

        RegisterCommand duplicate = new RegisterCommand("Bob", null, "5141234567", "password123", Role.CUSTOMER);
        ValidationException ex = assertThrows(ValidationException.class, () -> registerUseCase.register(duplicate));
        assertEquals("An account with this phone number already exists", ex.getMessage());
    }

    @Test
    void rejectsInvalidEmail() {
        RegisterCommand command = new RegisterCommand("Alice", "not-an-email", null, "password123", Role.CUSTOMER);

        ValidationException ex = assertThrows(ValidationException.class, () -> registerUseCase.register(command));
        assertEquals("Email format is invalid", ex.getMessage());
    }

    @Test
    void rejectsNullRole() {
        RegisterCommand command = new RegisterCommand("Alice", "a@b.com", null, "password123", null);

        ValidationException ex = assertThrows(ValidationException.class, () -> registerUseCase.register(command));
        assertEquals("Role must be selected", ex.getMessage());
    }
}
