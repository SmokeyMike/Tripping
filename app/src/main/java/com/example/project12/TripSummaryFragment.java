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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
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
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(
                R.layout.fragment_trip_summary,
                container, false);
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (trip == null) return;

        // Reference views
        TextView tvDestination    = view.findViewById(R.id.tvDestination);
        TextView tvDates          = view.findViewById(R.id.tvDates);
        TextView tvAccommodation  = view.findViewById(R.id.tvAccommodation);
        LinearLayout llWeather    = view.findViewById(R.id.llWeather);
        LinearLayout llActivities = view.findViewById(R.id.llActivities);
        TextView tvTotalExpense   = view.findViewById(R.id.tvTotalExpense);

        // 1) Destination
        tvDestination.setText(trip.getDestination());

        // 2) Dates (formatted "yyyy-MM-dd")
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        String dates = fmt.format(trip.getStartDate())
                + " – "
                + fmt.format(trip.getEndDate());
        tvDates.setText(dates);

        // 3) Accommodation: "Name (type) – price"
        TripPlace stay = trip.getStay();
        String accText = stay.getName()
                + " (" + stay.getType() + ") – ₪"
                + String.format(Locale.US, "%.2f", stay.getPrice());
        tvAccommodation.setText(accText);

        // 4) Weather Forecasts
        //    Each entry in trip.getWeather() is a String like "2025-06-10: 18–24°C, Clear"
        List<String> weatherList = trip.getWeather();
        if (weatherList != null && !weatherList.isEmpty()) {
            for (String w : weatherList) {
                TextView tvW = new TextView(llWeather.getContext());
                tvW.setLayoutParams(new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT));
                tvW.setText(w);
                tvW.setPadding(0, 4, 0, 4);
                llWeather.addView(tvW);
            }
        } else {
            // If no weather was fetched, show a placeholder
            TextView tvNoWeather = new TextView(llWeather.getContext());
            tvNoWeather.setLayoutParams(new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            tvNoWeather.setText("No forecast available");
            tvNoWeather.setPadding(0, 4, 0, 4);
            llWeather.addView(tvNoWeather);
        }

        // 5) Activities: sort by date, then inflate each
        List<TripPlace> activities = trip.getActivities();
        if (activities != null && !activities.isEmpty()) {
            Collections.sort(activities, new Comparator<TripPlace>() {
                @Override
                public int compare(TripPlace a, TripPlace b) {
                    if (a.getDate() == null || b.getDate() == null) return 0;
                    return a.getDate().compareTo(b.getDate());
                }
            });
            for (TripPlace act : activities) {
                View item = LayoutInflater.from(llActivities.getContext())
                        .inflate(R.layout.item_activity_summary, llActivities, false);
                TextView name = item.findViewById(R.id.tvActName);
                TextView cost = item.findViewById(R.id.tvActCost);
                TextView date = item.findViewById(R.id.tvActDate);

                name.setText(act.getName());
                cost.setText("₪" + String.format(Locale.US, "%.2f", act.getPrice()));
                date.setText(fmt.format(act.getDate()));

                llActivities.addView(item);
            }
        } else {
            TextView tvNoActs = new TextView(llActivities.getContext());
            tvNoActs.setLayoutParams(new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            tvNoActs.setText("No activities added");
            tvNoActs.setPadding(0, 4, 0, 4);
            llActivities.addView(tvNoActs);
        }

        // 6) Total Expense
        tvTotalExpense.setText("₪" + String.format(Locale.US, "%.2f", trip.getTotalExpense()));
    }
}
