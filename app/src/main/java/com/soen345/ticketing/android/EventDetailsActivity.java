package com.soen345.ticketing.android;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.soen345.ticketing.android.databinding.ActivityEventDetailsBinding;
import com.soen345.ticketing.application.auth.ValidationException;
import com.soen345.ticketing.application.event.AdminEventAccessPolicy;
import com.soen345.ticketing.application.reservation.InsufficientSeatsException;
import com.soen345.ticketing.application.reservation.ReserveTicketsCommand;
import com.soen345.ticketing.application.reservation.ReserveTicketsValidator;
import com.soen345.ticketing.application.reservation.ReservationConfirmation;
import com.soen345.ticketing.application.usecase.event.CancelEventUseCase;
import com.soen345.ticketing.application.usecase.event.ViewEventDetailsUseCase;
import com.soen345.ticketing.application.usecase.reservation.ReserveTicketsUseCase;
import com.soen345.ticketing.domain.event.Event;
import com.soen345.ticketing.domain.event.EventStatus;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import java.util.UUID;

public class EventDetailsActivity extends AppCompatActivity {
    public static final String EXTRA_USER_ID = "extra_user_id";
    public static final String EXTRA_EVENT_ID = "extra_event_id";
    public static final String EXTRA_ROLE = "extra_role";

    private static final DateTimeFormatter INPUT_DATE_TIME = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private static final DateTimeFormatter DISPLAY_DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private ActivityEventDetailsBinding binding;
    private UUID eventId;
    private UUID userId;
    private int ticketsLeft;
    private double price;
    private boolean isAdmin;
    private String userRole;
    private final AdminEventAccessPolicy adminEventAccessPolicy = new AdminEventAccessPolicy();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEventDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        userRole = getIntent().getStringExtra(EXTRA_ROLE);
        isAdmin = "ADMIN".equals(userRole);

        bindEventDetails();
        setupOrderForm();

