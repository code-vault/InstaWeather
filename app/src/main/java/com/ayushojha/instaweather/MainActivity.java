package com.ayushojha.instaweather;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.ayushojha.instaweather.gsonclasses.currentmodels.CurrentWeatherRootGson;
import com.ayushojha.instaweather.gsonclasses.forecastmodels.ForecastWeatherRootGson;
import com.ayushojha.instaweather.util.OpenWeatherAPIHandler;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;

public class MainActivity extends AppCompatActivity {
    private Typeface weatherFont;
    private FusedLocationProviderClient client;
    public final int MY_FINE_LOCATION_REQUEST = 101;
    private LocationCallback locationCallback;
    private LocationRequest locationRequest;
    private String mCurrentResponse, mForecastResponse;
    private CurrentWeatherRootGson currentGson;
    private ForecastWeatherRootGson forecastGson;
    private Toolbar toolbar;
    private Bundle bundle;
    private Handler handler;
    private ProgressDialog progressDialog;
    private TextView city, temperature, humidity, pressure, windSpeed, weatherIcon, description, lastUpdate, humidityIcon, pressureIcon, windIcon;
    private double mLat, mLon;
    private String country, state, place, district;
    private RequestQueue queue;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        houseKeeping();
        createGsons(mCurrentResponse, mForecastResponse);
        updateTodayUI();

    }

    private void createGsons(String currentResponse, String forecastResponse) {
        GsonBuilder gsonBuilder = new GsonBuilder();
        Gson gson = gsonBuilder.create();
        currentGson = gson.fromJson(mCurrentResponse, CurrentWeatherRootGson.class);
        forecastGson = gson.fromJson(mForecastResponse, ForecastWeatherRootGson.class);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        MenuItem searchItem = menu.findItem(R.id.menu_search);
        SearchView searchView = ((SearchView) searchItem.getActionView());
        searchView.setQueryHint("Seach weather for a location..");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }


    private void updateTodayUI() {
        place = (district == null) ? currentGson.getName() : district;
        city.setText(place + ", " + country);
        temperature.setText(format(currentGson.getMain().getTemp()) + "°C");
        pressure.setText(format(currentGson.getMain().getPressure()) +"kPa");
        windSpeed.setText(format(currentGson.getWind().getSpeed()) +"m/s");
        description.setText(currentGson.getWeather().get(0).getDescription().toUpperCase());
        humidity.setText(format(currentGson.getMain().getHumidity()) +"%");

        DateTime apiDate = new DateTime(currentGson.getDt() * 1000);
//        Log.d("DATE", apiDate.toString(DateTimeFormat.longDateTime()));

        DateTime sysDate = new DateTime();
        DateTime date = sysDate.getMillis() - apiDate.getMillis() > 300000 ? sysDate : apiDate;
        lastUpdate.setText(date.toString(DateTimeFormat.forPattern("MMM dd, yyyy hh:mm a z")));
        int weatherId = currentGson.getWeather().get(0).getId();
        long sunRise = currentGson.getSys().getSunrise();
        long sunSet = currentGson.getSys().getSunset();
        String icon = "";
        switch (weatherId) {
            case 701:
                weatherIcon.setText(R.string.wi_dust);
                break;
            case 731:
                weatherIcon.setText(R.string.wi_sandstorm);
                break;
            case 751:
                weatherIcon.setText(R.string.wi_sandstorm);
                break;
            case 761:
                weatherIcon.setText(R.string.wi_dust);
                break;
            case 762:
                weatherIcon.setText(R.string.wi_volcano);
                break;
            default:
                icon = OpenWeatherAPIHandler.setWeatherIcon(weatherId, sunRise, sunSet);
                int resId2 = getResources().getIdentifier(icon, "string", getPackageName());
                weatherIcon.setText(getString(resId2));
        }
//        Log.d("ICON", icon);
    }

    public String format(Number n) {
        NumberFormat format = DecimalFormat.getInstance();
        format.setRoundingMode(RoundingMode.FLOOR);
        format.setMinimumFractionDigits(0);
        format.setMaximumFractionDigits(2);
        return format.format(n);
    }

    private void houseKeeping() {
        city = findViewById(R.id.city);                                            //set all views
        temperature = findViewById(R.id.temperature);
        humidity = findViewById(R.id.humidity);
        pressure = findViewById(R.id.pressure);
        description = findViewById(R.id.description);
        windSpeed = findViewById(R.id.windSpeed);
        weatherIcon = findViewById(R.id.weatherIcon);
        humidityIcon = findViewById(R.id.humidityIcon);
        pressureIcon = findViewById(R.id.pressureIcon);
        windIcon = findViewById(R.id.windIcon);
        lastUpdate = findViewById(R.id.lastUpdate);

        Typeface typeface = Typeface.createFromAsset(getAssets(), "fonts/weathericons-regular-webfont.ttf"); //set all icons
        weatherIcon.setTypeface(typeface);
        humidityIcon.setTypeface(typeface);
        pressureIcon.setTypeface(typeface);
        windIcon.setTypeface(typeface);

        toolbar = findViewById(R.id.toolbar);                                    //configure the toolbar
        setSupportActionBar(toolbar);

        progressDialog = new ProgressDialog(this);                        //configure the progress dialog
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Refreshing weather data...");
        progressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#FFD4D9D0")));

        bundle = getIntent().getExtras();                                        //get data from SplashActivity
        mCurrentResponse = bundle.getString("CURRENT_DATA");
        Log.d("CURRENT_DATA", mCurrentResponse);
        mForecastResponse = bundle.getString("FORECAST_DATA");
//        Log.d("FORECAST_DATA", mForecastResponse);
        country = bundle.getString("SPLASH_COUNTRY");
        Log.d("SPLASH_COUNTRY", country);
        state = bundle.getString("SPLASH_STATE");
        Log.d("SPLASH_STATE", state);
        district = bundle.getString("SPLASH_DISTRICT");
        Log.d("SPLASH_DISTRICT", district);
    }

}

