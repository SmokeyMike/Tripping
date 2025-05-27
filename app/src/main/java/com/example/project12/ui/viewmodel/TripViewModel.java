package com.example.project12.ui.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.example.project12.models.Trip;
import com.example.project12.data.TripRepository;

import java.util.List;

public class TripViewModel extends ViewModel {
    private final TripRepository repository;

    private final MutableLiveData<List<Trip>> allTrips       = new MutableLiveData<>();
    private final MutableLiveData<List<Trip>> pastTrips      = new MutableLiveData<>();
    private final MutableLiveData<List<Trip>> sharedTrips    = new MutableLiveData<>();
    private final MutableLiveData<List<Trip>> tripsForProfile = new MutableLiveData<>();

    private final MutableLiveData<Trip> selectedTrip = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    public LiveData<String> getErrorMessage() { return errorMessage; }

    private final String currentUserEmail;

    public TripViewModel() {
        repository = new TripRepository();
        currentUserEmail = FirebaseAuth.getInstance()
                .getCurrentUser()
                .getEmail();
        loadAllTrips();
        loadPastTrips();
    }

    /** All trips **/
    public LiveData<List<Trip>> getAllTrips() { return allTrips; }
    public void loadAllTrips() {
        repository.getAllTrips(new TripRepository.TripsCallback() {
            @Override
            public void onSuccess(List<Trip> trips) {
                allTrips.postValue(trips);
            }
            @Override
            public void onFailure(Exception e) {
                errorMessage.postValue("Error loading all trips: " + e.getMessage());
            }
        });
    }

    /** Past trips (current user) **/
    public LiveData<List<Trip>> getPastTrips() { return pastTrips; }
    public void loadPastTrips() {
        repository.getTripsByUser(currentUserEmail, new TripRepository.TripsCallback() {
            @Override
            public void onSuccess(List<Trip> trips) {
                pastTrips.postValue(trips);
            }
            @Override
            public void onFailure(Exception e) {
                errorMessage.postValue("Error loading your trips: " + e.getMessage());
            }
        });
    }

    /** Shared trips (trips where current user is in sharedWith list) **/
    public LiveData<List<Trip>> getSharedTrips() { return sharedTrips; }
    public void loadSharedTrips() {
        repository.getSharedTrips(currentUserEmail, new TripRepository.TripsCallback() {
            @Override
            public void onSuccess(List<Trip> trips) {
                sharedTrips.postValue(trips);
            }
            @Override
            public void onFailure(Exception e) {
                errorMessage.postValue("Error loading shared trips: " + e.getMessage());
            }
        });
    }

    /** Trips for an arbitrary profile email (used in ProfileActivity) **/
    public LiveData<List<Trip>> getTripsForProfile() { return tripsForProfile; }
    public void loadTripsForProfile(String email) {
        repository.getTripsByUser(email, new TripRepository.TripsCallback() {
            @Override
            public void onSuccess(List<Trip> trips) {
                tripsForProfile.postValue(trips);
            }
            @Override
            public void onFailure(Exception e) {
                errorMessage.postValue("Error loading trips for " + email + ": " + e.getMessage());
            }
        });
    }

    /** Single trip **/
    public LiveData<Trip> getSelectedTrip() { return selectedTrip; }
    public void setSelectedTrip(Trip trip) { selectedTrip.setValue(trip); }
    public void loadTripById(String tripId) {
        repository.getTripById(tripId, new TripRepository.SingleTripCallback() {
            @Override
            public void onSuccess(Trip trip) {
                selectedTrip.postValue(trip);
            }
            @Override
            public void onFailure(Exception e) {
                errorMessage.postValue("Error loading trip: " + e.getMessage());
            }
        });
    }

    /** Create/update **/
    public void saveTrip(Trip trip) {
        if (trip.getOwnerEmail() == null || trip.getOwnerEmail().isEmpty()) {
            trip.setOwnerEmail(currentUserEmail);
        }
        repository.saveTrip(trip, new TripRepository.TripsCallback() {
            @Override
            public void onSuccess(List<Trip> trips) {
                loadPastTrips();
            }
            @Override
            public void onFailure(Exception e) {
                errorMessage.postValue("Error saving trip: " + e.getMessage());
            }
        });
    }

    public void deleteTrip(String tripId) {
        repository.deleteTrip(tripId, new TripRepository.ActionCallback() {
            @Override
            public void onSuccess() {
                loadPastTrips();
            }
            @Override
            public void onFailure(Exception e) {
                errorMessage.postValue("Error deleting trip: " + e.getMessage());
            }
        });
    }

    public void clearErrorMessage() {
        errorMessage.setValue(null);
    }
}
