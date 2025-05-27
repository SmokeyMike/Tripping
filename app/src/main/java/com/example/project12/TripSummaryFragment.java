package com.example.project12;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.project12.models.Trip;
import com.example.project12.models.TripPlace;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class TripSummaryFragment extends Fragment {
    private static final String ARG_TRIP = "trip";
    private Trip trip;

    public static TripSummaryFragment newInstance(Trip trip) {
        TripSummaryFragment fragment = new TripSummaryFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_TRIP, trip);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            trip = getArguments().getParcelable(ARG_TRIP);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_trip_summary,
                container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (trip == null) return;

        TextView tvDestination = view.findViewById(R.id.tvDestination);
        TextView tvDates       = view.findViewById(R.id.tvDates);
        TextView tvAccommodation= view.findViewById(R.id.tvAccommodation);
        LinearLayout llActivities = view.findViewById(R.id.llActivities);
        TextView tvTotalExpense = view.findViewById(R.id.tvTotalExpense);

        tvDestination.setText(trip.getDestination());

        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        String dates = fmt.format(trip.getStartDate()) + " – " + fmt.format(trip.getEndDate());
        tvDates.setText(dates);

        TripPlace stay = trip.getStay();
        String accText = stay.getName() + " (" + stay.getType() + ") – "
                + stay.getPrice();
        tvAccommodation.setText(accText);

        // Inflate each activity
        for (TripPlace act : trip.getActivities()) {
            View item = LayoutInflater.from(
                    llActivities.getContext()
            ).inflate(R.layout.item_activity_summary,
                    llActivities, false);
            TextView name = item.findViewById(R.id.tvActName);
            TextView cost = item.findViewById(R.id.tvActCost);
            TextView date = item.findViewById(R.id.tvActDate);

            name.setText(act.getName());
            cost.setText(String.valueOf(act.getPrice()));
            date.setText(fmt.format(act.getDate()));

            llActivities.addView(item);
        }

        tvTotalExpense.setText(String.valueOf(trip.getTotalExpense()));
    }
}
