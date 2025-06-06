package com.example.project12.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project12.R;
import com.example.project12.TripDetailsActivity;
import com.example.project12.models.Trip;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PastTripAdapter extends RecyclerView.Adapter<PastTripAdapter.PastTripViewHolder> {

    private final Context context;
    private final List<Trip> tripList = new ArrayList<>();
    private final SimpleDateFormat dateFormatter =
            new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

    public PastTripAdapter(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public PastTripViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context)
                .inflate(R.layout.item_past_trip, parent, false);
        return new PastTripViewHolder(itemView);
    }

    // PastTripAdapter.java
    @Override
    public void onBindViewHolder(@NonNull PastTripViewHolder holder, int position) {
        Trip trip = tripList.get(position);

        holder.tvTripTitle.setText(trip.getDestination());
        String start = dateFormatter.format(trip.getStartDate());
        String end = dateFormatter.format(trip.getEndDate());
        holder.tvTripDates.setText(start + " – " + end);

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, TripDetailsActivity.class);
            intent.putExtra("trip", trip); // send entire Trip
            context.startActivity(intent);
        });
    }


    @Override
    public int getItemCount() {
        return tripList.size();
    }

    /** Replace current list with new data and refresh */
    public void submitList(List<Trip> newTrips) {
        tripList.clear();
        if (newTrips != null) {
            tripList.addAll(newTrips);
        }
        notifyDataSetChanged();
    }

    static class PastTripViewHolder extends RecyclerView.ViewHolder {
        final TextView tvTripTitle;
        final TextView tvTripDates;

        PastTripViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTripTitle = itemView.findViewById(R.id.tvTripTitle);
            tvTripDates = itemView.findViewById(R.id.tvTripDates);
        }
    }
}
