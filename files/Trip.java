package com.example.project12.models;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.IgnoreExtraProperties;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import android.os.Parcelable;
import android.os.Parcel;

/**
 * Model representing a Trip.
 */
public class Trip implements Parcelable {
    private String destination;
    private Date startDate;
    private Date endDate;
    private TripPlace stay;
    private List<TripPlace> activities;
    private double totalExpense;
    private List<String> weather;
    private String tripId;
    private String ownerEmail;

    public Trip() { }

    public Trip(String destination, Date startDate, Date endDate,
                TripPlace stay, List<TripPlace> activities,
                double totalExpense, List<String> weather, String tripId, String ownerEmail) {
        this.destination = destination;
        this.startDate = startDate;
        this.endDate = endDate;
        this.stay = stay;
        this.activities = activities;
        this.totalExpense = totalExpense;
        this.weather = weather;
        this.tripId = tripId;
        this.ownerEmail = ownerEmail;
    }

    protected Trip(Parcel in) {
        destination = in.readString();
        long tmpStart = in.readLong();
        startDate = tmpStart != -1 ? new Date(tmpStart) : null;
        long tmpEnd = in.readLong();
        endDate = tmpEnd != -1 ? new Date(tmpEnd) : null;
        stay = in.readParcelable(TripPlace.class.getClassLoader());
        activities = in.createTypedArrayList(TripPlace.CREATOR);
        totalExpense = in.readDouble();
        weather = in.createStringArrayList();
        tripId = in.readString();
        ownerEmail = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(destination);
        dest.writeLong(startDate != null ? startDate.getTime() : -1);
        dest.writeLong(endDate != null ? endDate.getTime() : -1);
        dest.writeParcelable(stay, flags);
        dest.writeTypedList(activities);
        dest.writeDouble(totalExpense);
        dest.writeStringList(weather);
        dest.writeString(tripId);
        dest.writeString(ownerEmail);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Trip> CREATOR = new Creator<Trip>() {
        @Override
        public Trip createFromParcel(Parcel in) {
            return new Trip(in);
        }

        @Override
        public Trip[] newArray(int size) {
            return new Trip[size];
        }
    };

    // Getters & setters omitted for brevity

    // Getters & Setters
    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }

    public Date getStartDate() { return startDate; }
    public void setStartDate(Date startDate) { this.startDate = startDate; }

    public Date getEndDate() { return endDate; }
    public void setEndDate(Date endDate) { this.endDate = endDate; }

    public TripPlace getStay() { return stay; }
    public void setStay(TripPlace stay) { this.stay = stay; }

    public List<TripPlace> getActivities() { return activities; }
    public void setActivities(List<TripPlace> activities) { this.activities = activities; }

    public double getTotalExpense() { return totalExpense; }
    public void setTotalExpense(double totalExpense) { this.totalExpense = totalExpense; }

    public List<String> getWeather() { return weather; }
    public void setWeather(List<String> weather) { this.weather = weather; }

    public String getTripId() { return tripId; }
    public void setTripId(String tripId) { this.tripId = tripId; }

    public GeoPoint getAccommodationLatLng() {return this.stay.getLocation();}

    public String getOwnerEmail() {return ownerEmail;}

    public void setOwnerEmail(String ownerEmail) {this.ownerEmail = ownerEmail;}

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Trip trip = (Trip) o;
        return Double.compare(totalExpense, trip.totalExpense) == 0 && Objects.equals(destination, trip.destination) && Objects.equals(startDate, trip.startDate) && Objects.equals(endDate, trip.endDate) && Objects.equals(stay, trip.stay) && Objects.equals(activities, trip.activities) && Objects.equals(weather, trip.weather) && Objects.equals(tripId, trip.tripId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(destination, startDate, endDate, stay, activities, totalExpense, weather, tripId);
    }
}
