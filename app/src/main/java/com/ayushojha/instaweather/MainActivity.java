package com.ayushojha.instaweather;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.ayushojha.instaweather.common.APIHandler;
import com.ayushojha.instaweather.helper.Helper;
import com.ayushojha.instaweather.model.OpenWeatherMap;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.squareup.picasso.Picasso;

import java.lang.reflect.Type;

public class MainActivity extends AppCompatActivity implements LocationListener{

        TextView textCity, textLastUpdate, textWeatherDesc, textHumidity, textTime, textTemp;
        ImageView iconWeather;

        LocationManager locationManager;
        String provider;
        static double lat, lng;
        OpenWeatherMap openWeatherMap = new OpenWeatherMap();

        int MY_PERMISSION = 0;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);

            //Control
            textCity = (TextView) findViewById(R.id.textCity);
            //txtLastUpdate = (TextView) findViewById(R.id.textLastUpdate);
            textWeatherDesc = (TextView) findViewById(R.id.textWeatherDesc);
            textHumidity = (TextView) findViewById(R.id.textHumidity);
            textTime = (TextView) findViewById(R.id.textTime);
            textTemp = (TextView) findViewById(R.id.textTemp);
            iconWeather = (ImageView) findViewById(R.id.iconWeather);


            //Get Coordinates
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            provider = locationManager.getBestProvider(new Criteria(), false);

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {


                ActivityCompat.requestPermissions(MainActivity.this, new String[]{
                        Manifest.permission.INTERNET,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_NETWORK_STATE,
                        Manifest.permission.SYSTEM_ALERT_WINDOW,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE


                }, MY_PERMISSION);
            }
            Location location = locationManager.getLastKnownLocation(provider);
            if (location == null)
                Log.e("TAG","No Location");
        }

        @Override
        protected void onPause() {
            super.onPause();
            if (ActivityCompat.checkSelfPermission(                                                                                                                                                                this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{
                        Manifest.permission.INTERNET,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_NETWORK_STATE,
                        Manifest.permission.SYSTEM_ALERT_WINDOW,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE


                }, MY_PERMISSION);
            }
            locationManager.removeUpdates(this);
        }

        @Override
        protected void onResume() {
            super.onResume();
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{
                        Manifest.permission.INTERNET,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_NETWORK_STATE,
                        Manifest.permission.SYSTEM_ALERT_WINDOW,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE


                }, MY_PERMISSION);
            }
            locationManager.requestLocationUpdates(provider, 400, 1, this);
        }

        @Override
        public void onLocationChanged(Location location) {
            lat = location.getLatitude();
            lng = location.getLongitude();

            new GetWeather().execute(APIHandler.apiRequest(String.valueOf(lat),String.valueOf(lng)));
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }

        private class GetWeather extends AsyncTask<String,Void,String> {
            ProgressDialog pd = new ProgressDialog(MainActivity.this);


            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                pd.setTitle("Please wait...");
                pd.show();

            }


            @Override
            protected String doInBackground(String... params) {
                String stream = null;
                String urlString = params[0];

                Helper http = new Helper();
                stream = http.getHTTPData(urlString);
                return stream;
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                if(s.contains("Error: Not found city")){
                    pd.dismiss();
                    return;
                }
                Gson gson = new Gson();
                Type mType = new TypeToken<OpenWeatherMap>(){}.getType();
                openWeatherMap = gson.fromJson(s,mType);
                pd.dismiss();

                textCity.setText(String.format("%s,%s",openWeatherMap.getName(),openWeatherMap.getSys().getCountry()));
                textLastUpdate.setText(String.format("Last Updated: %s", APIHandler.getDateNow()));
                textWeatherDesc.setText(String.format("%s",openWeatherMap.getWeather().get(0).getDescription()));
                textHumidity.setText(String.format("%d%%",openWeatherMap.getMain().getHumidity()));
                textTime.setText(String.format("%s/%s",APIHandler.unixTimeStampToDateTime(openWeatherMap.getSys().getSunrise()),APIHandler.unixTimeStampToDateTime(openWeatherMap.getSys().getSunset())));
                textTemp.setText(String.format("%.2f °C",openWeatherMap.getMain().getTemp()));
                Picasso.get()
                        .load(APIHandler.getImage(openWeatherMap.getWeather().get(0).getIcon()))
                        .into(iconWeather);

            }

        }
    }

