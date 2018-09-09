package com.ayushojha.instaweather.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.ayushojha.instaweather.R;
import com.ayushojha.instaweather.util.OpenWeatherAPIHandler;
import com.google.android.gms.awareness.Awareness;
import com.google.android.gms.awareness.snapshot.WeatherResult;
import com.google.android.gms.awareness.state.Weather;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.IOException;
import java.util.List;

public class SplashActivity extends AppCompatActivity {
    private double sLat = 28.7041, sLon = 77.1025;
    private ProgressBar progressBar; //loading circle
    private TextView loadingText;
    private FusedLocationProviderClient locationProviderClient;
    public final int MY_FINE_LOCATION_REQUEST = 101;
    private LocationCallback locationCallback;
    private LocationRequest locationRequest;
    private RequestQueue queue;
    private Weather weather;
    private  Intent intent;
    private  Handler handler;
    private  GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        client = new GoogleApiClient.Builder(this)
                .addApi(Awareness.API)
                .build();
        client.connect();



        handler = new Handler();
        progressBar = findViewById(R.id.splashProgressBar);
        loadingText = findViewById(R.id.loadingText);

        locationRequest = new LocationRequest();
        locationRequest.setInterval(7500); //should use 15000
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        intent = new Intent(this, MainActivity.class);
        queue = Volley.newRequestQueue(this);

        locationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            progressBar.setVisibility(View.VISIBLE);

            locationProviderClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null) {
                        sLat = location.getLatitude();
                        sLon = location.getLongitude();
                        makeRequests();
                    }
                }
            });
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_FINE_LOCATION_REQUEST);
        }

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                for (Location location : locationResult.getLocations()) {
                    if (location != null) {
//                        sLat = location.getLatitude();
//                        sLon = location.getLongitude();
                    }
                }
            }
        };
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    @Override
    protected void onResume() {
        super.onResume();
        startLocationUpdates();
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_FINE_LOCATION_REQUEST);
        }
    }

    private void stopLocationUpdates() {
        locationProviderClient.removeLocationUpdates(locationCallback);
    }

    private void makeRequests() {
        handler.post(new Runnable() {
            @SuppressLint("MissingPermission")
            @Override
            public void run() {
                StringRequest currentReq = new StringRequest(OpenWeatherAPIHandler.makeURL(getString(R.string.current_weather_coordinate_url), String.valueOf(sLat), String.valueOf(sLon)), new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        intent.putExtra("CURRENT_DATA", response);
                        intent.putExtra("SLAT", sLat);
                        intent.putExtra("SLON", sLon);
                        Geocoder geocoder = new Geocoder(SplashActivity.this);
                        try {
                            List<Address> addressList = geocoder.getFromLocation(sLat, sLon, 1);
                            Log.d("SPLASH_ADDRESS", addressList.get(0).toString());
                            String sCountry = addressList.get(0).getCountryName();
                            String sState = addressList.get(0).getAdminArea();
                            String sDistrict = addressList.get(0).getSubAdminArea();
                            intent.putExtra("SPLASH_COUNTRY", sCountry);
                            intent.putExtra("SPLASH_STATE", sState);
                            intent.putExtra("SPLASH_DISTRICT", sDistrict);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        Awareness.SnapshotApi.getWeather(client)
                                .setResultCallback(new ResultCallback<WeatherResult>() {
                                    @Override
                                    public void onResult(@NonNull WeatherResult weatherResult) {
                                        if (!weatherResult.getStatus().isSuccess()) {
                                            Log.d("ERROR", "Could not get weather.");
                                            return;
                                        }
                                        weather = weatherResult.getWeather();
                                        intent.putExtra("GTEMP", weather.getTemperature(Weather.CELSIUS));
                                        intent.putExtra("GFEEL_TEMP", weather.getFeelsLikeTemperature(Weather.CELSIUS));
                                        intent.putExtra("GDEW_POINT", weather.getDewPoint(Weather.CELSIUS));
                                        Log.d("GTEMP",  weather.toString());
                                        progressBar.setVisibility(View.GONE);
                                        loadingText.setText("Let's Go!");
                                        startActivity(intent);
                                        finish();
                                    }
                                });

                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(SplashActivity.this, "Problem in fetching current weather data", Toast.LENGTH_LONG);
                    }
                });
                queue.add(currentReq);

            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case MY_FINE_LOCATION_REQUEST:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //permission granted
                } else {
                    Toast.makeText(getApplicationContext(), "This app needs location permisson to work.", Toast.LENGTH_LONG).show();
                    finish();
                }
                break;
        }
    }

}

