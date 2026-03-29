package com.soen345.ticketing.android.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import com.soen345.ticketing.android.databinding.ItemRegisteredEventCardBinding;
import com.soen345.ticketing.application.usecase.reservation.UserReservationDTO;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class RegisteredEventAdapter extends RecyclerView.Adapter<RegisteredEventAdapter.ViewHolder> {
    private List<UserReservationDTO> reservations;
    private final OnReservationClickListener listener;
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");

    public interface OnReservationClickListener {
        void onReservationClick(UserReservationDTO reservation);
    }

    public RegisteredEventAdapter(List<UserReservationDTO> reservations, OnReservationClickListener listener) {
        this.reservations = reservations;
        this.listener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ItemRegisteredEventCardBinding binding = ItemRegisteredEventCardBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.bind(reservations.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return reservations.size();
    }

    public void updateReservations(List<UserReservationDTO> reservations) {
        this.reservations = reservations;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemRegisteredEventCardBinding binding;

        ViewHolder(ItemRegisteredEventCardBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(UserReservationDTO reservation, OnReservationClickListener listener) {
            binding.eventName.setText(reservation.eventName());
            binding.eventStartTime.setText(reservation.startDateTime().format(TIME_FORMATTER));
            binding.eventEndTime.setText(reservation.endDateTime().format(TIME_FORMATTER));
            itemView.setOnClickListener(v -> listener.onReservationClick(reservation));
        }
    }
}
