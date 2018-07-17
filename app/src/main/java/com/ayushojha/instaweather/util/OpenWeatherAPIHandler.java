package com.ayushojha.instaweather.util;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class OpenWeatherAPIHandler {

    //private final String apiKey = "45a1c624df5f787257ef0492eebae544";
    private static final String requestUrl = "https://api.openweathermap.org/data/2.5/weather?lat=%s&lon=%s&units=metric&appid=45a1c624df5f787257ef0492eebae544";
    private URL url;
    private JSONObject json;
    private HttpsURLConnection connection;

    public static String getAPIRequest(String lat, String lon) {
        return String.format(requestUrl,lat,lon);
    }
    public String getJSONString(String apiRequest) {
        String data = "";
        try {
            url = new URL(apiRequest);
            connection = (HttpsURLConnection) url.openConnection();
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


}
