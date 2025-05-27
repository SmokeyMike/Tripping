package com.example.project12;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private static final int SPLASH_DURATION = 2000; // 2 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // Your splash screen layout

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                Log.d("Splash", "user="+user);
                if (user != null) {
                    // User already logged in
                    startActivity(new Intent(MainActivity.this, HomeActivity.class));
                } else {
                    // No user logged in
                    startActivity(new Intent(MainActivity.this, AuthActivity.class));
                }
                finish();
            }
        }, SPLASH_DURATION);
    }
}

