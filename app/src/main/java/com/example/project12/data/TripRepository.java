package com.example.project12.data;

import androidx.annotation.NonNull;

import com.example.project12.models.Trip;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * Repository for Trip-related Firestore operations.
 */
public class TripRepository {
    private final CollectionReference tripsRef;

    /** Callback for list-of-trips queries */
    public interface TripsCallback {
        void onSuccess(List<Trip> trips);
        void onFailure(Exception e);
    }

    /** Callback for single-trip queries */
    public interface SingleTripCallback {
        void onSuccess(Trip trip);
        void onFailure(Exception e);
    }

    /** Callback for delete/update actions */
    public interface ActionCallback {
        void onSuccess();
        void onFailure(Exception e);
    }

    public TripRepository() {
        tripsRef = FirebaseFirestore.getInstance().collection("trips");  // :contentReference[oaicite:0]{index=0}
    }

    /** Fetch all trips */
    public void getAllTrips(@NonNull TripsCallback callback) {
        tripsRef.get()
                .addOnSuccessListener((QuerySnapshot snap) -> {
                    List<Trip> list = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snap) {
                        try {
                            Trip t = doc.toObject(Trip.class);
                            t.setTripId(doc.getId());
                            list.add(t);
                        } catch (Exception ignored) { }
                    }
                    callback.onSuccess(list);
                })
                .addOnFailureListener(callback::onFailure);
    }

    /** Fetch a single trip by ID */
    public void getTripById(@NonNull String tripId, @NonNull SingleTripCallback callback) {
        tripsRef.document(tripId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        Trip t = doc.toObject(Trip.class);
                        t.setTripId(doc.getId());
                        callback.onSuccess(t);
                    } else {
                        callback.onFailure(new Exception("Trip not found: " + tripId));
                    }
                })
                .addOnFailureListener(callback::onFailure);
    }

    /** Fetch trips owned by a specific user */
    public void getTripsByUser(@NonNull String ownerEmail, @NonNull TripsCallback callback) {
        tripsRef.whereEqualTo("ownerEmail", ownerEmail)
                .get()
                .addOnSuccessListener((QuerySnapshot snap) -> {
                    List<Trip> list = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snap) {
                        try {
                            Trip t = doc.toObject(Trip.class);
                            t.setTripId(doc.getId());
                            list.add(t);
                        } catch (Exception ignored) { }
                    }
                    callback.onSuccess(list);
                })
                .addOnFailureListener(callback::onFailure);
    }

    /** Fetch trips where current user is in the sharedWith list */
    public void getSharedTrips(@NonNull String userEmail, @NonNull TripsCallback callback) {
        tripsRef.whereArrayContains("sharedWith", userEmail)
                .get()
                .addOnSuccessListener((QuerySnapshot snap) -> {
                    List<Trip> list = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snap) {
                        try {
                            Trip t = doc.toObject(Trip.class);
                            t.setTripId(doc.getId());
                            list.add(t);
                        } catch (Exception ignored) { }
                    }
                    callback.onSuccess(list);
                })
                .addOnFailureListener(callback::onFailure);
    }

    /** Save or update a trip */
    public void saveTrip(@NonNull Trip trip, @NonNull TripsCallback callback) {
        if (trip.getTripId() == null || trip.getTripId().isEmpty()) {
            tripsRef.add(trip)
                    .addOnSuccessListener((DocumentReference ref) -> {
                        trip.setTripId(ref.getId());
                        List<Trip> result = new ArrayList<>();
                        result.add(trip);
                        callback.onSuccess(result);
                    })
                    .addOnFailureListener(callback::onFailure);
        } else {
            tripsRef.document(trip.getTripId())
                    .set(trip)
                    .addOnSuccessListener(aVoid -> {
                        List<Trip> result = new ArrayList<>();
                        result.add(trip);
                        callback.onSuccess(result);
                    })
                    .addOnFailureListener(callback::onFailure);
        }
    }

    /** Delete a trip by ID */
    public void deleteTrip(@NonNull String tripId, @NonNull ActionCallback callback) {
        tripsRef.document(tripId)
                .delete()
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(callback::onFailure);
    }
}
