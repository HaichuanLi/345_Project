package com.soen345.ticketing.android;

import com.soen345.ticketing.application.auth.LoginRequestValidator;
import com.soen345.ticketing.application.auth.PasswordHasher;
import com.soen345.ticketing.application.usecase.auth.LoginUseCase;
import com.soen345.ticketing.application.usecase.auth.RegisterUseCase;
import com.soen345.ticketing.domain.Notifications.NotificationService;
import com.soen345.ticketing.domain.user.UserRepository;
import com.soen345.ticketing.infrastructure.Notifications.EmailNotificationAdapter;
import com.soen345.ticketing.infrastructure.security.PlainTextPasswordHasher;

public final class FakeAuthFactory {
    private static final UserRepository USER_REPOSITORY = new FirestoreUserRepository();
    private static final PasswordHasher PASSWORD_HASHER = new PlainTextPasswordHasher();
    private static final LoginUseCase LOGIN_USE_CASE = createLoginUseCase();
    private static final RegisterUseCase REGISTER_USE_CASE = createRegisterUseCase();
    private FakeAuthFactory() {
    }

    public static LoginUseCase loginUseCase() {
        return LOGIN_USE_CASE;
    }

    public static RegisterUseCase registerUseCase() {
        return REGISTER_USE_CASE;
    }

    private static LoginUseCase createLoginUseCase() {
        return new LoginUseCase(USER_REPOSITORY, PASSWORD_HASHER, new LoginRequestValidator());
    }

    private static RegisterUseCase createRegisterUseCase() {
        NotificationService notificationService = new EmailNotificationAdapter(
                BuildConfig.SENDER_EMAIL,
                BuildConfig.GOOGLE_APP_PASSWORD
        );
        return new RegisterUseCase(USER_REPOSITORY, PASSWORD_HASHER, notificationService);
    }
}
