package com.soen345.ticketing.android;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.soen345.ticketing.android.databinding.ActivityAddEventBinding;
import com.soen345.ticketing.application.auth.ValidationException;
import com.soen345.ticketing.application.event.AdminEventAccessPolicy;
import com.soen345.ticketing.application.event.EditEventCommand;
import com.soen345.ticketing.application.usecase.event.EditEventUseCase;
import com.soen345.ticketing.domain.event.Event;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.UUID;

public class EditEventActivity extends AppCompatActivity {
    public static final String EXTRA_EVENT_ID = "extra_event_id";
    public static final String EXTRA_USER_ID = "extra_user_id";

    private static final DateTimeFormatter DISPLAY_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private ActivityAddEventBinding binding;
    private UUID eventId;
    private UUID loggedInUserId;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private final AdminEventAccessPolicy adminEventAccessPolicy = new AdminEventAccessPolicy();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddEventBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.pageTitle.setText(getString(R.string.edit_event_title));

        String eventIdRaw = getIntent().getStringExtra(EXTRA_EVENT_ID);
        String userIdRaw = getIntent().getStringExtra(EXTRA_USER_ID);
        if (eventIdRaw == null || eventIdRaw.isBlank()) {
            Toast.makeText(this, getString(R.string.event_details_missing_event), Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        if (userIdRaw == null || userIdRaw.isBlank()) {
            Toast.makeText(this, getString(R.string.event_details_missing_event), Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        try {
            eventId = UUID.fromString(eventIdRaw);
            loggedInUserId = UUID.fromString(userIdRaw);
        } catch (IllegalArgumentException ex) {
            Toast.makeText(this, getString(R.string.event_details_missing_event), Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        prefillFields();

        binding.startTimeInput.setOnClickListener(v -> pickDateTime(true));
        binding.endTimeInput.setOnClickListener(v -> pickDateTime(false));
        binding.saveButton.setOnClickListener(v -> onSaveClicked());
        binding.backButton.setOnClickListener(v -> finish());
    }

    private void prefillFields() {
        new Thread(() -> {
            Event event = TicketingDataProvider.eventRepository(this)
                    .findById(eventId)
                    .orElse(null);

            if (event == null) {
                runOnUiThread(() -> {
                    Toast.makeText(this, getString(R.string.event_details_not_found), Toast.LENGTH_LONG).show();
                    finish();
                });
                return;
            }

            if (!adminEventAccessPolicy.canEditOrCancel(loggedInUserId, event)) {
                runOnUiThread(() -> {
                    Toast.makeText(this, "You can only modify events you created.", Toast.LENGTH_LONG).show();
                    finish();
                });
                return;
            }

            runOnUiThread(() -> {
                binding.eventCodeInput.setText(event.eventCode());
                binding.eventTitleInput.setText(event.title());
                binding.eventCategoryInput.setText(event.category());
                binding.eventLocationInput.setText(event.venue());
                binding.eventDescriptionInput.setText(event.description());
                binding.eventPriceInput.setText(String.format(Locale.US, "%.2f", event.price()));
                binding.eventSeatsInput.setText(String.valueOf(event.capacity()));
                startDateTime = event.startDateTime();
                endDateTime = event.endDateTime();
                binding.startTimeInput.setText(startDateTime.format(DISPLAY_FORMAT));
                binding.endTimeInput.setText(endDateTime.format(DISPLAY_FORMAT));
            });
        }).start();
    }

    private void pickDateTime(boolean isStart) {
        LocalDateTime initial = isStart && startDateTime != null ? startDateTime
                : !isStart && endDateTime != null ? endDateTime
                : LocalDateTime.now();

        new DatePickerDialog(this, (dv, year, month, day) -> {
            new TimePickerDialog(this, (tv, hour, minute) -> {
                LocalDateTime picked = LocalDateTime.of(year, month + 1, day, hour, minute);
                if (isStart) {
                    startDateTime = picked;
                    binding.startTimeInput.setText(picked.format(DISPLAY_FORMAT));
                } else {
                    endDateTime = picked;
                    binding.endTimeInput.setText(picked.format(DISPLAY_FORMAT));
                }
            }, initial.getHour(), initial.getMinute(), true).show();
        }, initial.getYear(), initial.getMonthValue() - 1, initial.getDayOfMonth()).show();
    }

    private void onSaveClicked() {
        binding.errorText.setText("");

        String priceText = binding.eventPriceInput.getText().toString().trim();
        String seatsText = binding.eventSeatsInput.getText().toString().trim();

        double price;
        int seats;
        try {
            price = priceText.isEmpty() ? -1 : Double.parseDouble(priceText);
        } catch (NumberFormatException e) {
            binding.errorText.setText(getString(R.string.event_price_hint));
            return;
        }
        try {
            seats = seatsText.isEmpty() ? 0 : Integer.parseInt(seatsText);
        } catch (NumberFormatException e) {
            binding.errorText.setText(getString(R.string.event_seats_hint));
            return;
        }

        EditEventCommand command = new EditEventCommand(
                eventId,
                binding.eventCodeInput.getText().toString(),
                binding.eventTitleInput.getText().toString(),
                binding.eventCategoryInput.getText().toString(),
                binding.eventDescriptionInput.getText().toString(),
                binding.eventLocationInput.getText().toString(),
                startDateTime,
                endDateTime,
                seats,
                price
        );

        binding.saveButton.setEnabled(false);

        new Thread(() -> {
            try {
                EditEventUseCase useCase = new EditEventUseCase(
                        TicketingDataProvider.eventRepository(this),
                        TicketingDataProvider.reservationRepository(this)
                );
                useCase.execute(command);
                runOnUiThread(this::finish);
            } catch (ValidationException e) {
                runOnUiThread(() -> {
                    binding.saveButton.setEnabled(true);
                    binding.errorText.setText(e.getMessage());
                });
            }
        }).start();
    }
}
