package com.example.project12.models;

import com.google.firebase.firestore.IgnoreExtraProperties;
import java.util.List;
import java.util.Objects;

@IgnoreExtraProperties
public class Profile {
    private String name;
    private String email;
    private String photoUrl;       // URI to avatar
    private List<Trip> trips;      // optional if you embed trips
    private String currency;       // e.g. "ILS", "USD"
    private String password;       // only if you need to store it (not recommended)

    // Required empty constructor
    public Profile() {}

    public Profile(String name, String email, String photoUrl,
                   List<Trip> trips, String currency, String password) {
        this.name = name;
        this.email = email;
        this.photoUrl = photoUrl;
        this.trips = trips;
        this.currency = currency;
        this.password = password;
    }

    // Getters & Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhotoUrl() { return photoUrl; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }

    public List<Trip> getTrips() { return trips; }
    public void setTrips(List<Trip> trips) { this.trips = trips; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Profile profile = (Profile) o;
        return Objects.equals(name, profile.name) && Objects.equals(email, profile.email) && Objects.equals(photoUrl, profile.photoUrl) && Objects.equals(trips, profile.trips) && Objects.equals(currency, profile.currency) && Objects.equals(password, profile.password);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, email, photoUrl, trips, currency, password);
    }
}
