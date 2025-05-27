package com.example.project12;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;

import com.example.project12.R;
import com.example.project12.models.Trip;
import com.example.project12.models.TripPlace;
import com.example.project12.ui.viewmodel.TripViewModel;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.firebase.firestore.GeoPoint;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TripPlanningActivity extends AppCompatActivity {
    // UI fragments
    private AutocompleteSupportFragment destFrag, stayFrag, activityFrag;

    // Inputs
    private EditText etStart, etEnd, etAccommodationCost, etCurrentActivity, etActivityCost;
    private Spinner spDaySelector;
    private Button btnAddActivity, btnGoToDetails;

    // State
    private String destinationName;
    private GeoPoint  destinationGeo;             // for Trip.destination → GeoPoint
    private TripPlace stayPlace;                  // holds accommodation
    private Place     pendingActivityPlace;       // chosen via activityFrag
    private Date      startDate, endDate;
    private final List<TripPlace> activities = new ArrayList<>();
    private final List<String>    dayLabels = new ArrayList<>();
    private ArrayAdapter<String>  dayAdapter;

    // Helpers
    private final Calendar cal = Calendar.getInstance();
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    // ViewModel
    private TripViewModel tripVM;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_planning);

        // Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Plan New Trip");
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        // ViewModel
        tripVM = new ViewModelProvider(this).get(TripViewModel.class);

        // Initialize Places SDK if needed
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(),
                    getString(R.string.places_api_key), Locale.getDefault());
        }

        // Find views
        etStart             = findViewById(R.id.etStartDate);
        etEnd               = findViewById(R.id.etEndDate);
        etAccommodationCost = findViewById(R.id.etAccommodationCost);
        etCurrentActivity   = findViewById(R.id.etCurrentActivity);
        etActivityCost      = findViewById(R.id.etActivityCost);
        spDaySelector       = findViewById(R.id.spDaySelector);
        btnAddActivity      = findViewById(R.id.btnAddActivity);
        btnGoToDetails      = findViewById(R.id.btnGoToDetails);

        // Spinner setup
        dayAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, dayLabels);
        dayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spDaySelector.setAdapter(dayAdapter);

        // Date pickers
        etStart.setOnClickListener(v -> pickDate(date -> {
            startDate = date;
            etStart.setText(sdf.format(date));
            refreshDays();
        }));
        etEnd.setOnClickListener(v -> pickDate(date -> {
            endDate = date;
            etEnd.setText(sdf.format(date));
            refreshDays();
        }));

        // Destination autocomplete
        destFrag = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.autocomplete_destination);
        destFrag.setPlaceFields(List.of(Place.Field.NAME, Place.Field.LAT_LNG));
        destFrag.setCountry("IL");
        destFrag.setHint("Destination");
        destFrag.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place p) {
                destinationName = p.getName();
                LatLng ll = p.getLatLng();
                destinationGeo = new GeoPoint(ll.latitude, ll.longitude);
            }
            @Override public void onError(@NonNull com.google.android.gms.common.api.Status s) { }
        });

        // Accommodation autocomplete
        stayFrag = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.autocomplete_accommodation);
        stayFrag.setPlaceFields(List.of(Place.Field.NAME, Place.Field.LAT_LNG));
        stayFrag.setCountry("IL");
        stayFrag.setHint("Accommodation");
        stayFrag.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place p) {
                double cost = parseDouble(etAccommodationCost.getText().toString(), 0);
                LatLng ll = p.getLatLng();
                stayPlace = new TripPlace(
                        p.getName(),
                        "accommodation",
                        new GeoPoint(ll.latitude, ll.longitude),
                        cost,
                        null,
                        p.getId()
                );
            }
            @Override public void onError(@NonNull com.google.android.gms.common.api.Status s) { }
        });

        // Activity autocomplete
        activityFrag = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.autocomplete_activity);
        activityFrag.setPlaceFields(List.of(Place.Field.NAME, Place.Field.LAT_LNG));
        activityFrag.setCountry("IL");
        activityFrag.setHint("Activity");
        activityFrag.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place p) {
                pendingActivityPlace = p;
                etCurrentActivity.setText(p.getName());
            }
            @Override public void onError(@NonNull com.google.android.gms.common.api.Status s) { }
        });

        // Add activity button
        btnAddActivity.setOnClickListener(v -> {
            if (pendingActivityPlace == null || startDate == null) return;
            int dayIdx = spDaySelector.getSelectedItemPosition();
            Date activityDate = getDateForDay(dayIdx);
            double cost = parseDouble(etActivityCost.getText().toString(), 0);
            LatLng ll = pendingActivityPlace.getLatLng();
            TripPlace tp = new TripPlace(
                    pendingActivityPlace.getName(),
                    "activity",
                    new GeoPoint(ll.latitude, ll.longitude),
                    cost,
                    activityDate,
                    pendingActivityPlace.getId()
            );
            activities.add(tp);
            etCurrentActivity.setText("");
            etActivityCost.setText("");
        });

        // Summary button → build Trip, save via VM, then launch details
        btnGoToDetails.setOnClickListener(v -> {
            if (destinationName == null || startDate == null ||
                    endDate == null || stayPlace == null) {
                Toast.makeText(this, "Fill all required fields", Toast.LENGTH_SHORT).show();
                return;
            }
            // compute total expense
            double total = stayPlace.getPrice();
            for (TripPlace a : activities) total += a.getPrice();

            // build Trip object
            String userEmail =
                    com.google.firebase.auth.FirebaseAuth.getInstance()
                            .getCurrentUser().getEmail();
            Trip trip = new Trip(
                    destinationName,
                    startDate,
                    endDate,
                    stayPlace,
                    activities,
                    total,
                    new ArrayList<>(),       // empty weather
                    "",                      // blank tripId for now
                    userEmail               // ownerEmail
            );

            // save then open details
            tripVM.saveTrip(trip);
            Intent i = new Intent(this, TripDetailsActivity.class);
            i.putExtra("trip", trip);
            startActivity(i);
            finish();
        });

        // VM error observer
        tripVM.getErrorMessage().observe(this, err -> {
            if (err != null) Toast.makeText(this, err, Toast.LENGTH_LONG).show();
        });
    }

    private void pickDate(DateCallback cb) {
        Calendar now = Calendar.getInstance();
        new DatePickerDialog(this, (DatePicker dp, int y, int m, int d) -> {
            Calendar sel = Calendar.getInstance();
            sel.set(y, m, d);
            cb.onChosen(sel.getTime());
        }, now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH))
                .show();
    }

    private void refreshDays() {
        dayLabels.clear();
        if (startDate != null && endDate != null && !endDate.before(startDate)) {
            Calendar c = Calendar.getInstance();
            c.setTime(startDate);
            int idx = 1;
            while (!c.getTime().after(endDate)) {
                dayLabels.add("Day " + idx++ + ": " + sdf.format(c.getTime()));
                c.add(Calendar.DATE, 1);
            }
        }
        dayAdapter.notifyDataSetChanged();
    }

    private Date getDateForDay(int index) {
        Calendar c = Calendar.getInstance();
        c.setTime(startDate);
        c.add(Calendar.DATE, index);
        return c.getTime();
    }

    private double parseDouble(String s, double def) {
        try { return Double.parseDouble(s); }
        catch (Exception ignored) { return def; }
    }

    private interface DateCallback { void onChosen(Date date); }
}
