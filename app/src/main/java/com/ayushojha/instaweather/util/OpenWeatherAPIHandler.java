package com.ayushojha.instaweather.util;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;

import javax.net.ssl.HttpsURLConnection;

public class OpenWeatherAPIHandler {

    //private final String apiKey = "45a1c624df5f787257ef0492eebae544";
    private static final String requestUrl = "http://api.openweathermap.org/data/2.5/weather?lat=%s&lon=%s&units=metric&appid=45a1c624df5f787257ef0492eebae544";
    private URL url;
    //private JSONObject json;
    private HttpURLConnection connection;

    public static String getAPIRequest(String lat, String lon) {
        return String.format(requestUrl,lat,lon);
    }
    public String getJSONString(String apiRequest) {
        String data = "";
        try {
            url = new URL(apiRequest);
            connection = (HttpURLConnection) url.openConnection();
            if(connection.getResponseCode() == 200) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line = "";
                while((line = reader.readLine())!=null)
                    sb.append(line);
                data = sb.toString();
                connection.disconnect();
            }
        }
        catch (MalformedURLException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return data;
    }

    public static String setWeatherIcon(String iconId) {
        return String.format("http://openweathermap.org/img/w/%s.png",iconId);
    }

    public static String setWeatherIcon(int actualId, long sunrise, long sunset){
        int id = actualId / 100;
        String icon = "";
        if(actualId == 800){
            long currentTime = new Date().getTime();
            if(currentTime>=sunrise && currentTime<sunset) {
                icon = "&#xf00d;";
            } else {
                icon = "&#xf02e;";
            }
        } else {
            switch(id) {
                case 2 : icon = "&#xf01e;";
                    break;
                case 3 : icon = "&#xf01c;";
                    break;
                case 7 : icon = "&#xf014;";
                    break;
                case 8 : icon = "&#xf013;";
                    break;
                case 6 : icon = "&#xf01b;";
                    break;
                case 5 : icon = "&#xf019;";
                    break;
            }
        }
        return icon;
    }



}
