package com.example.project12;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;

import com.example.project12.R;
import com.example.project12.models.Trip;
import com.example.project12.models.TripPlace;
import com.example.project12.ui.viewmodel.TripViewModel;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.firebase.firestore.GeoPoint;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.lang.ref.WeakReference;
import java.text.ParseException;

/**
 * TripPlanningActivity.java
 *
 * - Adds weather‐fetching logic (One Call API) inline (no extra model/service files).
 * - Uses a static AsyncTask with WeakReference to avoid memory leaks.
 * - After user selects destination & dates, it fetches daily forecasts and attaches them to the Trip.
 */
public class TripPlanningActivity extends AppCompatActivity {
    // UI fragments
    private AutocompleteSupportFragment destFrag, stayFrag, activityFrag;

    // Inputs
    private EditText etStart, etEnd, etAccommodationCost, etCurrentActivity, etActivityCost;
    private Spinner spDaySelector;
    private Button btnAddActivity, btnGoToDetails;

    // State
    private String destinationName;
    private GeoPoint destinationGeo;         // for Trip.destination → GeoPoint
    private TripPlace stayPlace;              // holds accommodation
    private Place pendingActivityPlace;       // chosen via activityFrag
    private Date startDate, endDate;
    private final List<TripPlace> activities = new ArrayList<>();
    private final List<String> dayLabels = new ArrayList<>();
    private ArrayAdapter<String> dayAdapter;

    // Helpers
    private final Calendar cal = Calendar.getInstance();
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    // ViewModel
    private TripViewModel tripVM;

    //––— INNER CLASS FOR DAILY FORECASTS —–––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––

    /**
     * Minimal container for a single day's forecast.
     * We only store dateString ("yyyy-MM-dd"), minTemp, maxTemp, and condition ("Rain", "Clear", etc.).
     */
    private static class DayForecast {
        String dateString;
        double minTemp, maxTemp;
        String condition;

        DayForecast(String dateString, double minTemp, double maxTemp, String condition) {
            this.dateString = dateString;
            this.minTemp = minTemp;
            this.maxTemp = maxTemp;
            this.condition = condition;
        }
    }

    //––— STATIC ASYNCTASK TO FETCH WEATHER —–––––––––––––––––––––––––––––––––––––––––––––––––––––––––

    /**
     * FetchWeatherTask runs off the UI thread, calls OpenWeatherMap One Call API,
     * parses the "daily" array, filters for dates between startDateStr and endDateStr,
     * and returns a List<DayForecast>.
     */
    private static class FetchWeatherTask extends AsyncTask<Void, Void, List<DayForecast>> {
        private final WeakReference<TripPlanningActivity> activityRef;
        private final double latitude, longitude;
        private final String startDateStr, endDateStr;

        FetchWeatherTask(
                TripPlanningActivity activity,
                double latitude,
                double longitude,
                String startDateStr,
                String endDateStr
        ) {
            this.activityRef = new WeakReference<>(activity);
            this.latitude = latitude;
            this.longitude = longitude;
            this.startDateStr = startDateStr;
            this.endDateStr = endDateStr;
        }

