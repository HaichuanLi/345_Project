package com.soen345.ticketing.android;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.soen345.ticketing.android.databinding.ActivityReservationConfirmationBinding;
import com.soen345.ticketing.application.reservation.ReservationConfirmation;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.UUID;

public class ReservationConfirmationActivity extends AppCompatActivity {
    public static final String EXTRA_RESERVATION_ID = "extra_reservation_id";

    private static final DateTimeFormatter DISPLAY_DATE_TIME =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm", Locale.CANADA)
                    .withZone(ZoneId.systemDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityReservationConfirmationBinding binding =
                ActivityReservationConfirmationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String reservationIdRaw = getIntent().getStringExtra(EXTRA_RESERVATION_ID);
        if (reservationIdRaw == null || reservationIdRaw.isBlank()) {
            Toast.makeText(this, getString(R.string.confirmation_missing_reservation), Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        UUID reservationId;
        try {
            reservationId = UUID.fromString(reservationIdRaw);
        } catch (IllegalArgumentException ex) {
            Toast.makeText(this, getString(R.string.confirmation_missing_reservation), Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        var confirmationOptional = TicketingDataProvider.confirmationService(this)
            .getConfirmation(reservationId);

        if (confirmationOptional.isEmpty()) {
            Toast.makeText(this, getString(R.string.confirmation_not_found), Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        var confirmation = confirmationOptional.get();

        sendSmsIfPhoneAvailable(confirmation);

        binding.confirmationTitle.setText(getString(R.string.confirmation_title));
        binding.reservationIdValue.setText(confirmation.reservationId().toString());
        binding.eventTitleValue.setText(confirmation.eventDetails().title());
        binding.eventCodeValue.setText(confirmation.eventDetails().eventCode());
        binding.eventCategoryValue.setText(confirmation.eventDetails().category());
        binding.eventVenueValue.setText(confirmation.eventDetails().venue());
        binding.eventStartValue.setText(confirmation.eventDetails().startDateTime().toString());
        binding.eventEndValue.setText(confirmation.eventDetails().endDateTime().toString());
        binding.ticketsReservedValue.setText(String.valueOf(confirmation.quantityReserved()));
        binding.reservedAtValue.setText(DISPLAY_DATE_TIME.format(confirmation.reservedAt()));

        double orderTotal = confirmation.quantityReserved() * confirmation.eventDetails().price();
        binding.orderTotalValue.setText(String.format(Locale.CANADA, "$%.2f", orderTotal));

        binding.doneButton.setOnClickListener(view -> finish());
    }

    private void sendSmsIfPhoneAvailable(ReservationConfirmation confirmation) {
        var userOptional = FakeAuthFactory.userRepository().findById(confirmation.customerId());
        if (userOptional.isEmpty()) {
            return;
        }

        String phone = userOptional.get().phone();
        if (phone == null || phone.isBlank()) {
            return;
        }

        String smsBody = buildConfirmationSmsBody(confirmation);
        Intent smsIntent = new Intent(Intent.ACTION_SENDTO);
        smsIntent.setData(Uri.parse("smsto:" + Uri.encode(phone.trim())));
        smsIntent.putExtra("sms_body", smsBody);

        if (smsIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(smsIntent);
        } else {
            Toast.makeText(this, getString(R.string.sms_app_not_available), Toast.LENGTH_LONG).show();
        }
    }

    private String buildConfirmationSmsBody(ReservationConfirmation confirmation) {
        double orderTotal = confirmation.quantityReserved() * confirmation.eventDetails().price();
        return "Reservation confirmed: "
                + confirmation.eventDetails().title()
                + " (" + confirmation.eventDetails().eventCode() + ")"
                + " at " + confirmation.eventDetails().venue()
                + " on " + confirmation.eventDetails().startDateTime()
                + ". Tickets: " + confirmation.quantityReserved()
                + ". Total: " + String.format(Locale.CANADA, "$%.2f", orderTotal)
                + ". Reservation ID: " + confirmation.reservationId();
    }
}