        binding.backButton.setOnClickListener(view -> finish());
        binding.reserveTicketButton.setOnClickListener(view -> onReserveClicked());
    }

    @Override
    protected void onResume() {
        super.onResume();
        bindEventDetails();
    }

    private void bindEventDetails() {
        String eventIdRaw = getIntent().getStringExtra(EXTRA_EVENT_ID);
        String userIdRaw = getIntent().getStringExtra(EXTRA_USER_ID);
        if (eventIdRaw == null || eventIdRaw.isBlank()) {
            Toast.makeText(this, getString(R.string.event_details_missing_event), Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        try {
            eventId = UUID.fromString(eventIdRaw);
            userId = (userIdRaw == null || userIdRaw.isBlank())
                    ? UUID.randomUUID()
                    : UUID.fromString(userIdRaw);
        } catch (IllegalArgumentException ex) {
            Toast.makeText(this, getString(R.string.event_details_missing_event), Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        ViewEventDetailsUseCase viewEventDetailsUseCase =
                new ViewEventDetailsUseCase(TicketingDataProvider.eventRepository(this));

        var eventDetailsOptional = viewEventDetailsUseCase.getEventDetails(eventId);
        if (eventDetailsOptional.isEmpty()) {
            Toast.makeText(this, getString(R.string.event_details_not_found), Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        var eventDetails = eventDetailsOptional.get();
        ticketsLeft = eventDetails.ticketsLeft();
        price = eventDetails.pricePerTicket();

        binding.eventTitle.setText(nonEmpty(eventDetails.eventName()));
        binding.eventCode.setText(nonEmpty(eventDetails.eventCode()));
        binding.eventCategory.setText(nonEmpty(eventDetails.category()));
        binding.eventVenue.setText(nonEmpty(eventDetails.venue()));
        binding.eventStartTime.setText(formatDateTime(eventDetails.startDateTime().toString()));
        binding.eventEndTime.setText(formatDateTime(eventDetails.endDateTime().toString()));
        binding.eventDescription.setText(nonEmpty(eventDetails.fullDescription()));

        binding.availableSeats.setText(String.valueOf(eventDetails.totalCapacity()));
        binding.ticketsLeft.setText(String.valueOf(ticketsLeft));
        binding.price.setText(formatMoney(price));

        binding.ticketQuantityInput.setHint(getString(R.string.event_details_quantity_hint));
        updateOrderTotal();

        // Check if event is cancelled
        Event event = TicketingDataProvider.eventRepository(this).findById(eventId).orElse(null);
        boolean isCancelled = event != null && event.status() == EventStatus.CANCELLED;
        boolean canAdminManageEvent = isAdmin && adminEventAccessPolicy.canEditOrCancel(userId, event);

        if (isCancelled) {
            binding.cancelledBanner.setVisibility(View.VISIBLE);
            binding.reserveTicketButton.setVisibility(View.GONE);
            binding.ticketQuantityInput.setVisibility(View.GONE);
            binding.cancelEventButton.setVisibility(View.GONE);
            binding.editEventButton.setVisibility(View.GONE);
        } else if (canAdminManageEvent) {
            binding.cancelEventButton.setVisibility(View.VISIBLE);
            binding.cancelEventButton.setOnClickListener(v -> showCancelConfirmation());
            binding.editEventButton.setVisibility(View.VISIBLE);
            binding.editEventButton.setOnClickListener(v -> {
                Intent intent = new Intent(this, EditEventActivity.class);
                intent.putExtra(EditEventActivity.EXTRA_EVENT_ID, eventId.toString());
                intent.putExtra(EditEventActivity.EXTRA_USER_ID, userId.toString());
                startActivity(intent);
            });
        } else {
            binding.cancelEventButton.setVisibility(View.GONE);
            binding.editEventButton.setVisibility(View.GONE);
        }
    }

    private void showCancelConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.cancel_event_confirm_title))
                .setMessage(getString(R.string.cancel_event_confirm_message))
                .setPositiveButton(getString(R.string.cancel_event_confirm_yes), (dialog, which) -> {
                    cancelEvent();
                })
                .setNegativeButton(getString(R.string.cancel_event_confirm_no), null)
                .show();
    }

    private void cancelEvent() {
        try {
            Event event = TicketingDataProvider.eventRepository(this).findById(eventId).orElse(null);
            if (!adminEventAccessPolicy.canEditOrCancel(userId, event)) {
                Toast.makeText(this, "You can only modify events you created.", Toast.LENGTH_LONG).show();
                return;
            }

            CancelEventUseCase cancelUseCase = new CancelEventUseCase(
                    TicketingDataProvider.eventRepository(this)
            );
            cancelUseCase.execute(eventId);
            finish();
        } catch (ValidationException e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void setupOrderForm() {
        binding.ticketQuantityInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateOrderTotal();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void updateOrderTotal() {
        int quantity = parseQuantity();
        double total = Math.max(0, quantity) * price;
        binding.orderTotal.setText(formatMoney(total));
    }

    private void onReserveClicked() {
        int quantity = parseQuantity();
        if (quantity <= 0) {
            binding.ticketQuantityInput.setError(getString(R.string.event_details_quantity_error_min));
            return;
        }

        if (quantity > ticketsLeft) {
            binding.ticketQuantityInput.setError(getString(R.string.event_details_quantity_error_max, ticketsLeft));
            return;
        }

        try {
            ReserveTicketsUseCase reserveTicketsUseCase = new ReserveTicketsUseCase(
                    TicketingDataProvider.eventRepository(this),
                    TicketingDataProvider.reservationRepository(this),
                    TicketingDataProvider.confirmationService(this),
                    new ReserveTicketsValidator()
            );

            ReservationConfirmation confirmation = reserveTicketsUseCase.reserve(
                    new ReserveTicketsCommand(userId, eventId, quantity)
            );

            Intent confirmationIntent = new Intent(this, ReservationConfirmationActivity.class);
            confirmationIntent.putExtra(
                    ReservationConfirmationActivity.EXTRA_RESERVATION_ID,
                    confirmation.reservationId().toString()
            );
            startActivity(confirmationIntent);
            finish();
        } catch (InsufficientSeatsException ex) {
            binding.ticketQuantityInput.setError(getString(
                    R.string.event_details_quantity_error_max,
                    ex.getAvailableSeats()
            ));
        } catch (RuntimeException ex) {
            Toast.makeText(this, getString(R.string.reservation_failed_message), Toast.LENGTH_LONG).show();
        }
    }

    private int parseQuantity() {
        String text = binding.ticketQuantityInput.getText() == null
                ? ""
                : binding.ticketQuantityInput.getText().toString().trim();
        if (text.isEmpty()) {
            return 0;
        }

        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException ex) {
            return 0;
        }
    }

    private String formatDateTime(String rawDateTime) {
        if (rawDateTime == null || rawDateTime.isBlank()) {
            return "-";
        }

        try {
            LocalDateTime parsed = LocalDateTime.parse(rawDateTime, INPUT_DATE_TIME);
            return parsed.format(DISPLAY_DATE_TIME);
        } catch (DateTimeParseException ex) {
            return rawDateTime;
        }
    }

    private String formatMoney(double amount) {
        return String.format(Locale.CANADA, "$%.2f", amount);
    }

    private String nonEmpty(String value) {
        if (value == null || value.isBlank()) {
            return "-";
        }
        return value;
    }
}