        @Override
        protected List<DayForecast> doInBackground(Void... voids) {
            List<DayForecast> resultList = new ArrayList<>();
            HttpURLConnection connection = null;
            BufferedReader reader = null;

            try {
                TripPlanningActivity activity = activityRef.get();
                if (activity == null || activity.isFinishing()) {
                    return resultList;
                }

                // 1) Build URL: One Call, exclude current,minutely,hourly,alerts
                String apiKey = activity.getString(R.string.openweather_api_key);
                String urlString = String.format(
                        Locale.US,
                        "https://api.openweathermap.org/data/3.0/onecall" +
                                "?lat=%f&lon=%f" +
                                "&exclude=current,minutely,hourly,alerts" +
                                "&units=metric&appid=%s",
                        latitude, longitude, apiKey
                );
                Log.d("WeatherFetch", "URL = " + urlString);
                URL url = new URL(urlString);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(10_000);
                connection.setReadTimeout(10_000);
                connection.connect();
                int code = connection.getResponseCode();
                Log.d("WeatherFetch", "HTTP response code: " + code);
                if (code != HttpURLConnection.HTTP_OK) {
                    // Read the error body (if any) and log it
                    InputStream err = connection.getErrorStream();
                    if (err != null) {
                        BufferedReader errReader = new BufferedReader(new InputStreamReader(err));
                        StringBuilder sb = new StringBuilder();
                        String line;
                        while ((line = errReader.readLine()) != null) {
                            sb.append(line);
                        }
                        Log.e("WeatherFetch", "Error response: " + sb.toString());
                    }
                    return resultList;
                }
                // Only now call getInputStream()
                InputStream is = connection.getInputStream();
                reader = new BufferedReader(new InputStreamReader(is));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                String jsonString = sb.toString();

                // 2) Parse top‐level JSON and get "daily" array
                JsonObject rootObj = JsonParser.parseString(jsonString).getAsJsonObject();
                JsonArray dailyArray = rootObj.getAsJsonArray("daily");

                // 3) Prepare date parsing
                SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                Date startDate = isoFormat.parse(startDateStr);
                Date endDate = isoFormat.parse(endDateStr);

                // 4) Loop each element in dailyArray
                for (JsonElement element : dailyArray) {
                    JsonObject dayObj = element.getAsJsonObject();

                    // dt = UNIX seconds timestamp (UTC 00:00 of that day)
                    long dtSeconds = dayObj.get("dt").getAsLong();
                    Date forecastDate = new Date(dtSeconds * 1000L);
                    String forecastDateString = isoFormat.format(forecastDate);

                    // Only keep if between startDate…endDate (inclusive)
                    if (!forecastDate.before(startDate) && !forecastDate.after(endDate)) {
                        JsonObject tempObj = dayObj.getAsJsonObject("temp");
                        double minT = tempObj.get("min").getAsDouble();
                        double maxT = tempObj.get("max").getAsDouble();

                        // weather[0].main (e.g., "Rain", "Clear")
                        JsonArray weatherArr = dayObj.getAsJsonArray("weather");
                        String condition = "Unknown";
                        if (weatherArr.size() > 0) {
                            JsonObject weather0 = weatherArr.get(0).getAsJsonObject();
                            condition = weather0.get("main").getAsString();
                        }

                        DayForecast df = new DayForecast(forecastDateString, minT, maxT, condition);
                        resultList.add(df);
                    }
                }
            } catch (Exception e) {
                Log.e("WeatherFetch", "Error fetching/parsing weather", e);
            } finally {
                if (reader != null) {
                    try { reader.close(); } catch (IOException ignored) {}
                }
                if (connection != null) {
                    connection.disconnect();
                }
            }
            return resultList;
        }

        @Override
        protected void onPostExecute(List<DayForecast> forecasts) {
            TripPlanningActivity activity = activityRef.get();
            if (activity == null || activity.isFinishing()) return;

            // Build a List<String> instead of List<HashMap<…>>
            List<String> weatherStrings = new ArrayList<>();
            for (DayForecast df : forecasts) {
                String s = df.dateString
                        + ": "
                        + df.minTemp + "–" + df.maxTemp + "°C, "
                        + df.condition;
                weatherStrings.add(s);
            }

            // Now this matches Trip.setWeather(List<String>)
            Trip trip = activity.currentTrip;
            trip.setWeather(weatherStrings);

            Intent i = new Intent(activity, TripDetailsActivity.class);
            i.putExtra("trip", trip);
            activity.startActivity(i);
            activity.finish();
        }

    }

