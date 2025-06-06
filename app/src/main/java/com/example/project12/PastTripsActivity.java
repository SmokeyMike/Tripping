package com.example.project12;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.project12.adapters.PastTripAdapter;
import com.example.project12.models.Trip;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

public class PastTripsActivity extends AppCompatActivity {

    private RecyclerView rvPastTrips;
    private PastTripAdapter adapter;
    private final List<Trip> pastTrips = new ArrayList<>();
    private FirebaseFirestore db;
    private String currentUserEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_past_trips);

        // 1) Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        // 2) RecyclerView & Adapter
        rvPastTrips = findViewById(R.id.rvPastTrips);
        rvPastTrips.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PastTripAdapter(this, pastTrips);
        rvPastTrips.setAdapter(adapter);

        // 3) Firestore setup
        db = FirebaseFirestore.getInstance();
        currentUserEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        db.collection("trips")
                .whereEqualTo("userEmail", currentUserEmail)
                .get()
                .addOnSuccessListener(qs -> Log.d("FirestoreTest","Got " + qs.size() + " docs"))
                .addOnFailureListener(e -> Log.e("FirestoreTest","Error", e));

        // 4) Listen for real-time updates under "trips" collection → only this user's trips
        db.collection("trips")
                .whereEqualTo("userEmail", currentUserEmail)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot snapshots,
                                        @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.e("PastTripsActivity", "Error loading past trips", e);
                            Toast.makeText(PastTripsActivity.this,
                                    "Failed to load past trips", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // Clear list and re-populate
                        pastTrips.clear();
                        for (DocumentSnapshot doc : snapshots.getDocuments()) {
                            Trip trip = doc.toObject(Trip.class);
                            if (trip != null) {
                                // Ensure Trip has its Firestore-generated ID if needed
                                trip.setTripId(doc.getId());
                                pastTrips.add(trip);
                            }
                        }
                        adapter.notifyDataSetChanged();
                    }
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate menu if needed (Home, Logout, Profile, etc.)
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // Handle toolbar menu (e.g., Home, Profile, Logout)
        int id = item.getItemId();
        if (id == R.id.menu_home) {
            startActivity(new Intent(this, HomeActivity.class));
            finish();
            return true;
        }
        if (id == R.id.menu_profile) {
            startActivity(new Intent(this, ProfileActivity.class));
            return true;
        }
        if (id == R.id.menu_logout) {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(this, AuthActivity.class));
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
