package com.example.admin.wasthereacrime;

import android.app.Application;

import com.example.admin.wasthereacrime.model.Crime;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

public class WasThereACrimeApp extends Application {

    private WasThereACrimeApp instance;

    private ArrayList<Crime> crimes = new ArrayList<>();

    private LatLng myLocation = new LatLng(0.0, 0.0);

    public WasThereACrimeApp() {

    }

    public WasThereACrimeApp getInstance() {
        instance = this;
        return instance;
    }

    public void clearCrimes() {
        crimes.clear();
    }

    public void addCrime(Crime c) {
        crimes.add(c);
    }

    public void addCrimes(ArrayList<Crime> newCrimes) {
        crimes = newCrimes;
    }

    public ArrayList<Crime> getCrimes() {
        return crimes;
    }

    public LatLng getMyLocation() {
        return myLocation;
    }

    public void setMyLocation(LatLng location) {
        myLocation = location;
    }
}
