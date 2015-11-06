package com.example.admin.wasthereacrime.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.admin.wasthereacrime.R;
import com.example.admin.wasthereacrime.model.Crime;

import java.util.List;

public class CrimeAdapter extends ArrayAdapter<Crime> {

    private static final String TAG = CrimeAdapter.class.getSimpleName();

    private final List<Crime> crimes;

    public CrimeAdapter(Context context, int resource, List<Crime> items) {
        super(context, resource, items);
        crimes = items;
    }

    @Override
    public int getCount() {
        return crimes.size();
    }

    @Override
    public Crime getItem(int position) {
        return crimes.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        if (v == null) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            v = inflater.inflate(R.layout.item_crime, parent, false);
        }

        Crime c = getItem(position);

        if (c != null) {
            TextView descriptionLabel = (TextView) v.findViewById(R.id.crime_description);
            TextView dateLabel = (TextView) v.findViewById(R.id.crime_date);
            TextView latLabel = (TextView) v.findViewById(R.id.crime_location_lat);
            TextView lngLabel = (TextView) v.findViewById(R.id.crime_location_lng);

            descriptionLabel.setText(c.getDescription());
            dateLabel.setText(c.getDatetime());
            latLabel.setText("Lat: " + c.getLatitude());
            lngLabel.setText("Lng: " + c.getLongitude());
        }

        return v;
    }
}
