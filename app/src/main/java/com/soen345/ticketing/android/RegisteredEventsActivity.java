package com.soen345.ticketing.android;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.soen345.ticketing.android.adapter.RegisteredEventAdapter;
import com.soen345.ticketing.android.databinding.ActivityRegisteredEventsBinding;
import com.soen345.ticketing.application.usecase.reservation.GetUserReservationsUseCase;
import com.soen345.ticketing.application.usecase.reservation.UserReservationDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class RegisteredEventsActivity extends AppCompatActivity {
    public static final String EXTRA_USER_ID = "extra_user_id";

    private ActivityRegisteredEventsBinding binding;
    private RegisteredEventAdapter adapter;
    private UUID userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisteredEventsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String userIdRaw = getIntent().getStringExtra(EXTRA_USER_ID);
        if (userIdRaw == null || userIdRaw.isBlank()) {
            finish();
            return;
        }
        userId = UUID.fromString(userIdRaw);

        adapter = new RegisteredEventAdapter(new ArrayList<>(), reservation -> {
            Intent intent = new Intent(this, RegisteredEventDetailsActivity.class);
            intent.putExtra(RegisteredEventDetailsActivity.EXTRA_RESERVATION_ID, reservation.reservationId().toString());
            intent.putExtra(RegisteredEventDetailsActivity.EXTRA_USER_ID, userId.toString());
            startActivity(intent);
        });

        binding.registeredEventsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.registeredEventsRecyclerView.setAdapter(adapter);
        binding.backButton.setOnClickListener(view -> finish());
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadReservations();
    }

    private void loadReservations() {
        GetUserReservationsUseCase useCase = new GetUserReservationsUseCase(
                TicketingDataProvider.reservationRepository(this),
                TicketingDataProvider.eventRepository(this)
        );

        List<UserReservationDTO> reservations = useCase.execute(userId);
        adapter.updateReservations(reservations);
        binding.emptyStateText.setVisibility(reservations.isEmpty() ? View.VISIBLE : View.GONE);
    }
}
