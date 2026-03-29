package com.soen345.ticketing.android;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.soen345.ticketing.android.databinding.ActivityAddEventBinding;
import com.soen345.ticketing.application.auth.ValidationException;
import com.soen345.ticketing.application.event.AddEventCommand;
import com.soen345.ticketing.application.usecase.event.AddEventUseCase;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class AddEventActivity extends AppCompatActivity {
    public static final String EXTRA_USER_ID = "extra_user_id";

    private static final DateTimeFormatter DISPLAY_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private ActivityAddEventBinding binding;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private UUID organizerId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddEventBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String userId = getIntent().getStringExtra(EXTRA_USER_ID);
        organizerId = (userId != null && !userId.isBlank())
                ? UUID.fromString(userId)
                : UUID.randomUUID();

        binding.startTimeInput.setOnClickListener(v -> pickDateTime(true));
        binding.endTimeInput.setOnClickListener(v -> pickDateTime(false));
        binding.saveButton.setOnClickListener(v -> onSaveClicked());
        binding.backButton.setOnClickListener(v -> finish());
    }

    private void pickDateTime(boolean isStart) {
        LocalDateTime now = LocalDateTime.now();
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
            }, now.getHour(), now.getMinute(), true).show();
        }, now.getYear(), now.getMonthValue() - 1, now.getDayOfMonth()).show();
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

        AddEventCommand command = new AddEventCommand(
                binding.eventCodeInput.getText().toString(),
                binding.eventTitleInput.getText().toString(),
                binding.eventCategoryInput.getText().toString(),
                binding.eventDescriptionInput.getText().toString(),
                binding.eventLocationInput.getText().toString(),
                startDateTime,
                endDateTime,
                seats,
                price,
                organizerId
        );

        try {
            AddEventUseCase useCase = new AddEventUseCase(
                    TicketingDataProvider.eventRepository(this)
            );
            useCase.execute(command);
            finish();
        } catch (ValidationException e) {
            binding.errorText.setText(e.getMessage());
        }
    }
}
