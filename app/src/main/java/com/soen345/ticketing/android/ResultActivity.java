package com.soen345.ticketing.android;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.soen345.ticketing.android.databinding.ActivityResultBinding;

public class ResultActivity extends AppCompatActivity {
    public static final String EXTRA_NAME = "extra_name";
    public static final String EXTRA_EMAIL = "extra_email";
    public static final String EXTRA_ROLE = "extra_role";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityResultBinding binding = ActivityResultBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String name = getIntent().getStringExtra(EXTRA_NAME);
        String email = getIntent().getStringExtra(EXTRA_EMAIL);
        String role = getIntent().getStringExtra(EXTRA_ROLE);

        binding.resultTitle.setText(getString(R.string.login_success_title));
        binding.resultSubtitle.setText(getString(R.string.login_success_subtitle, name, role, email));
        binding.doneButton.setOnClickListener(view -> finish());
    }
}
