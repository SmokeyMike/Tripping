package com.example.project12;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.project12.models.Profile;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Collections;

public class AuthActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private EditText emailInput, passwordInput;
    private Button btnLogin, btnRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        mAuth = FirebaseAuth.getInstance();
        db    = FirebaseFirestore.getInstance();

        emailInput    = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        btnLogin      = findViewById(R.id.btnLogin);
        btnRegister   = findViewById(R.id.btnRegister);

        // If already logged in, skip auth
        if (mAuth.getCurrentUser() != null) {
            goToHome();
        }

        btnLogin.setOnClickListener(v -> loginUser());
        btnRegister.setOnClickListener(v -> registerUser());
    }

    private void loginUser() {
        String email = emailInput.getText().toString().trim();
        String pwd   = passwordInput.getText().toString().trim();

        if (email.isEmpty() || pwd.isEmpty()) {
            Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, pwd)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show();
                        goToHome();
                    } else {
                        Toast.makeText(this, "Login failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void registerUser() {
        String email = emailInput.getText().toString().trim();
        String pwd   = passwordInput.getText().toString().trim();

        if (email.isEmpty() || pwd.isEmpty()) {
            Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, pwd)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // after registration succeeds…
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                String email = user.getEmail();         // use this as doc ID
                                Profile profile = new Profile(
                                        /* displayName */ "",
                                        /* email */       email,
                                        /* photoUrl */    "",
                                        /* tripsList */   Collections.emptyList(),
                                        /* currency */    "ILS",
                                        /* password*/     pwd
                                );
                                db.collection("profiles")
                                        .document(email)
                                        .set(profile)
                                        .addOnSuccessListener(aVoid -> {
                                            // Profile created
                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(
                                                    AuthActivity.this,
                                                    "Failed to create profile: " + e.getMessage(),
                                                    Toast.LENGTH_LONG
                                            ).show();
                                        });
                            }

                            Toast.makeText(AuthActivity.this,
                                            "Registration successful", Toast.LENGTH_SHORT)
                                    .show();
                            goToHome();
                        } else {
                            Toast.makeText(AuthActivity.this,
                                            "Registration failed", Toast.LENGTH_SHORT)
                                    .show();
                        }
                    }
                });
    }

    private void goToHome() {
        startActivity(new Intent(this, HomeActivity.class));
        finish();
    }
}
