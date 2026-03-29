package com.soen345.ticketing.android;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.soen345.ticketing.android.adapter.EventListAdapter;
import com.soen345.ticketing.android.databinding.ActivityEventListBinding;
import com.soen345.ticketing.application.usecase.event.FilterEventsUseCase;
import com.soen345.ticketing.domain.event.Event;
import com.soen345.ticketing.domain.event.EventRepository;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

public class EventListActivity extends AppCompatActivity {
    public static final String EXTRA_USER_ID = "extra_user_id";
    public static final String EXTRA_ROLE = "extra_role";

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private ActivityEventListBinding binding;
    private EventListAdapter adapter;
    private EventRepository eventRepository;
    private FilterEventsUseCase filterEventsUseCase;
    private List<Event> allEvents = new ArrayList<>();
    private LocalDate selectedDate;
    private String loggedInUserId;
    private String userRole;
    private boolean isAdmin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEventListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String userId = getIntent().getStringExtra(EXTRA_USER_ID);
        if (userId == null || userId.isBlank()) {
            userId = UUID.randomUUID().toString();
        }
        loggedInUserId = userId;
        userRole = getIntent().getStringExtra(EXTRA_ROLE);
        isAdmin = "ADMIN".equals(userRole);

        eventRepository = TicketingDataProvider.eventRepository(this);
        filterEventsUseCase = new FilterEventsUseCase();
        loadEvents();

        adapter = new EventListAdapter(allEvents, event -> {
            Intent intent = new Intent(this, EventDetailsActivity.class);
            intent.putExtra(EventDetailsActivity.EXTRA_EVENT_ID, event.id().toString());
            intent.putExtra(EventDetailsActivity.EXTRA_USER_ID, loggedInUserId);
            intent.putExtra(EventDetailsActivity.EXTRA_ROLE, userRole);
            startActivity(intent);
        });

        binding.eventsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.eventsRecyclerView.setAdapter(adapter);
        setupFilterDropdowns();
        setupAutoFilterListeners();

        binding.filterDateInput.setOnClickListener(view -> showDatePicker());
        binding.clearFiltersButton.setOnClickListener(view -> clearFilters());
        binding.backButton.setOnClickListener(view -> finish());

        if (isAdmin) {
            binding.addEventButton.setVisibility(View.VISIBLE);
            binding.addEventButton.setOnClickListener(view -> {
                Intent intent = new Intent(this, AddEventActivity.class);
                intent.putExtra(AddEventActivity.EXTRA_USER_ID, loggedInUserId);
                startActivity(intent);
            });
        }

        applyFilters();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshEvents();
    }

    private void loadEvents() {
        allEvents = eventRepository.listAll();
    }

    private void refreshEvents() {
        loadEvents();
        setupFilterDropdowns();
        applyFilters();
    }

    private void showDatePicker() {
        LocalDate initialDate = selectedDate != null ? selectedDate : LocalDate.now();
        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    selectedDate = LocalDate.of(year, month + 1, dayOfMonth);
                    binding.filterDateInput.setText(selectedDate.format(DATE_FORMATTER));
                    applyFilters();
                },
                initialDate.getYear(),
                initialDate.getMonthValue() - 1,
                initialDate.getDayOfMonth()
        );
        dialog.show();
    }

    private void clearFilters() {
        selectedDate = null;
        binding.filterDateInput.setText("");
        binding.filterLocationInput.setSelection(0);
        binding.filterCategoryInput.setSelection(0);
        applyFilters();
    }

    private void applyFilters() {
        String selectedLocation = binding.filterLocationInput.getSelectedItem() == null
            ? ""
            : binding.filterLocationInput.getSelectedItem().toString();
        String selectedCategory = binding.filterCategoryInput.getSelectedItem() == null
            ? ""
            : binding.filterCategoryInput.getSelectedItem().toString();

        boolean allLocationsSelected = selectedLocation.equals(getString(R.string.filter_all_locations));
        boolean allCategoriesSelected = selectedCategory.equals(getString(R.string.filter_all_categories));

        String locationFilter = allLocationsSelected ? "" : selectedLocation;
        String categoryFilter = allCategoriesSelected ? "" : selectedCategory;

        List<Event> filteredEvents = filterEventsUseCase.filter(
            allEvents,
            selectedDate,
            locationFilter,
            categoryFilter
        );

        adapter.updateEvents(filteredEvents);
        binding.emptyStateText.setVisibility(filteredEvents.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void setupFilterDropdowns() {
        List<String> locationOptions = new ArrayList<>();
        locationOptions.add(getString(R.string.filter_all_locations));
        locationOptions.addAll(uniqueValuesFromEvents(true));

        List<String> categoryOptions = new ArrayList<>();
        categoryOptions.add(getString(R.string.filter_all_categories));
        categoryOptions.addAll(uniqueValuesFromEvents(false));

        ArrayAdapter<String> locationAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                locationOptions
        );
        locationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.filterLocationInput.setAdapter(locationAdapter);

        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                categoryOptions
        );
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.filterCategoryInput.setAdapter(categoryAdapter);
    }

    private List<String> uniqueValuesFromEvents(boolean useVenue) {
        Set<String> uniqueValues = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        for (Event event : allEvents) {
            uniqueValues.add(useVenue ? event.venue() : event.category());
        }
        return new ArrayList<>(uniqueValues);
    }

    private void setupAutoFilterListeners() {
        AdapterView.OnItemSelectedListener listener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                applyFilters();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                applyFilters();
            }
        };

        binding.filterLocationInput.setOnItemSelectedListener(listener);
        binding.filterCategoryInput.setOnItemSelectedListener(listener);
    }
}
