package com.example.project12.ui.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.project12.data.ProfileRepository;
import com.example.project12.data.TripRepository;
import com.example.project12.models.Profile;
import com.example.project12.models.Trip;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

public class ProfileViewModel extends ViewModel {

    private final ProfileRepository profileRepo = new ProfileRepository();
    private final TripRepository tripRepo     = new TripRepository();

    private final MutableLiveData<List<Profile>> profiles        = new MutableLiveData<>();
    private final MutableLiveData<Profile>       currentProfile  = new MutableLiveData<>();
    private final MutableLiveData<List<Trip>>    tripsForProfile = new MutableLiveData<>();
    private final MutableLiveData<String>        errorMessage    = new MutableLiveData<>();

    public ProfileViewModel() {
        // automatically load current user’s profile
        String email = FirebaseAuth.getInstance()
                .getCurrentUser()
                .getEmail();
        if (email != null) {
            loadCurrentProfile(email);
        } else {
            errorMessage.postValue("No logged-in user");
        }
    }

    /** All profiles **/
    public LiveData<List<Profile>> getProfiles() {
        return profiles;
    }
    public void loadProfiles() {
        profileRepo.getAllProfiles(new ProfileRepository.ProfilesCallback() {
            @Override
            public void onSuccess(List<Profile> list) {
                profiles.postValue(list);
            }
            @Override
            public void onFailure(Exception e) {
                errorMessage.postValue("Error loading profiles: " + e.getMessage());
            }
        });
    }

    /** Save name / currency / photo URL */
    public void saveProfile(Profile profile) {
        profileRepo.saveProfile(profile, new ProfileRepository.SingleProfileCallback() {
            @Override
            public void onSuccess(Profile updated) {
                currentProfile.postValue(updated);
            }
            @Override
            public void onFailure(@NonNull Exception e) {
                errorMessage.postValue("Error saving profile: " + e.getMessage());
            }
        });
    }

    /** Change Firebase password */
    public void changePassword(String newPassword) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            errorMessage.postValue("No user logged in");
            return;
        }
        user.updatePassword(newPassword)
                .addOnSuccessListener(aVoid -> {/* no-op */})
                .addOnFailureListener(e ->
                        errorMessage.postValue("Error changing password: " + e.getMessage())
                );
    }

    /** Current user’s profile **/
    public LiveData<Profile> getCurrentProfile() {
        return currentProfile;
    }
    public void loadCurrentProfile(String email) {
        profileRepo.getProfileByEmail(email, new ProfileRepository.SingleProfileCallback() {
            @Override
            public void onSuccess(Profile profile) {
                currentProfile.postValue(profile);
            }
            @Override
            public void onFailure(Exception e) {
                errorMessage.postValue("Error loading your profile: " + e.getMessage());
            }
        });
    }

    /** Trips for arbitrary profile email **/
    public LiveData<List<Trip>> getTripsForProfile() {
        return tripsForProfile;
    }
    public void loadTripsForProfile(String email) {
        tripRepo.getTripsByUser(email, new TripRepository.TripsCallback() {
            @Override
            public void onSuccess(List<Trip> list) {
                tripsForProfile.postValue(list);
            }
            @Override
            public void onFailure(Exception e) {
                errorMessage.postValue("Error loading trips for " + email + ": " + e.getMessage());
            }
        });
    }

    /** Error/status messages **/
    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }
    public void clearError() {
        errorMessage.setValue(null);
    }
}
