// Package declaration (adjust as needed)
package com.example.project12.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project12.R;
import com.example.project12.models.Trip;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

// SharedTripAdapter without owner field (owner inferred from selection context)
public class SharedTripAdapter extends ListAdapter<Trip, SharedTripAdapter.SharedTripViewHolder> {

    public interface OnTripClickListener {
        void onTripClick(Trip trip);
    }

    private final OnTripClickListener listener;
    private static final SimpleDateFormat dateFormat =
            new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());

    public SharedTripAdapter(OnTripClickListener listener) {
        super(new DiffUtil.ItemCallback<Trip>() {
            @Override
            public boolean areItemsTheSame(@NonNull Trip oldItem, @NonNull Trip newItem) {
                return oldItem.getTripId().equals(newItem.getTripId());
            }

            @Override
            public boolean areContentsTheSame(@NonNull Trip oldItem, @NonNull Trip newItem) {
                return oldItem.equals(newItem);
            }
        });
        this.listener = listener;
    }

    @NonNull
    @Override
    public SharedTripViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_shared_trip, parent, false);
        return new SharedTripViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SharedTripViewHolder holder, int position) {
        Trip trip = getItem(position);
        holder.bind(trip);
    }

    class SharedTripViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDates;

        SharedTripViewHolder(View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvSharedTripTitle);
            tvDates = itemView.findViewById(R.id.tvSharedTripDates);
            itemView.setOnClickListener(v -> listener.onTripClick(getItem(getAdapterPosition())));
        }

        void bind(Trip trip) {
            tvTitle.setText(trip.getDestination());
            Date start = trip.getStartDate();
            Date end = trip.getEndDate();
            String dates = dateFormat.format(start) + " – " + dateFormat.format(end);
            tvDates.setText(dates);
        }
    }
}
