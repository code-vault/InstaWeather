package com.ayushojha.instaweather;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ayushojha.instaweather.gsonclasses.OpenWeatherJSONResponse;
import com.ayushojha.instaweather.util.OpenWeatherAPIHandler;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class MainActivity extends AppCompatActivity {
    TextView textCity, textTime, textIcon, textTemp, textWeatherDesc, textHumidity, textWindSpeed, textPressure;
    ImageView imgIcon;
    double lat, lon;
    Typeface weatherFont;

    FusedLocationProviderClient client;
    static final int MY_FINE_LOCATION_REQUEST = 101;
    LocationRequest locationRequest;
    LocationCallback locationCallback;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_main);

        weatherFont = Typeface.createFromAsset(getApplicationContext().getAssets(), "fonts/weathericons-regular-webfont.ttf");

        textCity = findViewById(R.id.textCity);
        textTime = findViewById(R.id.textTime);
//        textIcon = findViewById(R.id.textIcon);
        textTemp = findViewById(R.id.textTemp);
        textWeatherDesc = findViewById(R.id.textWeatherDesc);
        textWindSpeed = findViewById(R.id.textWindSpeed);
        textPressure = findViewById(R.id.textPresure);
        textHumidity = findViewById(R.id.textHumidity);
//        textIcon = findViewById(R.id.textIcon);
//        textIcon.setTypeface(weatherFont);
        imgIcon = findViewById(R.id.imgIcon);

        locationRequest = new LocationRequest();
        locationRequest.setInterval(7500); //should use 15000
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        client = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            client.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null) {
                        lat = location.getLatitude();
                        lon = location.getLongitude();
                        new GetWeather().execute(OpenWeatherAPIHandler.getAPIRequest(String.valueOf(lat), String.valueOf(lon)));
                        Log.d("APIRequest", OpenWeatherAPIHandler.getAPIRequest(String.valueOf(lat), String.valueOf(lon)));
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
                    //update the user interface with user data
                    if (location != null) {
                        lat = location.getLatitude();
                        lon = location.getLongitude();
                        new GetWeather().execute(OpenWeatherAPIHandler.getAPIRequest(String.valueOf(lat), String.valueOf(lon)));
                        Log.d("APIRequest", OpenWeatherAPIHandler.getAPIRequest(String.valueOf(lat), String.valueOf(lon)));
                    }
                }
            }
        };
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


    @Override
    protected void onResume() {
        super.onResume();
        startLocationUpdates();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    private void stopLocationUpdates() {
        client.removeLocationUpdates(locationCallback);
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            client.requestLocationUpdates(locationRequest, locationCallback, null);
        }
        else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_FINE_LOCATION_REQUEST);
        }
    }

    public class GetWeather extends AsyncTask<String, Void, String> {
        OpenWeatherAPIHandler handler;
        OpenWeatherJSONResponse response;
        @Override
        protected String doInBackground(String... params) {
            handler = new OpenWeatherAPIHandler();
            String httpData = handler.getJSONString(params[0]);
            return httpData;
        }

        @Override
        protected void onPostExecute(String s) {
            DateFormat df = new SimpleDateFormat("h:mm a z");

            try {
                super.onPostExecute(s);
                Gson gson = new Gson();
                response = gson.fromJson(s,OpenWeatherJSONResponse.class);
                textCity.setText(String.format("%s,%s",response.getName(),response.getSys().getCountry()));
                //txtLastUpdate.setText(String.format("Last Updated: %s", Common.getDateNow()));
                textWeatherDesc.setText(String.format("%s",response.getWeather().get(0).getDescription()));
                textHumidity.setText(String.format("%d%%",response.getMain().getHumidity()));
                textTime.setText(df.format(response.getDt()*1000));
                textTemp.setText(String.format("%.2f °C",response.getMain().getTemp()));
                textWindSpeed.setText(String.format("%.2f km/h",response.getWind().getSpeed()*18/5));
                textPressure.setText(String.format("%.2f hPa", response.getMain().getPressure()));
                //textIcon.setText(Html.fromHtml(OpenWeatherAPIHandler.setWeatherIcon(response.getWeather().get(0).getId(), (long) response.getSys().getSunrise() *1000, (long) response.getSys().getSunset()*1000)));

                Picasso.get().load(OpenWeatherAPIHandler.setWeatherIcon(response.getWeather().get(0).getIcon())).into(imgIcon);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

