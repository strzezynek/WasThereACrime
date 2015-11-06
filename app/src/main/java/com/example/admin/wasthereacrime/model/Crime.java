package com.example.admin.wasthereacrime.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.example.admin.wasthereacrime.helper.StringParser;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.annotations.SerializedName;


public class Crime implements Parcelable {

    private String id;
    private String description;
    private String datetime;
    @SerializedName("lat")
    private double latitude;
    @SerializedName("long")
    private double longitude;

    public Crime(String id, String description, String datetime, double latitude, double lng) {
        init(id, description, datetime, latitude, lng);
    }

    private void init(String id, String description, String date, double lat, double lng) {
        setId(id);
        setDescription(StringParser.parseCrimeName(description));
        setDatetime(date);
        setLatitude(lat);
        setLongitude(lng);
    }

    public String getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public String getDatetime() {
        return datetime;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public LatLng getLatLng() { return new LatLng(latitude, longitude); }

    public void setId(String id) {
        this.id = id;
    }

    private void setDescription(String description) {
        this.description = description;
    }

    private void setDatetime(String datetime) {
        this.datetime = datetime;
    }

    private void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    private void setLongitude(double lng) {
        this.longitude = lng;
    }

    private Crime(Parcel parcel) {
        String[] data = new String[5];

        parcel.readStringArray(data);
        id = data[0];
        description = data[1];
        datetime = data[2];
        latitude = Double.parseDouble(data[3]);
        longitude = Double.parseDouble(data[4]);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStringArray(new String[] {id, description, datetime, String.valueOf(latitude),
                String.valueOf(longitude)});
    }

    public static Parcelable.Creator creator = new Parcelable.Creator() {

        @Override
        public Crime createFromParcel(Parcel parcel) {
            return new Crime(parcel);
        }

        @Override
        public Crime[] newArray(int size) {
            return new Crime[size];
        }
    };
}
