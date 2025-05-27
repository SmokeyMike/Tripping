package com.example.project12;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;

import com.example.project12.models.Trip;
import com.example.project12.models.TripPlace;
import com.example.project12.ui.viewmodel.TripViewModel;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class TripDetailsActivity extends AppCompatActivity implements OnMapReadyCallback {
    private static final String TAG = "TripDetails";
    private TripViewModel tripVM;
    private Trip trip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_details);
        Log.d(TAG, "onCreate()");

        // Toolbar
        Toolbar toolbar = findViewById(R.id.toolbarDetails);
        toolbar.setTitle("Trip Details");
        setSupportActionBar(toolbar);

        // ViewModel
        tripVM = new ViewModelProvider(this).get(TripViewModel.class);

        // Get Trip from Intent and set in ViewModel
        Trip incoming = (Trip) getIntent().getParcelableExtra("trip");
        if (incoming != null) {
            tripVM.setSelectedTrip(incoming);
        }

        // Observe Trip
        tripVM.getSelectedTrip().observe(this, t -> {
            Log.d(TAG, "Trip being observed");
            if (t == null) {Log.d(TAG, "Trip was null");return;}
            trip = t;

            // Load summary fragment
            TripSummaryFragment summary = new TripSummaryFragment();
            Bundle args = new Bundle();
            args.putParcelable("trip", trip);
            summary.setArguments(args);
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.summary_container, summary)
                    .commit();

            // Initialize map
            SupportMapFragment mapFrag = (SupportMapFragment)
                    getSupportFragmentManager()
                            .findFragmentById(R.id.map_fragment);
            if (mapFrag != null) {
                mapFrag.getMapAsync(this);
            }
        });
        // Observe errors
        tripVM.getErrorMessage().observe(this, msg -> {
            if (msg != null) {
                Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
                tripVM.clearErrorMessage();
            }
        });

        // Buttons
        Button btnEdit = findViewById(R.id.btnEdit);
        btnEdit.clearFocus();
        Button btnSave = findViewById(R.id.btnSave);

        btnEdit.setOnClickListener(v -> {
            // Edit: go back to planning with the same trip
            Log.d(TAG, "Edit clicked — navigating to TripPlanningActivity");
            Intent i = new Intent(this, TripPlanningActivity.class);
            i.putExtra("trip", trip);
            startActivity(i);
            finish();
        });

        btnSave.setOnClickListener(v -> {
            if (trip == null) return;
            Log.d(TAG, "Save clicked — calling saveTrip()");
            tripVM.saveTrip(trip);
            finish();
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        if (trip == null) return;

        // Add stay marker
        TripPlace stay = trip.getStay();
        LatLng stayPos = new LatLng(
                stay.getLocation().getLatitude(),
                stay.getLocation().getLongitude()
        );
        googleMap.addMarker(new MarkerOptions()
                .position(stayPos)
                .title(stay.getName())
        );
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(stayPos, 12f));

        // Add activity markers
        for (TripPlace act : trip.getActivities()) {
            LatLng pos = new LatLng(
                    act.getLocation().getLatitude(),
                    act.getLocation().getLongitude()
            );
            googleMap.addMarker(new MarkerOptions()
                    .position(pos)
                    .title(act.getName())
            );
        }
    }
}
