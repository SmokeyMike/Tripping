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
import java.util.List;
import java.util.Locale;

public class PastTripAdapter extends RecyclerView.Adapter<PastTripAdapter.PastTripViewHolder> {

    private final Context context;
    private final List<Trip> tripList;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

    public PastTripAdapter(Context context, List<Trip> tripList) {
        this.context = context;
        this.tripList = tripList;
    }

    @NonNull
    @Override
    public PastTripViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_past_trip, parent, false);
        return new PastTripViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PastTripViewHolder holder, int position) {
        Trip trip = tripList.get(position);

        // 1) Title = destination
        holder.tvTripTitle.setText(trip.getDestination());

        // 2) Dates = "yyyy-MM-dd – yyyy-MM-dd"
        String start = dateFormat.format(trip.getStartDate());
        String end   = dateFormat.format(trip.getEndDate());
        holder.tvTripDates.setText(start + " – " + end);

        // 3) Click listener: launch TripDetailsActivity with full Trip
        holder.itemView.setOnClickListener(view -> {
            Intent i = new Intent(context, TripDetailsActivity.class);
            i.putExtra("selectedTrip", trip);
            context.startActivity(i);
        });
    }

    @Override
    public int getItemCount() {
        return tripList.size();
    }

    static class PastTripViewHolder extends RecyclerView.ViewHolder {
        TextView tvTripTitle, tvTripDates;
        PastTripViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTripTitle = itemView.findViewById(R.id.tvTripTitle);
            tvTripDates = itemView.findViewById(R.id.tvTripDates);
        }
    }
}
