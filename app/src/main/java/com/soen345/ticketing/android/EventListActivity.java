package com.soen345.ticketing.android;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.soen345.ticketing.android.adapter.EventListAdapter;
import com.soen345.ticketing.android.databinding.ActivityEventListBinding;
import com.soen345.ticketing.application.usecase.event.FilterEventsUseCase;
import com.soen345.ticketing.application.usecase.event.ListEventsUseCase;
import com.soen345.ticketing.domain.event.Event;
import com.soen345.ticketing.infrastructure.persistence.inmemory.InMemoryEventRepository;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class EventListActivity extends AppCompatActivity {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private ActivityEventListBinding binding;
    private EventListAdapter adapter;
    private FilterEventsUseCase filterEventsUseCase;
    private List<Event> allEvents = new ArrayList<>();
    private LocalDate selectedDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEventListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        InMemoryEventRepository eventRepository = new InMemoryEventRepository();
        ListEventsUseCase listEventsUseCase = new ListEventsUseCase(eventRepository);
        filterEventsUseCase = new FilterEventsUseCase();
        allEvents = listEventsUseCase.listAvailableEvents();

        adapter = new EventListAdapter(allEvents, event -> {
            // Handle event click - for now just a placeholder
            // TODO: Navigate to event details page (TR-FR-007)
        });

        binding.eventsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.eventsRecyclerView.setAdapter(adapter);
        setupFilterDropdowns();
        setupAutoFilterListeners();

        binding.filterDateInput.setOnClickListener(view -> showDatePicker());
        binding.clearFiltersButton.setOnClickListener(view -> clearFilters());

        applyFilters();
        binding.backButton.setOnClickListener(view -> finish());
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

