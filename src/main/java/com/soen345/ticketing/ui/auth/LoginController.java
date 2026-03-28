package com.soen345.ticketing.ui.auth;

import com.soen345.ticketing.application.auth.LoginCommand;
import com.soen345.ticketing.application.auth.LoginResult;
import com.soen345.ticketing.application.usecase.auth.LoginUseCase;

public class LoginController {
    private final LoginUseCase loginUseCase;

    public LoginController(LoginUseCase loginUseCase) {
        this.loginUseCase = loginUseCase;
    }

    public LoginResponse login(LoginRequest request) {
        LoginResult result = loginUseCase.login(new LoginCommand(request.identifier(), request.password()));
        return new LoginResponse(
                result.userId().toString(),
                result.name(),
                result.email(),
                result.phone(),
                result.role().name()
        );
    }
}
