package com.example.admin.wasthereacrime.activity;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.location.Location;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;

import com.example.admin.wasthereacrime.R;
import com.example.admin.wasthereacrime.WasThereACrimeApp;
import com.example.admin.wasthereacrime.helper.HelpNotifier;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

import java.util.Calendar;

public class CrimeDetailsActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener{

    private static final String TAG = CrimeDetailsActivity.class.getSimpleName();

    private static final String EXTRA_CHOOSING_LOCATION = "choosing_location";
    private static final String EXTRA_START_DATE = "start_date";
    private static final String EXTRA_END_DATE = "end_date";
    private static final String EXTRA_LATITUDE = "latitude";
    private static final String EXTRA_LONGITUDE = "longitude";

    private static final int REQUEST_CHOOSE_LOCATION = 100;

    private WasThereACrimeApp app;

    private EditText startDateEdit;
    private EditText endDateEdit;
    private EditText latitudeEdit;
    private EditText longitudeEdit;
    private Button showCrimesBtn;
    private ImageButton chooseLocationBtn;

    private GoogleApiClient googleApiClient;
    private Location lastLocation;

    private final DatePickerDialog.OnDateSetListener startDateSetListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            String date = String.valueOf(monthOfYear) + "/" + String.valueOf(dayOfMonth) + "/"
                    + String.valueOf(year);
            startDateEdit.setText(date);
        }
    };

    private final DatePickerDialog.OnDateSetListener endDateSetListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            String date = String.valueOf(monthOfYear) + "/" + String.valueOf(dayOfMonth) + "/"
                    + String.valueOf(year);
            endDateEdit.setText(date);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crime_details);

        app = (WasThereACrimeApp) getApplication();

        startDateEdit = (EditText) findViewById(R.id.edit_start_date);
        startDateEdit.setFocusable(false);
        startDateEdit.setText("9/19/2015");
        startDateEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog(startDateSetListener);
            }
        });
        endDateEdit = (EditText) findViewById(R.id.edit_end_date);
        endDateEdit.setFocusable(false);
        endDateEdit.setText("9/25/2015");
        endDateEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog(endDateSetListener);
            }
        });
        latitudeEdit = (EditText) findViewById(R.id.edit_latitude);
        latitudeEdit.setFocusable(false);
        latitudeEdit.setText("37.757815");
        longitudeEdit = (EditText) findViewById(R.id.edit_longitude);
        longitudeEdit.setFocusable(false);
        longitudeEdit.setText("-122.5076392");
        chooseLocationBtn = (ImageButton) findViewById(R.id.btn_choose_location);
        chooseLocationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CrimeDetailsActivity.this, MapsActivity.class);
                intent.putExtra(EXTRA_CHOOSING_LOCATION, true);
                intent.putExtra(EXTRA_LATITUDE, Double.parseDouble(latitudeEdit.getText().toString()));
                intent.putExtra(EXTRA_LONGITUDE, Double.parseDouble(longitudeEdit.getText().toString()));
                startActivityForResult(intent, REQUEST_CHOOSE_LOCATION);
            }
        });
        showCrimesBtn = (Button) findViewById(R.id.btn_show_crimes);
        showCrimesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (areDatesFilled()) {
//                    Intent intent = new Intent(CrimeDetailsActivity.this, CrimeListActivity.class);
                    Intent intent = new Intent(CrimeDetailsActivity.this, MapsActivity.class);
                    intent.putExtra(EXTRA_CHOOSING_LOCATION, false);
                    intent.putExtra(EXTRA_START_DATE, startDateEdit.getText().toString());
                    intent.putExtra(EXTRA_END_DATE, endDateEdit.getText().toString());
                    intent.putExtra(EXTRA_LATITUDE, Double.parseDouble(latitudeEdit.getText().toString()));
                    intent.putExtra(EXTRA_LONGITUDE, Double.parseDouble(longitudeEdit.getText().toString()));
                    startActivity(intent);
                } else {
                    Snackbar.make(v, "Enter dates", Snackbar.LENGTH_SHORT).show();
                }
            }
        });

        setUpFabs();

        buildGoogleApiClient();
    }

    private synchronized void buildGoogleApiClient() {
//        Log.d(TAG, "Building google api client");
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    private void setUpFabs() {
        HelpNotifier.setUpAlarmFabs(this, app.getMyLocation());
    }

    private void showDatePickerDialog(DatePickerDialog.OnDateSetListener listener) {
        Calendar cal = Calendar.getInstance();
        cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH) - 1);
        DatePickerDialog dateDialog = new DatePickerDialog(CrimeDetailsActivity.this,
                listener, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
        dateDialog.getDatePicker().setMaxDate(cal.getTimeInMillis());
        dateDialog.show();
    }

    private boolean areDatesFilled() {
//        Log.d(TAG, "Start date: " + startDateEdit.getText().toString());
//        Log.d(TAG, "End date: " + endDateEdit.getText().toString());
        return !(startDateEdit.getText().toString().equals("")
                && endDateEdit.getText().toString().equals(""));
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d(TAG, "Connected to location api");
        lastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
        if (lastLocation != null) {
            latitudeEdit.setText(String.valueOf(lastLocation.getLatitude()));
            longitudeEdit.setText(String.valueOf(lastLocation.getLongitude()));
        } else {
            latitudeEdit.setText("0.0");
            longitudeEdit.setText("0.0");
        }

        app.setMyLocation(new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude()));
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "Localization api connection suspended");
        latitudeEdit.setText("0.0");
        longitudeEdit.setText("0.0");
        app.setMyLocation(new LatLng(Double.parseDouble(latitudeEdit.getText().toString()),
                Double.parseDouble(longitudeEdit.getText().toString())));
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "Localization api connection failed");
        latitudeEdit.setText("10.0");
        longitudeEdit.setText("10.0");
        app.setMyLocation(new LatLng(Double.parseDouble(latitudeEdit.getText().toString()),
                Double.parseDouble(longitudeEdit.getText().toString())));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CHOOSE_LOCATION) {
            if (resultCode == Activity.RESULT_OK) {
                latitudeEdit.setText(String.valueOf(data.getDoubleExtra("latitude", 0.0)));
                longitudeEdit.setText(String.valueOf(data.getDoubleExtra("longitude", 0.0)));
            }
        }
    }
}
