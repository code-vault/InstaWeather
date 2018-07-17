package com.ayushojha.instaweather;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.ayushojha.instaweather.gsonclasses.OpenWeatherJSONResponse;
import com.ayushojha.instaweather.util.OpenWeatherAPIHandler;
import com.google.gson.Gson;
import java.text.DateFormat;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity{
    TextView textCity, textTime, textIcon, textTemp, textWeatherDesc,textHumidity, textWindSpeed, textPressure;
    double lat = 26.8177, lon =82.7633;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //getSupportActionBar().hide();
        setContentView(R.layout.activity_main);
        textCity = findViewById(R.id.textCity);
        textTime = findViewById(R.id.textTime);
        textIcon = findViewById(R.id.textIcon);
        textTemp = findViewById(R.id.textTemp);
        textWeatherDesc = findViewById(R.id.textWeatherDesc);
        textWindSpeed = findViewById(R.id.textWindSpeed);
        textPressure = findViewById(R.id.textPresure);
        textHumidity = findViewById(R.id.textHumidity);
        new GetWeather().execute(OpenWeatherAPIHandler.getAPIRequest(String.valueOf(lat), String.valueOf(lon)));
        Log.d("APIRequest", OpenWeatherAPIHandler.getAPIRequest(String.valueOf(lat), String.valueOf(lon)));

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
            DateFormat df = new SimpleDateFormat();

            try {
                super.onPostExecute(s);
                Gson gson = new Gson();
                response = gson.fromJson(s,OpenWeatherJSONResponse.class);
                textCity.setText(String.format("%s,%s",response.getName(),response.getSys().getCountry()));
                //txtLastUpdate.setText(String.format("Last Updated: %s", Common.getDateNow()));
                textWeatherDesc.setText(String.format("%s",response.getWeather().get(0).getDescription()));
                textHumidity.setText(String.format("%d%%",response.getMain().getHumidity()));
                textTime.setText(String.format("%s/%s",df.format(new Date((int)response.getDt()))));
                textTemp.setText(String.format("%.2f °C",response.getMain().getTemp()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

