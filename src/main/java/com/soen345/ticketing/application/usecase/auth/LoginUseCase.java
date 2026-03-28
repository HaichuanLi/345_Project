package com.soen345.ticketing.application.usecase.auth;

import com.soen345.ticketing.application.auth.AuthenticationException;
import com.soen345.ticketing.application.auth.LoginCommand;
import com.soen345.ticketing.application.auth.LoginRequestValidator;
import com.soen345.ticketing.application.auth.LoginResult;
import com.soen345.ticketing.application.auth.PasswordHasher;
import com.soen345.ticketing.domain.user.User;
import com.soen345.ticketing.domain.user.UserRepository;
import com.soen345.ticketing.domain.user.UserStatus;

import java.util.Optional;

public class LoginUseCase {
    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;
    private final LoginRequestValidator validator;

    public LoginUseCase(
            UserRepository userRepository,
            PasswordHasher passwordHasher,
            LoginRequestValidator validator
    ) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
        this.validator = validator;
    }

    public LoginResult login(LoginCommand command) {
        validator.validate(command);

        String identifier = command.identifier().trim();
        User user = findUser(identifier)
                .orElseThrow(() -> new AuthenticationException("Invalid credentials"));

        if (user.status() != UserStatus.ACTIVE) {
            throw new AuthenticationException("User account is not active");
        }

        if (!passwordHasher.matches(command.password(), user.passwordHash())) {
            throw new AuthenticationException("Invalid credentials");
        }

        return new LoginResult(user.id(), user.name(), user.email(), user.phone(), user.role());
    }

    private Optional<User> findUser(String identifier) {
        if (LoginRequestValidator.isEmail(identifier)) {
            return userRepository.findByEmail(identifier.toLowerCase());
        } else {
            return userRepository.findByPhone(identifier);
        }
    }
}
