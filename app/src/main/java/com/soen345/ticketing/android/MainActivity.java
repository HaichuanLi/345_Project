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
        binding.demoCredentials.setText(getString(R.string.demo_credentials));
        binding.loginButton.setOnClickListener(view -> attemptLogin());
    }

    private void attemptLogin() {
        binding.errorText.setText("");

        String email = binding.emailInput.getText().toString();
        String password = binding.passwordInput.getText().toString();

        try {
            LoginResult result = loginUseCase.login(new LoginCommand(email, password));
            Intent intent = new Intent(this, ResultActivity.class);
            intent.putExtra(ResultActivity.EXTRA_NAME, result.name());
            intent.putExtra(ResultActivity.EXTRA_EMAIL, result.email());
            intent.putExtra(ResultActivity.EXTRA_ROLE, result.role().name());
            startActivity(intent);
        } catch (ValidationException | AuthenticationException exception) {
            binding.errorText.setText(exception.getMessage());
        }
    }
}
