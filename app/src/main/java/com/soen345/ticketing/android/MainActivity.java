package com.soen345.ticketing.android;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.soen345.ticketing.android.databinding.ActivityMainBinding;
import com.soen345.ticketing.application.auth.AuthenticationException;
import com.soen345.ticketing.application.auth.LoginCommand;
import com.soen345.ticketing.application.auth.LoginResult;
import com.soen345.ticketing.application.auth.ValidationException;
import com.soen345.ticketing.application.usecase.auth.LoginUseCase;

public class MainActivity extends AppCompatActivity {
    private static final boolean DEBUG_AUTO_LOGIN_ENABLED = true;
    private static final String DEBUG_ADMIN_EMAIL = "admin@gmail.com";
    private static final String DEBUG_ADMIN_SECRET = new String(
            new char[]{'a', 'd', 'm', 'i', 'n', 'p', 'a', 's', 's'}
    );

    private ActivityMainBinding binding;
    private LoginUseCase loginUseCase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        loginUseCase = FakeAuthFactory.loginUseCase();
        binding.demoCredentials.setText(getString(R.string.demo_credentials));
        binding.loginButton.setOnClickListener(view -> attemptLogin());

        if (isDebugBuild() && DEBUG_AUTO_LOGIN_ENABLED) {
            attemptLogin(DEBUG_ADMIN_EMAIL, DEBUG_ADMIN_SECRET);
        }
    }

    private boolean isDebugBuild() {
        return (getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
    }

    private void attemptLogin() {
        String email = binding.emailInput.getText().toString();
        String password = binding.passwordInput.getText().toString();

        attemptLogin(email, password);
    }

    private void attemptLogin(String email, String password) {
        binding.errorText.setText("");

        try {
            LoginResult result = loginUseCase.login(new LoginCommand(email, password));
            navigateToResult(result);
        } catch (ValidationException | AuthenticationException exception) {
            binding.errorText.setText(exception.getMessage());
        }
    }

    private void navigateToResult(LoginResult result) {
        Intent intent = new Intent(this, ResultActivity.class);
        intent.putExtra(ResultActivity.EXTRA_NAME, result.name());
        intent.putExtra(ResultActivity.EXTRA_EMAIL, result.email());
        intent.putExtra(ResultActivity.EXTRA_ROLE, result.role().name());
        startActivity(intent);
    }
}
