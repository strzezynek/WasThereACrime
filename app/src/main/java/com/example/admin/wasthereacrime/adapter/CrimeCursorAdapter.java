package com.example.admin.wasthereacrime.adapter;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.example.admin.wasthereacrime.R;
import com.example.admin.wasthereacrime.model.Crime;

public class CrimeCursorAdapter extends CursorAdapter {
    public CrimeCursorAdapter(Context context, Cursor cursor) {
        super(context, cursor, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {

        return LayoutInflater.from(context).inflate(R.layout.item_crime, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        Crime c = Crime.getCrimeFromCursor(cursor);
        if (c != null) {
            TextView descriptionLabel = (TextView) view.findViewById(R.id.crime_description);
            TextView dateLabel = (TextView) view.findViewById(R.id.crime_date);
            TextView latLabel = (TextView) view.findViewById(R.id.crime_location_lat);
            TextView lngLabel = (TextView) view.findViewById(R.id.crime_location_lng);

            descriptionLabel.setText(c.getDescription());
            dateLabel.setText(c.getDatetime());
            latLabel.setText("Lat: " + c.getLatitude());
            lngLabel.setText("Lng: " + c.getLongitude());
        }
    }
}
