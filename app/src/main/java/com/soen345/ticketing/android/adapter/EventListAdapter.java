package com.soen345.ticketing.android.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import com.soen345.ticketing.android.databinding.ItemEventCardBinding;
import com.soen345.ticketing.domain.event.Event;
import com.soen345.ticketing.domain.event.EventStatus;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class EventListAdapter extends RecyclerView.Adapter<EventListAdapter.EventViewHolder> {
    private List<Event> events;
    private final OnEventClickListener listener;
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");

    public interface OnEventClickListener {
        void onEventClick(Event event);
    }

    public EventListAdapter(List<Event> events, OnEventClickListener listener) {
        this.events = events;
        this.listener = listener;
    }

    @Override
    public EventViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ItemEventCardBinding binding = ItemEventCardBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new EventViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(EventViewHolder holder, int position) {
        holder.bind(events.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    public void updateEvents(List<Event> events) {
        this.events = events;
        notifyDataSetChanged();
    }

    static class EventViewHolder extends RecyclerView.ViewHolder {
        private final ItemEventCardBinding binding;

        EventViewHolder(ItemEventCardBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Event event, OnEventClickListener listener) {
            binding.eventCode.setText(event.eventCode());
            binding.eventTitle.setText(event.title());
            binding.eventCategory.setText(event.category());
            binding.eventLocation.setText(event.venue());
            binding.eventStartTime.setText(event.startDateTime().format(TIME_FORMATTER));
            binding.eventEndTime.setText(event.endDateTime().format(TIME_FORMATTER));
            binding.availableSeats.setText(String.valueOf(event.availableTickets()));

            boolean cancelled = event.status() == EventStatus.CANCELLED;
            binding.cancelledLabel.setVisibility(cancelled ? View.VISIBLE : View.GONE);
            itemView.setAlpha(cancelled ? 0.5f : 1.0f);

            if (cancelled) {
                itemView.setOnClickListener(null);
                itemView.setClickable(false);
            } else {
                itemView.setClickable(true);
                itemView.setOnClickListener(v -> listener.onEventClick(event));
            }
        }
    }
}
