package com.ayushojha.instaweather;

import android.Manifest;
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
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.ayushojha.instaweather.util.OpenWeatherAPIHandler;
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
    private FusedLocationProviderClient client;
    public final int MY_FINE_LOCATION_REQUEST = 101;
    private LocationCallback locationCallback;
    private LocationRequest locationRequest;
    private RequestQueue queue;
    Intent intent;
    Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        handler = new Handler();
        progressBar = findViewById(R.id.splashProgressBar);
        loadingText = findViewById(R.id.loadingText);

        locationRequest = new LocationRequest();
        locationRequest.setInterval(7500); //should use 15000
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        intent = new Intent(this, MainActivity.class);
        queue = Volley.newRequestQueue(this);

        client = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            client.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null) {
//                        sLat = location.getLatitude();
//                        sLon = location.getLongitude();
                        progressBar.setVisibility(View.VISIBLE);
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
            client.requestLocationUpdates(locationRequest, locationCallback, null);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_FINE_LOCATION_REQUEST);
        }
    }

    private void stopLocationUpdates() {
        client.removeLocationUpdates(locationCallback);
    }

    private void makeRequests() {
        StringRequest currentReq = new StringRequest(OpenWeatherAPIHandler.makeURL(getString(R.string.current_weather_url), String.valueOf(sLat), String.valueOf(sLon)), new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                intent.putExtra("CURRENT_DATA", response);
                Geocoder geocoder = new Geocoder(SplashActivity.this);
                try {
                    List<Address> addressList = geocoder.getFromLocation(sLat, sLon, 1);
//                    Log.d("SPLASH_ADDRESS", addressList.get(0).toString());
                    String sCountry = addressList.get(0).getCountryName();
                    String sState = addressList.get(0).getAdminArea();
                    String sDistrict = addressList.get(0).getSubAdminArea();
                    intent.putExtra("SPLASH_COUNTRY", sCountry);
                    intent.putExtra("SPLASH_STATE", sState);
                    intent.putExtra("SPLASH_DISTRICT", sDistrict);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(SplashActivity.this, "Problem in fetching current weather data", Toast.LENGTH_LONG);
            }
        });
        queue.add(currentReq);


        StringRequest forecastReq = new StringRequest(OpenWeatherAPIHandler.makeURL(getString(R.string.current_weather_url), String.valueOf(sLat), String.valueOf(sLon)), new Response.Listener<String>() {
            @Override
            public void onResponse(final String response) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        intent.putExtra("FORECAST_DATA", response);
                        progressBar.setVisibility(View.GONE);
                        loadingText.setText("Let's Go!");
//                Log.d("SPLASH_LAT", String.valueOf(sLat));
//                Log.d("SPLASH_LON", String.valueOf(sLon));
                        startActivity(intent);
                        finish();
                    }
                });
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(SplashActivity.this, "Problem in fetching weather forecast data", Toast.LENGTH_LONG);
            }
        });
        queue.add(forecastReq);

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

