package com.ayushojha.instaweather.util;

import android.app.Activity;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.Handler;
import android.view.View;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.ayushojha.instaweather.R;
import com.ayushojha.instaweather.activities.SplashActivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

public class OpenWeatherAPIHandler {

    private Handler handler;
    private RequestQueue queue;

    //format the api url with endpoint[metric] and lat lon values
    public static String makeURL(String endPoint, String lat, String lon) {
        return String.format(endPoint, lat, lon);
    }


    public static String setWeatherIcon(String iconId) {
        return String.format("https://openweathermap.org/img/w/%s.png", iconId);
    }

    public static String setWeatherIcon(int weatherId, long sunrise, long sunset) {
        String icon = "wi_owm_%s_%s";
        long currentTime = new Date().getTime()/1000;
        if (currentTime >= sunrise && currentTime < sunset) {
            return String.format(icon, "day", String.valueOf(weatherId));
        } else {
            return String.format(icon, "night", String.valueOf(weatherId));
        }
    }


}
