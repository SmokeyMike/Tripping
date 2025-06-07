package com.example.project12;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentTransaction;
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
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;
import java.util.Objects;

/**
 * TripDetailsActivity.java
 *
 * - Reads the full Trip object from the Intent ("selectedTrip").
 * - Uses TripViewModel via ViewModelProvider (not a static call).
 * - Loads TripSummaryFragment and SupportMapFragment.
 * - Places markers for accommodation and activities.
 * - “Edit Trip” → TripPlanningActivity; “Save Trip” → tripVM.saveTrip(trip).
 */
public class TripDetailsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "TripDetailsActivity";
    public static final String EXTRA_TRIP = "selectedTrip";

    private Trip trip;
    private String ownerEmail;
    private GoogleMap googleMap;
    private TripViewModel tripVM;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_details);

        // 1) Toolbar
        Toolbar toolbar = findViewById(R.id.toolbarDetails);
        toolbar.setTitle("Trip Details");
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        // 2) Obtain TripViewModel instance properly
        tripVM = new ViewModelProvider(this).get(TripViewModel.class);

        // 3) Read Trip from Intent
        trip = getIntent().getParcelableExtra("trip");
        ownerEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        if (trip == null) {
            Toast.makeText(this, "No trip data available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // 4) Load TripSummaryFragment into summary_container
        TripSummaryFragment summaryFragment = TripSummaryFragment.newInstance(trip);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.summary_container, summaryFragment);
        ft.commit();

        // 5) Initialize SupportMapFragment and set callback
        SupportMapFragment mapFrag = (SupportMapFragment)
                getSupportFragmentManager().findFragmentById(R.id.map_fragment);
        if (mapFrag != null) {
            mapFrag.getMapAsync(this);
        } else {
            Log.e(TAG, "SupportMapFragment not found in layout.");
        }

        // 6) Edit Trip button
        Button btnEdit = findViewById(R.id.btnEdit);
        btnEdit.setOnClickListener(v -> {
            Intent editIntent = new Intent(this, TripPlanningActivity.class);
            editIntent.putExtra("trip", trip);
            startActivity(editIntent);
            finish();
        });

        // 7) Save Trip button (now using tripVM instance)
        Button btnSave = findViewById(R.id.btnSave);
        boolean back;
        if (Objects.equals(trip.getOwnerEmail(), ownerEmail)) {back = false;}
        else {
            btnSave.setText("Back");
            back = true;
        }
        btnSave.setOnClickListener(v -> {
            if (!back){
                tripVM.saveTrip(trip);
                Toast.makeText(this, "Trip saved", Toast.LENGTH_SHORT).show();
            }
            finish();
        });
    }

    //─── OnMapReadyCallback ─────────────────────────────────────────────────────────────────────────────

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        this.googleMap = map;

        // a) Add marker for accommodation (stay)
        TripPlace stay = trip.getStay();
        LatLng stayPos = new LatLng(
                stay.getLocation().getLatitude(),
                stay.getLocation().getLongitude()
        );
        googleMap.addMarker(new MarkerOptions()
                .position(stayPos)
                .title(stay.getName())
        );
        // Move camera to accommodation
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(stayPos, 12f));

        // b) Add markers for each activity
        List<TripPlace> activities = trip.getActivities();
        if (activities != null) {
            for (TripPlace act : activities) {
                LatLng actPos = new LatLng(
                        act.getLocation().getLatitude(),
                        act.getLocation().getLongitude()
                );
                googleMap.addMarker(new MarkerOptions()
                        .position(actPos)
                        .title(act.getName())
                );
            }
        }
    }
}
