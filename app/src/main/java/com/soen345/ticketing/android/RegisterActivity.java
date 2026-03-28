package com.soen345.ticketing.android;

import android.os.Bundle;
import android.view.View;
import android.widget.RadioGroup;

import androidx.appcompat.app.AppCompatActivity;

import com.soen345.ticketing.android.databinding.ActivityRegisterBinding;
import com.soen345.ticketing.application.auth.RegisterCommand;
import com.soen345.ticketing.application.auth.ValidationException;
import com.soen345.ticketing.application.usecase.auth.RegisterUseCase;
import com.soen345.ticketing.domain.user.Role;

public class RegisterActivity extends AppCompatActivity {
    private ActivityRegisterBinding binding;
    private RegisterUseCase registerUseCase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        registerUseCase = FakeAuthFactory.registerUseCase();

        binding.contactTypeGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radioEmail) {
                binding.emailInput.setVisibility(View.VISIBLE);
                binding.phoneInput.setVisibility(View.GONE);
                binding.phoneInput.setText("");
            } else {
                binding.emailInput.setVisibility(View.GONE);
                binding.phoneInput.setVisibility(View.VISIBLE);
                binding.emailInput.setText("");
            }
        });

        binding.registerButton.setOnClickListener(view -> attemptRegister());
        binding.loginLink.setOnClickListener(view -> finish());
    }

    private void attemptRegister() {
        binding.errorText.setText("");
        binding.successText.setText("");

        String name = binding.nameInput.getText().toString();
        String email = binding.emailInput.getText().toString();
        String phone = binding.phoneInput.getText().toString();
        String password = binding.passwordInput.getText().toString();

        Role role = getSelectedRole();
        if (role == null) {
            binding.errorText.setText("Please select a role");
            return;
        }

        String emailValue = email.isBlank() ? null : email;
        String phoneValue = phone.isBlank() ? null : phone;

        try {
            registerUseCase.register(new RegisterCommand(name, emailValue, phoneValue, password, role));
            binding.successText.setText("Registration successful! You can now log in.");
        } catch (ValidationException exception) {
            binding.errorText.setText(exception.getMessage());
        }
    }

    private Role getSelectedRole() {
        int checkedId = binding.roleGroup.getCheckedRadioButtonId();
        if (checkedId == R.id.roleCustomer) {
            return Role.CUSTOMER;
        } else if (checkedId == R.id.roleOrganizer) {
            return Role.ADMIN;
        }
        return null;
    }
}
