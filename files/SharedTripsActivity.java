package com.example.project12;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project12.R;
import com.example.project12.adapters.ProfileAdapter;
import com.example.project12.adapters.SharedTripAdapter;
import com.example.project12.models.Profile;
import com.example.project12.models.Trip;
import com.example.project12.ui.viewmodel.ProfileViewModel;
import com.example.project12.ui.viewmodel.TripViewModel;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

public class SharedTripsActivity extends AppCompatActivity {
    private ProfileViewModel profileVM;
    private TripViewModel    tripVM;

    private EditText etEmailSearch;
    private RecyclerView rvProfiles, rvProfileTrips;

    private ProfileAdapter     profileAdapter;
    private SharedTripAdapter  tripAdapter;

    // keep full list for client‐side filtering
    private final List<Profile> allProfiles = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shared_trips);  // :contentReference[oaicite:0]{index=0}

        // Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Views
        etEmailSearch   = findViewById(R.id.etEmailSearch);
        rvProfiles      = findViewById(R.id.rvProfiles);
        rvProfileTrips  = findViewById(R.id.rvProfileTrips);

        // ViewModels
        profileVM = new ViewModelProvider(this).get(ProfileViewModel.class);
        tripVM    = new ViewModelProvider(this).get(TripViewModel.class);

        // Profile RecyclerView
        profileAdapter = new ProfileAdapter(profile -> {
            // on profile tapped: load their trips
            tripVM.loadTripsForProfile(profile.getEmail());
        });  // :contentReference[oaicite:1]{index=1}
        rvProfiles.setLayoutManager(new LinearLayoutManager(this));
        rvProfiles.setAdapter(profileAdapter);

        // Trips RecyclerView
        tripAdapter = new SharedTripAdapter(trip -> {
            // on trip tapped: open details
            Intent i = new Intent(this, TripDetailsActivity.class);
            i.putExtra("tripId", trip.getTripId());
            startActivity(i);
        });  // :contentReference[oaicite:2]{index=2}
        rvProfileTrips.setLayoutManager(new LinearLayoutManager(this));
        rvProfileTrips.setAdapter(tripAdapter);

        // Observe profiles
        profileVM.getProfiles().observe(this, profiles -> {
            allProfiles.clear();
            allProfiles.addAll(profiles);
            filterProfiles(etEmailSearch.getText().toString());
        });
        profileVM.getErrorMessage().observe(this, err -> {
            if (err != null) Toast.makeText(this, err, Toast.LENGTH_LONG).show();
        });

        // Observe trips for selected profile
        tripVM.getTripsForProfile().observe(this, trips -> {
            tripAdapter.submitList(trips);
        });
        tripVM.getErrorMessage().observe(this, err -> {
            if (err != null) Toast.makeText(this, err, Toast.LENGTH_LONG).show();
        });

        // Load all profiles initially
        profileVM.loadProfiles();

        // Filter as user types
        etEmailSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) {
                filterProfiles(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void filterProfiles(String query) {
        if (query.isEmpty()) {
            profileAdapter.submitList(new ArrayList<>(allProfiles));
            return;
        }
        List<Profile> filtered = new ArrayList<>();
        String lower = query.trim().toLowerCase();
        for (Profile p : allProfiles) {
            if (p.getEmail().toLowerCase().contains(lower)) {
                filtered.add(p);
            }
        }
        profileAdapter.submitList(filtered);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
            return true;
        } else if (id == R.id.menu_home) {
            startActivity(new Intent(this, HomeActivity.class));
            return true;
        } else if (id == R.id.menu_shared) {
            // already here
            return true;
        } else if (id == R.id.menu_logout) {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(this, AuthActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
