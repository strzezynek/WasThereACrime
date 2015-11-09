package com.example.admin.wasthereacrime.helper;

import android.content.Context;

import com.example.admin.wasthereacrime.R;
import com.example.admin.wasthereacrime.model.Crime;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class StringParser {

    public static String parseCrimeName(String name) {
        String[] splittedName = name.split(":");

        if (splittedName.length > 1) {
            String crimeName = splittedName[1];
            crimeName = crimeName.substring(1);
            return crimeName;
        }

        return name;
    }

    public static String getProperDateFormat(String date) {
        String[] splitted = date.split("/");

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < splitted.length; i++) {
            sb.append(splitted[i]);
            if (i < splitted.length-1) {
                sb.append("%2F");
            }
        }

        return sb.toString();
    }

    public static String extractMonth(String date) {
        String[] splitted = date.split("-");
        return splitted[1];
    }

    public static String formatDateAsJulian(String date) {
        //Formating date from MM/DD/YYYY to YYYY-MM-DD
        String[] splitted = date.split("/");
        return splitted[2] + "-" + splitted[0] + "-" + splitted[1];
    }

    public static String[] splitDateTime(String dateTime) {
        String[] splitted = dateTime.split(" ");
        return new String[]{splitted[0], splitted[1] + splitted[2]};
    }

    public static String prepareCrimeApiUrl(String baseUrl, String startDate, String endDate,
                                            String lat, String lng) {
        StringBuilder sb = new StringBuilder();

        sb.append(baseUrl);
        sb.append("enddate=");
        sb.append(StringParser.getProperDateFormat(endDate));
        sb.append("&lat=");
        sb.append(lat);
        sb.append("&long=");
        sb.append(lng);
        sb.append("&startdate=");
        sb.append(StringParser.getProperDateFormat(startDate));

        return sb.toString();
    }

    public static String preparePlacesApiUrl(Context context, LatLng placeCoords, double radius) {
        StringBuilder searchUrl = new StringBuilder();
        searchUrl.append("https://maps.googleapis.com/maps/api/place/nearbysearch/");
        searchUrl.append("json?");
        searchUrl.append("location=");
        searchUrl.append(String.valueOf(placeCoords.latitude));
        searchUrl.append(",");
        searchUrl.append(String.valueOf(placeCoords.longitude));
        searchUrl.append("&radius=");
        searchUrl.append(String.valueOf(radius));
        searchUrl.append("&key=");
        searchUrl.append(context.getString(R.string.google_maps_key));

        return searchUrl.toString();
    }

    public static ArrayList<Crime> parseCrimeApiResponse(String response) {
        Gson gson = new Gson();
        Type type = new TypeToken<ArrayList<Crime>>(){}.getType();
        return gson.fromJson(response, type);
    }

}