    // Reference to the Trip under construction (so we can set weather before saving)
    private Trip currentTrip;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_planning);

        // Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Plan New Trip");
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        // ViewModel
        tripVM = new ViewModelProvider(this).get(TripViewModel.class);

        // Initialize Places SDK if needed
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(),
                    getString(R.string.places_api_key),
                    Locale.getDefault());
        }

        // Find views
        etStart             = findViewById(R.id.etStartDate);
        etEnd               = findViewById(R.id.etEndDate);
        etAccommodationCost = findViewById(R.id.etAccommodationCost);
        etCurrentActivity   = findViewById(R.id.etCurrentActivity);
        etActivityCost      = findViewById(R.id.etActivityCost);
        spDaySelector       = findViewById(R.id.spDaySelector);
        btnAddActivity      = findViewById(R.id.btnAddActivity);
        btnGoToDetails      = findViewById(R.id.btnGoToDetails);

        // Spinner setup
        dayAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, dayLabels);
        dayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spDaySelector.setAdapter(dayAdapter);

        // Date pickers
        etStart.setOnClickListener(v -> pickDate(date -> {
            startDate = date;
            etStart.setText(sdf.format(date));
            refreshDays();
        }));
        etEnd.setOnClickListener(v -> pickDate(date -> {
            endDate = date;
            etEnd.setText(sdf.format(date));
            refreshDays();
        }));

        // Destination autocomplete
        destFrag = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.autocomplete_destination);
        destFrag.setPlaceFields(List.of(Place.Field.NAME, Place.Field.LAT_LNG));
        destFrag.setCountry("IL");
        destFrag.setHint("Destination");
        destFrag.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place p) {
                destinationName = p.getName();
                LatLng ll = p.getLatLng();
                destinationGeo = new GeoPoint(ll.latitude, ll.longitude);
            }
            @Override public void onError(@NonNull com.google.android.gms.common.api.Status s) { }
        });

        // Accommodation autocomplete
        stayFrag = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.autocomplete_accommodation);
        stayFrag.setPlaceFields(List.of(Place.Field.NAME, Place.Field.LAT_LNG));
        stayFrag.setCountry("IL");
        stayFrag.setHint("Accommodation");
        stayFrag.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place p) {
                double cost = parseDouble(etAccommodationCost.getText().toString(), 0);
                LatLng ll = p.getLatLng();
                stayPlace = new TripPlace(
                        p.getName(),
                        "accommodation",
                        new GeoPoint(ll.latitude, ll.longitude),
                        cost,
                        null,          // no date needed for stay
                        p.getId()
                );
            }
            @Override public void onError(@NonNull com.google.android.gms.common.api.Status s) { }
        });

        // Activity autocomplete
        activityFrag = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.autocomplete_activity);
        activityFrag.setPlaceFields(List.of(Place.Field.NAME, Place.Field.LAT_LNG));
        activityFrag.setCountry("IL");
        activityFrag.setHint("Activity");
        activityFrag.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place p) {
                pendingActivityPlace = p;
                etCurrentActivity.setText(p.getName());
            }
            @Override public void onError(@NonNull com.google.android.gms.common.api.Status s) { }
        });

        // Add activity button
        btnAddActivity.setOnClickListener(v -> {
            if (pendingActivityPlace == null || startDate == null) return;
            int dayIdx = spDaySelector.getSelectedItemPosition();
            Date activityDate = getDateForDay(dayIdx);
            double cost = parseDouble(etActivityCost.getText().toString(), 0);
            LatLng ll = pendingActivityPlace.getLatLng();
            TripPlace tp = new TripPlace(
                    pendingActivityPlace.getName(),
                    "activity",
                    new GeoPoint(ll.latitude, ll.longitude),
                    cost,
                    activityDate,
                    pendingActivityPlace.getId()
            );
            activities.add(tp);
            etCurrentActivity.setText("");
            etActivityCost.setText("");
            Toast.makeText(this, "Activity added to day", Toast.LENGTH_SHORT).show();
        });

        // Summary button → build Trip, fetch weather, then save & launch details
        btnGoToDetails.setOnClickListener(v -> {
            if (destinationName == null || startDate == null ||
                    endDate == null || stayPlace == null) {
                Toast.makeText(this, "Fill all required fields", Toast.LENGTH_SHORT).show();
                return;
            }

            stayPlace.setPrice(parseDouble(etAccommodationCost.getText().toString(), 0));

            // 1) Compute total expense so far (accommodation + activities)
            double total = stayPlace.getPrice();
            for (TripPlace a : activities) total += a.getPrice();

            // 2) Build a Trip object with empty weather list
            String userEmail =
                    com.google.firebase.auth.FirebaseAuth.getInstance()
                            .getCurrentUser().getEmail();
            Trip trip = new Trip(
                    destinationName,
                    startDate,
                    endDate,
                    stayPlace,
                    activities,
                    total,
                    new ArrayList<>(),   // empty weather for now
                    "",                  // blank tripId (will be set by Firestore)
                    userEmail
            );
            currentTrip = trip;   // store in field so AsyncTask can update it later

            // 3) Convert start/end Date → "yyyy-MM-dd" strings
            String startDateString = sdf.format(startDate);
            String endDateString   = sdf.format(endDate);

            // 4) Extract lat/lon from destinationGeo
            double lat = destinationGeo.getLatitude();
            double lon = destinationGeo.getLongitude();

            // 5) Launch AsyncTask to fetch weather for every day in [startDate, endDate]
            new FetchWeatherTask(
                    TripPlanningActivity.this,
                    lat, lon,
                    startDateString,
                    endDateString
            ).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            // *Note*: onPostExecute() will save the Trip and launch details
        });

        // VM error observer
        tripVM.getErrorMessage().observe(this, err -> {
            if (err != null) Toast.makeText(this, err, Toast.LENGTH_LONG).show();
        });
    }

    //––— DATE PICKER & UTILS —––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––

    private void pickDate(DateCallback cb) {
        Calendar now = Calendar.getInstance();
        new DatePickerDialog(this, (DatePicker dp, int y, int m, int d) -> {
            Calendar sel = Calendar.getInstance();
            sel.set(y, m, d);
            cb.onChosen(sel.getTime());
        }, now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH))
                .show();
    }

    private void refreshDays() {
        dayLabels.clear();
        if (startDate != null && endDate != null && !endDate.before(startDate)) {
            Calendar c = Calendar.getInstance();
            c.setTime(startDate);
            int idx = 1;
            while (!c.getTime().after(endDate)) {
                dayLabels.add("Day " + idx++ + ": " + sdf.format(c.getTime()));
                c.add(Calendar.DATE, 1);
            }
        }
        dayAdapter.notifyDataSetChanged();
    }

    private Date getDateForDay(int index) {
        Calendar c = Calendar.getInstance();
        c.setTime(startDate);
        c.add(Calendar.DATE, index);
        return c.getTime();
    }

    private double parseDouble(String s, double def) {
        try { return Double.parseDouble(s); }
        catch (Exception ignored) { return def; }
    }

    private interface DateCallback { void onChosen(Date date); }
}
