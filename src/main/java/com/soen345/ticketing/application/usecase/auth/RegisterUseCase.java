package com.soen345.ticketing.application.usecase.auth;

import com.soen345.ticketing.application.auth.LoginRequestValidator;
import com.soen345.ticketing.application.auth.PasswordHasher;
import com.soen345.ticketing.application.auth.RegisterCommand;
import com.soen345.ticketing.application.auth.ValidationException;
import com.soen345.ticketing.domain.Notifications.NotificationService;
import com.soen345.ticketing.domain.user.User;
import com.soen345.ticketing.domain.user.UserRepository;
import com.soen345.ticketing.domain.user.UserStatus;

import java.util.UUID;

public class RegisterUseCase {
    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;
    private final NotificationService notificationService;

    public RegisterUseCase(UserRepository userRepository, PasswordHasher passwordHasher, NotificationService notificationService) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
        this.notificationService = notificationService;
    }

    public RegisterUseCase(UserRepository userRepository, PasswordHasher passwordHasher) {
        this(userRepository, passwordHasher, null);
    }

    public User register(RegisterCommand command) {
        validateCommand(command);

        String hashedPassword = passwordHasher.hash(command.password());

        User user = new User(
                UUID.randomUUID(),
                command.name().trim(),
                command.email() != null ? command.email().trim().toLowerCase() : null,
                command.phone() != null ? command.phone().trim() : null,
                hashedPassword,
                command.role(),
                UserStatus.ACTIVE
        );

        User savedUser = userRepository.save(user);

        if (notificationService != null && savedUser.email() != null && !savedUser.email().isBlank()) {
            notificationService.sendConfirmation(
                    savedUser.email(),
                    "Welcome to Ticketing Application",
                    "Your account has been successfully created. Welcome to our ticketing application!"
            );
        }

        return savedUser;
    }

    private void validateCommand(RegisterCommand command) {
        if (command == null) {
            throw new ValidationException("Registration request must not be null");
        }

        if (command.name() == null || command.name().isBlank()) {
            throw new ValidationException("Name must not be blank");
        }

        boolean hasEmail = command.email() != null && !command.email().isBlank();
        boolean hasPhone = command.phone() != null && !command.phone().isBlank();

        if (!hasEmail && !hasPhone) {
            throw new ValidationException("Email or phone number is required");
        }

        if (hasEmail && !LoginRequestValidator.isEmail(command.email().trim())) {
            throw new ValidationException("Email format is invalid");
        }

        if (hasPhone && !LoginRequestValidator.isPhone(command.phone().trim())) {
            throw new ValidationException("Phone number format is invalid");
        }

        if (hasEmail && userRepository.findByEmail(command.email().trim().toLowerCase()).isPresent()) {
            throw new ValidationException("An account with this email already exists");
        }

        if (hasPhone && userRepository.findByPhone(command.phone().trim()).isPresent()) {
            throw new ValidationException("An account with this phone number already exists");
        }

        if (command.password() == null || command.password().length() < 6) {
            throw new ValidationException("Password must be at least 6 characters");
        }

        if (command.role() == null) {
            throw new ValidationException("Role must be selected");
        }
    }
}
