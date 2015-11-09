package com.example.admin.wasthereacrime.model;

import java.util.ArrayList;

public class Crimes {

    ArrayList<Crime> crimes;

    public Crimes() {
        crimes = new ArrayList<>();
    }

    public void addCrime(Crime crime) {
        crimes.add(crime);
    }

    public void addCrimes(ArrayList<Crime> newCrimes) {
        crimes = newCrimes;
    }

    public void clearCrimes() {
        crimes.clear();
    }

    public Crime getCrime(int idx) {
        return crimes.get(idx);
    }

    public ArrayList<Crime> getCrimes() {
        return crimes;
    }
}
