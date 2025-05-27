package com.example.project12.adapters;

import android.icu.text.SimpleDateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.project12.R;
import com.example.project12.models.Profile;
import com.example.project12.models.Trip;

import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;

public class PastTripAdapter extends ListAdapter<Trip, PastTripAdapter.PastTripViewHolder> {

    public interface OnTripClickListener {
        void onTripClick(Trip trip);
    }

    private final OnTripClickListener listener;
    private static final SimpleDateFormat dateFormat =
            new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());

    public PastTripAdapter(OnTripClickListener listener) {
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
    public PastTripViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_past_trip, parent, false);
        return new PastTripViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PastTripViewHolder holder, int position) {
        Trip trip = getItem(position);
        holder.bind(trip);
    }

    class PastTripViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDates;

        PastTripViewHolder(View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTripTitle);
            tvDates = itemView.findViewById(R.id.tvTripDates);
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

