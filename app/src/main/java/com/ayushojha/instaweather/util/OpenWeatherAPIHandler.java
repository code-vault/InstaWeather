package com.ayushojha.instaweather.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;

import javax.net.ssl.HttpsURLConnection;

public class OpenWeatherAPIHandler {
    private URL url;
    //private JSONObject json;
    private HttpsURLConnection connection;


    //format the api url with endpoint[metric] and lat lon values
    public static String makeURL(String endPoint, String lat, String lon) {
        return String.format(endPoint, lat, lon);
    }

    //get the JSON response as string
    public String getJSONString(String apiRequest) {
        String data = "";
        try {
            url = new URL(apiRequest);
            connection = (HttpsURLConnection) url.openConnection();
            if (connection.getResponseCode() == 200) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line = "";
                while ((line = reader.readLine()) != null)
                    sb.append(line);
                data = sb.toString();
                connection.disconnect();
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return data;
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
