package com.example.project12;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project12.adapters.PastTripAdapter;
import com.example.project12.ui.viewmodel.TripViewModel;

public class PastTripsActivity extends AppCompatActivity {

    private TripViewModel tripViewModel;
    private PastTripAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_past_trips);

        // ====== Toolbar setup ======
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Past Trips");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            // Pressing back arrow just finishes this activity:
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        // ====== RecyclerView & Adapter ======
        RecyclerView rvPastTrips = findViewById(R.id.rvPastTrips);
        rvPastTrips.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PastTripAdapter(this);
        rvPastTrips.setAdapter(adapter);

        // ====== ViewModel & LiveData ======
        tripViewModel = new ViewModelProvider(this).get(TripViewModel.class);
        tripViewModel.getPastTrips().observe(this, trips -> {
            // Whenever the list of past trips changes, submit to adapter
            adapter.submitList(trips);
        });

        // Trigger initial load of past trips
        tripViewModel.loadPastTrips();
    }
}
