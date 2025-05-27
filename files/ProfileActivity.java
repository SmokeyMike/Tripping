package com.example.project12;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import com.bumptech.glide.Glide;
import com.example.project12.R;
import com.example.project12.models.Profile;
import com.example.project12.ui.viewmodel.ProfileViewModel;
import com.google.firebase.auth.FirebaseAuth;

public class ProfileActivity extends AppCompatActivity {

    private ProfileViewModel viewModel;
    private ImageView ivPhoto;
    private TextView tvName, tvEmail, tvCurrency;
    private Button btnEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // enable back arrow
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // views
        ivPhoto    = findViewById(R.id.ivProfilePhoto);
        tvName     = findViewById(R.id.tvName);
        tvEmail    = findViewById(R.id.tvEmail);
        tvCurrency = findViewById(R.id.tvCurrency);
        btnEdit    = findViewById(R.id.btnEditProfile);

        // ViewModel
        viewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
        viewModel.getCurrentProfile().observe(this, this::bindProfile);
        viewModel.getErrorMessage().observe(this, err -> {
            if (err != null) Toast.makeText(this, err, Toast.LENGTH_LONG).show();
        });

        // Edit button → show fragment
        btnEdit.setOnClickListener(v -> {
            new EditProfileFragment()
                    .show(getSupportFragmentManager(), "edit_profile");
        });
    }

    private void bindProfile(Profile p) {
        if (p == null) return;
        tvName.setText(p.getName());
        tvEmail.setText(p.getEmail());
        tvCurrency.setText(p.getCurrency());
        String url = p.getPhotoUrl();
        if (url != null && !url.isEmpty()) {
            Glide.with(this).load(url).circleCrop().into(ivPhoto);
        } else {
            ivPhoto.setImageResource(R.drawable.ic_launcher_foreground);
        }
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
            startActivity(new Intent(this, SharedTripsActivity.class));
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
