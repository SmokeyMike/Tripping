package com.example.project12.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.firestore.GeoPoint;
import java.util.Date;

/**
 * Model representing a Place used in a trip (accommodation or activity).
 */
public class TripPlace implements Parcelable {
    private String name;
    private String type;      // "accommodation" or "activity"
    private GeoPoint location;
    private double price;
    private Date date;
    private String placeId;

    // Empty constructor for Firestore / serialization
    public TripPlace() { }

    public TripPlace(String name, String type, GeoPoint location,
                     double price, Date date, String placeId) {
        this.name = name;
        this.type = type;
        this.location = location;
        this.price = price;
        this.date = date;
        this.placeId = placeId;
    }

    // <-- no @Override here
    protected TripPlace(Parcel in) {
        name    = in.readString();
        type    = in.readString();
        double lat = in.readDouble();
        double lng = in.readDouble();
        location = new GeoPoint(lat, lng);
        price   = in.readDouble();
        long tmpDate = in.readLong();
        date    = tmpDate == -1 ? null : new Date(tmpDate);
        placeId = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(type);
        dest.writeDouble(location.getLatitude());
        dest.writeDouble(location.getLongitude());
        dest.writeDouble(price);
        dest.writeLong(date != null ? date.getTime() : -1);
        dest.writeString(placeId);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<TripPlace> CREATOR =
            new Parcelable.Creator<TripPlace>() {
                @Override
                public TripPlace createFromParcel(Parcel in) {
                    return new TripPlace(in);
                }

                @Override
                public TripPlace[] newArray(int size) {
                    return new TripPlace[size];
                }
            };

// Getters & Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public GeoPoint getLocation() { return location; }
    public void setLocation(GeoPoint location) { this.location = location; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public Date getDate() { return date; }
    public void setDate(Date date) { this.date = date; }

    public String getPlaceId() { return placeId; }
    public void setPlaceId(String placeId) { this.placeId = placeId; }
}
