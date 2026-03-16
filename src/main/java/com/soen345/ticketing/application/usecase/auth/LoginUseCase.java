package com.soen345.ticketing.application.usecase.auth;

import com.soen345.ticketing.application.auth.AuthenticationException;
import com.soen345.ticketing.application.auth.LoginCommand;
import com.soen345.ticketing.application.auth.LoginRequestValidator;
import com.soen345.ticketing.application.auth.LoginResult;
import com.soen345.ticketing.application.auth.PasswordHasher;
import com.soen345.ticketing.domain.user.User;
import com.soen345.ticketing.domain.user.UserRepository;
import com.soen345.ticketing.domain.user.UserStatus;

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

        String normalizedEmail = command.email().trim().toLowerCase();
        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new AuthenticationException("Invalid email or password"));

        if (user.status() != UserStatus.ACTIVE) {
            throw new AuthenticationException("User account is not active");
        }

        boolean passwordMatches = passwordHasher.matches(command.password(), user.passwordHash());
        if (!passwordMatches) {
            throw new AuthenticationException("Invalid email or password");
        }

        return new LoginResult(user.id(), user.name(), user.email(), user.role());
    }
}
