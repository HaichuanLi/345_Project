package com.soen345.ticketing.android;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.soen345.ticketing.android.databinding.ActivityRegisteredEventDetailsBinding;
import com.soen345.ticketing.application.auth.ValidationException;
import com.soen345.ticketing.application.usecase.reservation.CancelReservationUseCase;
import com.soen345.ticketing.application.usecase.reservation.GetUserReservationsUseCase;
import com.soen345.ticketing.application.usecase.reservation.UserReservationDTO;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class RegisteredEventDetailsActivity extends AppCompatActivity {
    public static final String EXTRA_RESERVATION_ID = "extra_reservation_id";
    public static final String EXTRA_USER_ID = "extra_user_id";

    private static final DateTimeFormatter DISPLAY_DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private ActivityRegisteredEventDetailsBinding binding;
    private UUID reservationId;
    private UUID userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisteredEventDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String reservationIdRaw = getIntent().getStringExtra(EXTRA_RESERVATION_ID);
        String userIdRaw = getIntent().getStringExtra(EXTRA_USER_ID);

        if (reservationIdRaw == null || reservationIdRaw.isBlank()
                || userIdRaw == null || userIdRaw.isBlank()) {
            Toast.makeText(this, getString(R.string.registered_event_details_missing), Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        reservationId = UUID.fromString(reservationIdRaw);
        userId = UUID.fromString(userIdRaw);

        bindDetails();

        binding.cancelReservationButton.setOnClickListener(v -> showCancelConfirmation());
        binding.backButton.setOnClickListener(v -> finish());
    }

    private void bindDetails() {
        GetUserReservationsUseCase useCase = new GetUserReservationsUseCase(
                TicketingDataProvider.reservationRepository(this),
                TicketingDataProvider.eventRepository(this)
        );

        List<UserReservationDTO> reservations = useCase.execute(userId);
        UserReservationDTO reservation = reservations.stream()
                .filter(r -> r.reservationId().equals(reservationId))
                .findFirst()
                .orElse(null);

        if (reservation == null) {
            Toast.makeText(this, getString(R.string.registered_event_details_not_found), Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        binding.eventName.setText(reservation.eventName());
        binding.eventCode.setText(reservation.eventCode());
        binding.eventCategory.setText(reservation.category());
        binding.eventVenue.setText(reservation.venue());
        binding.eventStartTime.setText(reservation.startDateTime().format(DISPLAY_DATE_TIME));
        binding.eventEndTime.setText(reservation.endDateTime().format(DISPLAY_DATE_TIME));
        binding.eventDescription.setText(reservation.description());
        binding.ticketsPurchased.setText(String.valueOf(reservation.ticketsPurchased()));
        binding.totalPrice.setText(String.format(Locale.CANADA, "$%.2f", reservation.totalPrice()));
    }

    private void showCancelConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.cancel_reservation_confirm_title))
                .setMessage(getString(R.string.cancel_reservation_confirm_message))
                .setPositiveButton(getString(R.string.cancel_reservation_confirm_yes), (dialog, which) -> {
                    cancelReservation();
                })
                .setNegativeButton(getString(R.string.cancel_reservation_confirm_no), null)
                .show();
    }

    private void cancelReservation() {
        try {
            CancelReservationUseCase useCase = new CancelReservationUseCase(
                    TicketingDataProvider.reservationRepository(this),
                    TicketingDataProvider.eventRepository(this)
            );
            useCase.execute(reservationId);
            finish();
        } catch (ValidationException e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
