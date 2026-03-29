package com.soen345.ticketing.android;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.soen345.ticketing.android.databinding.ActivityMainBinding;
import com.soen345.ticketing.application.auth.AuthenticationException;
import com.soen345.ticketing.application.auth.LoginCommand;
import com.soen345.ticketing.application.auth.LoginResult;
import com.soen345.ticketing.application.auth.ValidationException;
import com.soen345.ticketing.application.usecase.auth.LoginUseCase;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private LoginUseCase loginUseCase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        loginUseCase = FakeAuthFactory.loginUseCase();

        TicketingDataProvider.seedEventsIfEmpty(this);

        binding.loginButton.setOnClickListener(view -> attemptLogin());
        binding.registerLink.setOnClickListener(view -> navigateToRegister());
    }
    private void attemptLogin() {
        String identifier = binding.identifierInput.getText().toString();
        String password = binding.passwordInput.getText().toString();

        binding.errorText.setText("");
        binding.loginButton.setEnabled(false);

        new Thread(() -> {
            try {
                LoginResult result = loginUseCase.login(new LoginCommand(identifier, password));
                runOnUiThread(() -> {
                    binding.loginButton.setEnabled(true);
                    navigateToResult(result);
                });
            } catch (ValidationException | AuthenticationException exception) {
                runOnUiThread(() -> {
                    binding.loginButton.setEnabled(true);
                    binding.errorText.setText(exception.getMessage());
                });
            }
        }).start();
    }

    private void navigateToResult(LoginResult result) {
        Intent intent = new Intent(this, ResultActivity.class);
        intent.putExtra(ResultActivity.EXTRA_USER_ID, result.userId().toString());
        intent.putExtra(ResultActivity.EXTRA_NAME, result.name());
        intent.putExtra(ResultActivity.EXTRA_EMAIL, result.email());
        intent.putExtra(ResultActivity.EXTRA_PHONE, result.phone());
        intent.putExtra(ResultActivity.EXTRA_ROLE, result.role().name());
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void navigateToRegister() {
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);
    }
}
