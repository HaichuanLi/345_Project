package com.soen345.ticketing.android;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.soen345.ticketing.android.databinding.ActivityResultBinding;

public class ResultActivity extends AppCompatActivity {
    public static final String EXTRA_NAME = "extra_name";
    public static final String EXTRA_EMAIL = "extra_email";
    public static final String EXTRA_PHONE = "extra_phone";
    public static final String EXTRA_ROLE = "extra_role";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityResultBinding binding = ActivityResultBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String name = getIntent().getStringExtra(EXTRA_NAME);
        String email = getIntent().getStringExtra(EXTRA_EMAIL);
        String phone = getIntent().getStringExtra(EXTRA_PHONE);
        String role = getIntent().getStringExtra(EXTRA_ROLE);

        String displayRole = "ADMIN".equals(role) ? "Event Organizer" : "Customer";
        String contact = (email != null && !email.isEmpty()) ? email : phone;

        binding.resultTitle.setText(getString(R.string.login_success_title));
        binding.resultSubtitle.setText(getString(R.string.login_success_subtitle, name, displayRole, contact));

        binding.viewEventsButton.setOnClickListener(view -> {
            Intent intent = new Intent(this, EventListActivity.class);
            startActivity(intent);
        });

        binding.logoutButton.setOnClickListener(view -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }
}
