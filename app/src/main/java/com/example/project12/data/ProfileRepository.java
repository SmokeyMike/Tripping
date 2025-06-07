package com.example.project12.data;

import androidx.annotation.NonNull;

import com.example.project12.models.Profile;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * Repository for Profile-related Firestore operations.
 */
public class ProfileRepository {
    private final CollectionReference profiles;

    /** Callback for list-of-profiles queries */
    public interface ProfilesCallback {
        void onSuccess(List<Profile> profiles);
        void onFailure(Exception e);
    }

    /** Callback for single-profile queries */
    public interface SingleProfileCallback {
        void onSuccess(Profile profile);
        void onFailure(Exception e);
    }

    /** Callback for actions like delete */
    public interface ActionCallback {
        void onSuccess();
        void onFailure(Exception e);
    }

    public ProfileRepository() {
        profiles = FirebaseFirestore.getInstance().collection("profiles");
    }

    /** Fetch all profiles */
    public void getAllProfiles(@NonNull ProfilesCallback callback) {
        profiles
                .get()
                .addOnSuccessListener((QuerySnapshot snapshot) -> {
                    List<Profile> list = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snapshot) {
                        try {
                            Profile p = doc.toObject(Profile.class);
                            list.add(p);
                        } catch (Exception ignored) { }
                    }
                    callback.onSuccess(list);
                })
                .addOnFailureListener(callback::onFailure);
    }

    /** Fetch a profile by email (assumes document ID == email) */
    public void getProfileByEmail(@NonNull String email, @NonNull SingleProfileCallback callback) {
        profiles.document(email)
                .get()
                .addOnSuccessListener((DocumentSnapshot doc) -> {
                    if (doc.exists()) {
                        try {
                            Profile p = doc.toObject(Profile.class);
                            callback.onSuccess(p);
                        } catch (Exception e) {
                            callback.onFailure(e);
                        }
                    } else {
                        callback.onFailure(new Exception("Profile not found for: " + email));
                    }
                })
                .addOnFailureListener(callback::onFailure);
    }

    /** Create or update a profile (uses email as document ID) */
    public void saveProfile(@NonNull Profile profile, @NonNull SingleProfileCallback callback) {
        String email = profile.getEmail();
        DocumentReference ref = profiles.document(email);
        ref.set(profile)
                .addOnSuccessListener(aVoid -> callback.onSuccess(profile))
                .addOnFailureListener(callback::onFailure);
    }

    /** Delete a profile by email */
    public void deleteProfile(@NonNull String email, @NonNull ActionCallback callback) {
        profiles.document(email)
                .delete()
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(callback::onFailure);
    }
}
