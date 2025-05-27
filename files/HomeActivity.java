package com.example.project12;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;

public class HomeActivity extends AppCompatActivity {
    private Button btnPlanNewTrip;
    private Button btnViewMyTrips;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Log.d("Home", "onCreate()");

        // 1) Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // 2) Buttons
        btnPlanNewTrip = findViewById(R.id.btnPlanNewTrip);
        btnViewMyTrips = findViewById(R.id.btnViewMyTrips);

        btnPlanNewTrip.setOnClickListener(v -> {
            startActivity(new Intent(HomeActivity.this, TripPlanningActivity.class));
        });

        btnViewMyTrips.setOnClickListener(v -> {
            startActivity(new Intent(HomeActivity.this, PastTripsActivity.class));
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate our shared toolbar menu
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.menu_home) {
            // Already on Home
            return true;
        } else if (id == R.id.menu_shared) {
            startActivity(new Intent(this, SharedTripsActivity.class));
            return true;
        } else if (id == R.id.menu_profile) {
            startActivity(new Intent(this, ProfileActivity.class));
            return true;
        } else if (id == R.id.menu_logout) {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(this, AuthActivity.class));
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
