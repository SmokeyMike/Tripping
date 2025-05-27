package com.example.project12;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project12.R;
import com.example.project12.adapters.PastTripAdapter;
import com.example.project12.models.Trip;
import com.example.project12.ui.viewmodel.TripViewModel;

import java.util.List;

public class PastTripsActivity extends AppCompatActivity {
    private RecyclerView rvPastTrips;
    private PastTripAdapter adapter;
    private TripViewModel viewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_past_trips);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        rvPastTrips = findViewById(R.id.rvPastTrips);
        rvPastTrips.setLayoutManager(new LinearLayoutManager(this));

        adapter = new PastTripAdapter(new PastTripAdapter.OnTripClickListener() {
            @Override
            public void onTripClick(Trip trip) {
                Intent intent = new Intent(PastTripsActivity.this, TripDetailsActivity.class);
                intent.putExtra("tripId", trip.getTripId());
                startActivity(intent);
            }
        });
        rvPastTrips.setAdapter(adapter);

        viewModel = new ViewModelProvider(this).get(TripViewModel.class);
        viewModel.getPastTrips().observe(this, newTrips -> {
            adapter.submitList(newTrips);
        });

        // Trigger data load if needed
        viewModel.loadPastTrips();
    }
}
