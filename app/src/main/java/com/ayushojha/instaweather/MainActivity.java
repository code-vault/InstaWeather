package com.ayushojha.instaweather;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.ayushojha.instaweather.gsonclasses.OpenWeatherJSONResponse;
import com.ayushojha.instaweather.util.OpenWeatherAPIHandler;
import com.google.gson.Gson;

public class MainActivity extends AppCompatActivity{
    TextView textCity, textTime, textIcon, textTemp, textWeatherDesc,textHumidity, textWindSpeed, textPressure;
    double lat = 26.8177, lon =82.7633;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_main);
        textCity = findViewById(R.id.textCity);
        textTime = findViewById(R.id.textTime);
        textIcon = findViewById(R.id.textIcon);
        textTemp = findViewById(R.id.textTemp);
        textWeatherDesc = findViewById(R.id.textWeatherDesc);
        textWindSpeed = findViewById(R.id.textWindSpeed);
        textPressure = findViewById(R.id.textPresure);
        new GetWeather().execute(OpenWeatherAPIHandler.getAPIRequest(String.valueOf(lat), String.valueOf(lon)));

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
            super.onPostExecute(s);
            Gson gson = new Gson();
            response = gson.fromJson(s,OpenWeatherJSONResponse.class);
            textCity.setText(response.getName().toUpperCase());
            textHumidity.setText(response.getMain().getHumidity());
            textTemp.setText(String.valueOf((response.getMain().getTemp())));
            textPressure.setText(String.valueOf(response.getMain().getPressure()));
            textWeatherDesc.setText(response.getWeathers().get(0).getDescription().toUpperCase());
        }
    }
}

