package com.example.admin.wasthereacrime.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.example.admin.wasthereacrime.helper.StringParser;
import com.example.admin.wasthereacrime.model.Crime;
import com.google.android.gms.maps.model.LatLng;

public class DBOperator {

    private static final String TAG = DBOperator.class.getSimpleName();

    public DBOperator() {

    }

    public Cursor getDataFromDB(Context context, Uri uri, String[] projection, String selection,
                                       String[] selectionArgs, String sortOrder) {
        return context.getContentResolver().query(uri, projection, selection,
                selectionArgs, null);
    }

    public Cursor getAllCrimes(Context context) {
        String[] projection = {};
        String selection = "";
        String[] selectionArgs = {};
        return getDataFromDB(context, CrimeProvider.CONTENT_URI, projection, selection,
                selectionArgs, null);
    }

    public int updateCrime(Context context, Crime crime) {
        String selection = CrimeProvider.CrimeColumns.COL_CRIME_ID + " = ?";
        String[] selectionArgs = {crime.getId()};
        return context.getContentResolver().update(CrimeProvider.CONTENT_URI, getContentValues(crime), selection,
                selectionArgs);
    }

    public ContentValues getContentValues(Crime crime) {
        ContentValues values = new ContentValues();
        values.put(CrimeProvider.CrimeColumns.COL_CRIME_ID, crime.getId());
        values.put(CrimeProvider.CrimeColumns.COL_DESCRIPTION, crime.getDescription());
        String[] dateTimeSplit = StringParser.splitDateTime(crime.getDatetime());
        values.put(CrimeProvider.CrimeColumns.COL_DATE, StringParser.formatDateAsJulian(dateTimeSplit[0]));
        values.put(CrimeProvider.CrimeColumns.COL_TIME, dateTimeSplit[1]);
        values.put(CrimeProvider.CrimeColumns.COL_LAT, String.valueOf(crime.getLatitude()));
        values.put(CrimeProvider.CrimeColumns.COL_LNG, String.valueOf(crime.getLongitude()));
        return values;
    }

    public Crime getCrimeFromCursor(Cursor cursor) {
        String id = cursor.getString(cursor.getColumnIndexOrThrow(
                CrimeProvider.CrimeColumns.COL_CRIME_ID));
        String descr = cursor.getString(cursor.getColumnIndexOrThrow(
                CrimeProvider.CrimeColumns.COL_DESCRIPTION));
        String date = cursor.getString(cursor.getColumnIndexOrThrow(
                CrimeProvider.CrimeColumns.COL_DATE)) + " "
                + cursor.getString(cursor.getColumnIndexOrThrow(CrimeProvider.CrimeColumns.COL_TIME));
        double lat = Double.parseDouble(cursor.getString(cursor.getColumnIndexOrThrow(
                CrimeProvider.CrimeColumns.COL_LAT)));
        double lng = Double.parseDouble(cursor.getString(cursor.getColumnIndexOrThrow(
                CrimeProvider.CrimeColumns.COL_LNG)));
        return new Crime(id, descr, date, lat, lng);
    }

    public LatLng getCrimeLatLngFromCursor(Cursor cursor) {
        Double cLat = Double.parseDouble(cursor.getString(
                cursor.getColumnIndexOrThrow(CrimeProvider.CrimeColumns.COL_LAT)));
        Double cLng = Double.parseDouble(cursor.getString(
                cursor.getColumnIndexOrThrow(CrimeProvider.CrimeColumns.COL_LNG)));
        return new LatLng(cLat, cLng);
    }

    public boolean isCrimeUnique(Context context, Crime crime) {
        Cursor cursor;
        String[] projection = {CrimeProvider.CrimeColumns.COL_CRIME_ID};
        String selection = CrimeProvider.CrimeColumns.COL_CRIME_ID + " = ?";
        String[] selectionArgs = {crime.getId()};
        cursor = context.getContentResolver().query(CrimeProvider.CONTENT_URI,
                projection, selection, selectionArgs, null);
        if (cursor != null && cursor.getCount() > 0) {
//                Log.d(TAG, "Crime not unique");
            cursor.close();
            return false;
        } else if (cursor != null && cursor.getCount() == 0) {
//                Log.d(TAG, "Crime unique");
            cursor.close();
            return true;
        } else {
//                Log.d(TAG, "Cursor null");
            return false;
        }
    }

    public boolean isCrimeUpToDate(Context context, Crime crime) {
        Cursor cursor;
        String[] projection = {};
        String selection = CrimeProvider.CrimeColumns.COL_CRIME_ID + " = ?";
        String[] selectionArgs = {crime.getId()};
        cursor = context.getContentResolver().query(CrimeProvider.CONTENT_URI, projection, selection,
                selectionArgs, null);
        cursor.moveToFirst();

        String description = crime.getDescription();
        String[] splittedDateTime = StringParser.splitDateTime(crime.getDatetime());
        String date = splittedDateTime[0];
        String time = splittedDateTime[1];
        String lat = String.valueOf(crime.getLatitude());
        String lng = String.valueOf(crime.getLongitude());

        if (!cursor.getString(cursor.getColumnIndexOrThrow(
                CrimeProvider.CrimeColumns.COL_DESCRIPTION)).equals(description))
            return false;
        else if (!cursor.getString(cursor.getColumnIndexOrThrow(
                CrimeProvider.CrimeColumns.COL_DATE)).equals(date))
            return false;
        else if (!cursor.getString(cursor.getColumnIndexOrThrow(
                CrimeProvider.CrimeColumns.COL_TIME)).equals(time))
            return false;
        else if (!cursor.getString(cursor.getColumnIndexOrThrow(
                CrimeProvider.CrimeColumns.COL_LAT)).equals(lat))
            return false;
        else if (!cursor.getString(cursor.getColumnIndexOrThrow(
                CrimeProvider.CrimeColumns.COL_LNG)).equals(lng))
            return false;

        return true;
    }
}
