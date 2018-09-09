package com.ayushojha.instaweather.activities;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.ayushojha.instaweather.R;
import com.ayushojha.instaweather.gsonclasses.currentmodels.CurrentWeatherRootGson;
import com.ayushojha.instaweather.util.OpenWeatherAPIHandler;
import com.google.android.gms.awareness.Awareness;
import com.google.android.gms.awareness.snapshot.WeatherResult;
import com.google.android.gms.awareness.state.Weather;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import java.io.IOException;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private String mCurrentResponse;
    private CurrentWeatherRootGson currentGson;
    private Toolbar toolbar;
    private Bundle bundle;
    private ProgressDialog progressDialog;
    private TextView city, temperature, feelTemperature, dewPointText, humidity, pressure, windSpeed, weatherIcon, description, lastUpdate, humidityIcon, pressureIcon, windIcon;
    private String country, state, place, district;
    private SwipeRefreshLayout swipeRefreshLayout;
    double temp, gTemp, feelTemp, dewPoint;
    private Handler handler;
    private RequestQueue queue;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        handler = new Handler();
        queue = Volley.newRequestQueue(this);
        swipeRefreshLayout = findViewById(R.id.swipeLayout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Log.d("SWIPE", "Swipe Done!");
                updateTodayUI();
            }
        });
        houseKeeping();
        createGson(mCurrentResponse);
        updateTodayUI();
    }

    private void createGson(String currentResponse) {
        GsonBuilder gsonBuilder = new GsonBuilder();
        Gson gson = gsonBuilder.create();
        currentGson = gson.fromJson(currentResponse, CurrentWeatherRootGson.class);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        MenuItem searchItem = menu.findItem(R.id.menu_search);
        SearchView searchView = ((SearchView) searchItem.getActionView());
        searchView.setQueryHint("Seach weather for a location..");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(final String query) {
                Log.d("CITY_WEATHER", getString(R.string.current_weather_city_url, query));
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        StringRequest currentReq = new StringRequest(OpenWeatherAPIHandler.makeURL(getString(R.string.current_weather_city_url), query), new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                mCurrentResponse = response;
                                Log.d("Resp", response);
                                createGson(mCurrentResponse);
                                updateTodayUI();
                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Toast.makeText(MainActivity.this, "Problem in fetching weather data", Toast.LENGTH_LONG);
                            }
                        });
                        queue.add(currentReq);
                    }
                });
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_refresh:
                updateTodayUI();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }


    private void updateTodayUI() {
        place = currentGson.getName();
        city.setText(place + ", " + country);
        double owmTemp = currentGson.getMain().getTemp();
        temp = Math.max(gTemp, currentGson.getMain().getTemp());
        temperature.setText(format(temp));
        feelTemperature.setText("Feels like " + format(feelTemp) + "°C");
        dewPointText.setText(format(dewPoint) + "°C");

        pressure.setText(format(currentGson.getMain().getPressure()) + " kPa");
        windSpeed.setText(format(currentGson.getWind().getSpeed()) + " m/s");
        description.setText(StringUtils.capitalize(currentGson.getWeather().get(0).getDescription()));
        humidity.setText(format(currentGson.getMain().getHumidity()) + " %");

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
        format.setMaximumFractionDigits(0);
        return format.format(n);
    }

    private void houseKeeping() {
        city = findViewById(R.id.city);                                            //set all views
        temperature = findViewById(R.id.temperature);
        feelTemperature = findViewById(R.id.feelTemperature);
        dewPointText = findViewById(R.id.dewPoint);
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
        country = bundle.getString("SPLASH_COUNTRY");
        Log.d("SPLASH_COUNTRY", country);

        gTemp = bundle.getFloat("GTEMP");
        feelTemp = bundle.getFloat("GFEEL_TEMP");
        dewPoint = bundle.getFloat("GDEW_POINT");

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString("RES", mCurrentResponse);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        mCurrentResponse = savedInstanceState.getString("RES");
        createGson(mCurrentResponse);
        Log.d("Gson", currentGson.toString());
        updateTodayUI();
        super.onRestoreInstanceState(savedInstanceState);
    }
}



