package com.ayushojha.instaweather.common;

import android.support.annotation.NonNull;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class APIHandler {
        public static String API_KEY = "64cf53bbbeff828af61697d956a98849";
        public static String API_URL = "http://api.openweathermap.org/data/2.5/weather";

        @NonNull
        public static String apiRequest(String lat, String lng){
            StringBuilder sb = new StringBuilder(API_URL);
            sb.append(String.format("?lat=%s&lon=%s&APPID=%s&units=metric",lat,lng,API_KEY));
            return sb.toString();
        }

        public static String unixTimeStampToDateTime(double unixTimeStamp){
            DateFormat dateFormat = new SimpleDateFormat("HH:mm");
            Date date = new Date();
            date.setTime((long)unixTimeStamp*1000);
            return dateFormat.format(date);
        }

        public static String getImage(String icon){
            return String.format("http://openweathermap.org/img/w/%s.png",icon);
        }

        public static String getDateNow(){
            DateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy HH:mm");
            Date date = new Date();
            return dateFormat.format(date);
        }

}
