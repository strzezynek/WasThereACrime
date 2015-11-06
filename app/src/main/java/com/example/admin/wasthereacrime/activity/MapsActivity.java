package com.example.admin.wasthereacrime.activity;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.design.widget.FloatingActionButton;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.admin.wasthereacrime.R;
import com.example.admin.wasthereacrime.WasThereACrimeApp;
import com.example.admin.wasthereacrime.adapter.CrimeCursorAdapter;
import com.example.admin.wasthereacrime.database.CrimeProvider;
import com.example.admin.wasthereacrime.helper.HelpNotifier;
import com.example.admin.wasthereacrime.helper.StringParser;
import com.example.admin.wasthereacrime.model.Crime;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.maps.android.heatmaps.HeatmapTileProvider;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = MapsActivity.class.getSimpleName();

    private static final String EXTRA_CHOOSING_LOCATION = "choosing_location";
    private static final String EXTRA_START_DATE = "start_date";
    private static final String EXTRA_END_DATE = "end_date";
    private static final String EXTRA_LATITUDE = "latitude";
    private static final String EXTRA_LONGITUDE = "longitude";

    private WasThereACrimeApp app;

    private GoogleMap mMap;

    private String startDate;
    private String endDate;
    private double latitude;
    private double longitude;

    private Marker marker;

    private static final String baseUrl = "https://jgentes-Crime-Data-v1.p.mashape.com/crime?";
    private String apiUrl;

    private boolean choosingLocation;

    private List<LatLng> coordinates = new ArrayList<>();
    private HeatmapTileProvider heatMapProvider = null;
    private TileOverlay overlay = null;
    private boolean heatTurnedOn = false;

    private EditText startDateEdit;
    private EditText endDateEdit;
    private Button showBtn;

    private CrimeCursorAdapter cursorAdapter;
    private Cursor crimeCursor = null;
    private boolean connectionSuccess = true;
    private String[] projection;
    private String selection;
    private String[] selectionArgs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        app = (WasThereACrimeApp) getApplication();
        Intent intent = getIntent();
        choosingLocation = intent.getBooleanExtra(EXTRA_CHOOSING_LOCATION, false);
        latitude = intent.getDoubleExtra(EXTRA_LATITUDE, 0.0);
        longitude = intent.getDoubleExtra(EXTRA_LONGITUDE, 0.0);
        if (!choosingLocation) {
            setContentView(R.layout.activity_maps);
            startDateEdit = (EditText) findViewById(R.id.edit_start_date);
            endDateEdit = (EditText) findViewById(R.id.edit_end_date);
            showBtn = (Button) findViewById(R.id.btn_show_crimes);
            showBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startDate = startDateEdit.getText().toString();
                    endDate = endDateEdit.getText().toString();
                    getNewCrimeData();
                }
            });
            startDate = intent.getStringExtra(EXTRA_START_DATE);
            endDate = intent.getStringExtra(EXTRA_END_DATE);
            startDateEdit.setText(startDate);
            endDateEdit.setText(endDate);
            apiUrl = prepareUrl();
            setUpMapFabs();
        } else {
            setContentView(R.layout.activity_maps2);
        }
        setUpFabs();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        mapFragment.getMapAsync(this);
    }

    private void setUpFabs() {
        HelpNotifier.setUpAlarmFabs(this, app.getMyLocation());
    }

    private void setUpMapFabs() {
        FloatingActionButton fabHeat = (FloatingActionButton) findViewById(R.id.fab_heat);
        fabHeat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                heatTurnedOn = !heatTurnedOn;
                if (heatTurnedOn) {
                    removeMarkers();
                } else {
                    markCrimesOnMap();
                }
                turnOnHeatMap(heatTurnedOn);
            }
        });
        FloatingActionButton fabList = (FloatingActionButton) findViewById(R.id.fab_list);
        fabList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MapsActivity.this, CrimeListActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (choosingLocation) {
            Intent intent = new Intent();
            intent.putExtra("latitude", latitude);
            intent.putExtra("longitude", longitude);
            setResult(Activity.RESULT_OK, intent);
        }
        finish();
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
//        Log.d(TAG, "MapReady");
        mMap = googleMap;
        mMap.setMyLocationEnabled(true);

        UiSettings mapSettings = mMap.getUiSettings();
        mapSettings.setZoomControlsEnabled(true);
        mapSettings.setMapToolbarEnabled(true);
        mapSettings.setAllGesturesEnabled(true);

        LatLng location = new LatLng(latitude, longitude);
        latitude = location.latitude;
        longitude = location.longitude;
//        Log.d(TAG, "Loc: " + latitude + " " + longitude);

        marker = mMap.addMarker(new MarkerOptions().position(location).title("Chosen location")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
        marker.setDraggable(true);
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 10));
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                marker.remove();
                latitude = latLng.latitude;
                longitude = latLng.longitude;
                Log.d(TAG, "Loc: " + latitude + " " + longitude);

                if (!choosingLocation) {
                    getNewCrimeData();
                }

                //TODO marker dragable
                marker = mMap.addMarker(new MarkerOptions().position(latLng).title("Chosen location")
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
                marker.setDraggable(true);
                mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
            }
        });

        if (!choosingLocation) {
            getCrimeDataFromApi();
        }
    }

    private void markCrimesOnMap() {
        LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
        if (connectionSuccess) {
            for (Crime c : app.getCrimes()) {
                Marker m = addMarker(c);
                boundsBuilder.include(m.getPosition());
                coordinates.add(c.getLatLng());
            }
        } else {
            crimeCursor.moveToFirst();
            while (!crimeCursor.isAfterLast()) {
                LatLng cLatLng = getCrimeLatLngFromCursor(crimeCursor);
                if (isCrimeInRadius(cLatLng)) {
                    Crime c = createCrimeFromCursor(crimeCursor);

                    Marker m = addMarker(c);
                    boundsBuilder.include(m.getPosition());
                    coordinates.add(c.getLatLng());
                }
                crimeCursor.moveToNext();
            }
        }
        if (app.getCrimes().size() > 0 || coordinates.size() > 0) {
            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), 30));
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);
            builder.setTitle("No crimes found").setMessage("No crimes found in chosen area");
            AlertDialog dialog = builder.create();
            dialog.show();
            Log.d(TAG, "test");
        }
    }

    private Marker addMarker(Crime crime) {
        LatLng latLng = crime.getLatLng();
        return mMap.addMarker(new MarkerOptions().position(latLng)
                .title(crime.getDescription()).snippet(crime.getDatetime()));
    }

    private void removeMarkers() {
        mMap.clear();
    }

    private void turnOnHeatMap(boolean showed) {
        heatTurnedOn = showed;
        if (heatTurnedOn) {
            Log.d(TAG, "HeatMapOn");
            heatMapProvider = new HeatmapTileProvider.Builder().data(coordinates).build();
            overlay = mMap.addTileOverlay(new TileOverlayOptions().tileProvider(heatMapProvider));
        } else {
            Log.d(TAG, "HeatMapOff");
            if (overlay != null) {
                overlay.remove();
            }
        }
    }

    private boolean isCrimeInRadius(LatLng crimeLatLng) {
        float[] result = new float[3];
        Location.distanceBetween(latitude, longitude, crimeLatLng.latitude, crimeLatLng.longitude,
                result);

        if (result[0] <= 5000){
            return true;
        }
        return false;
    }

    private LatLng getCrimeLatLngFromCursor(Cursor cursor) {
        Double cLat = Double.parseDouble(cursor.getString(
                cursor.getColumnIndexOrThrow(CrimeProvider.CrimeColumns.COL_LAT)));
        Double cLng = Double.parseDouble(cursor.getString(
                cursor.getColumnIndexOrThrow(CrimeProvider.CrimeColumns.COL_LNG)));
        return new LatLng(cLat, cLng);
    }

    private Crime createCrimeFromCursor(Cursor cursor) {
        String cId = cursor.getString(
                cursor.getColumnIndexOrThrow(CrimeProvider.CrimeColumns.COL_CRIME_ID));
        String cDescr = cursor.getString(
                cursor.getColumnIndexOrThrow(CrimeProvider.CrimeColumns.COL_DESCRIPTION));
        String cDate = cursor.getString(
                cursor.getColumnIndexOrThrow(CrimeProvider.CrimeColumns.COL_DATE));
        String cTime = cursor.getString(
                cursor.getColumnIndexOrThrow(CrimeProvider.CrimeColumns.COL_TIME));
        String cLat = cursor.getString(
                cursor.getColumnIndexOrThrow(CrimeProvider.CrimeColumns.COL_LAT));
        String cLng = cursor.getString(
                cursor.getColumnIndexOrThrow(CrimeProvider.CrimeColumns.COL_LNG));
        return new Crime(cId, cDescr, cDate + " " + cTime, Double.parseDouble(cLat),
                Double.parseDouble(cLng));
    }

    private void getNewCrimeData() {
        turnOnHeatMap(false);
        removeMarkers();
        app.clearCrimes();
        coordinates.clear();
        apiUrl = prepareUrl();
        getCrimeDataFromApi();
    }

    private void getCrimeDataFromApi() {
        Log.d(TAG, "Connecting with API: " + apiUrl);
        RequestQueue reqQueue = Volley.newRequestQueue(this);

        StringRequest crimeDataRequest = new StringRequest(Request.Method.GET, apiUrl,//tmpUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG, "Response received");
                        connectionSuccess = true;
                        parseResponseAndCreateCrimes(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d(TAG, "Response error");
                        connectionSuccess = false;
                        AlertDialog.Builder dialogBuilder
                                = new AlertDialog.Builder(MapsActivity.this);
                        dialogBuilder.setTitle("Connection failed")
                                .setMessage("Connection with remote API failed. Getting crime data from DB if it exists");
                        dialogBuilder.create().show();

                        projection = new String[]{};
                        selection = CrimeProvider.CrimeColumns.COL_DATE + " BETWEEN ? AND ?";
                        selectionArgs = new String[]{StringParser.formatDateAsJulian(startDate),
                                StringParser.formatDateAsJulian(endDate)};
                        getLoaderManager().initLoader(0, null, MapsActivity.this);
                    }
                }) {

            @Override
            public HashMap<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("X-Mashape-Key", "NnGbWLUJaemshVbmqhvHqkkuAorLp16nKWYjsn4FDu7SUVaxVi");
                headers.put("Accept", "application/json");

                return headers;
            }

        };

        reqQueue.add(crimeDataRequest);
    }

    private void parseResponseAndCreateCrimes(String response) {
        app.clearCrimes();

        Log.d(TAG, response);
        Gson gson = new Gson();
        ArrayList<Crime> crimes;
        Type type = new TypeToken<ArrayList<Crime>>(){}.getType();
        crimes = gson.fromJson(response, type);
        app.addCrimes(crimes);

        new InsertTask().execute(crimes);

        markCrimesOnMap();
    }

    private String prepareUrl() {
        StringBuilder sb = new StringBuilder();

        sb.append(baseUrl);
        sb.append("enddate=");
        sb.append(StringParser.getProperDateFormat(endDate));
        sb.append("&lat=");
        sb.append(latitude);
        sb.append("&long=");
        sb.append(longitude);
        sb.append("&startdate=");
        sb.append(StringParser.getProperDateFormat(startDate));

        return sb.toString();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
//        projection = new String[]{};
//        String selection = "";
//        String[] selectionArgs = {};

        return new CursorLoader(MapsActivity.this, CrimeProvider.CONTENT_URI, projection, selection, selectionArgs,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        crimeCursor = data;
        Log.d(TAG, "Crimes retrieved: " + crimeCursor.getCount());
        markCrimesOnMap();
//        cursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        crimeCursor = null;
//        cursorAdapter.swapCursor(null);
    }

    private class InsertTask extends AsyncTask<ArrayList<Crime>, Integer, Integer> {

        @Override
        protected Integer doInBackground(ArrayList<Crime>... params) {
            ArrayList<Crime> crimes = params[0];
//            Log.d(TAG, "Crimes count: " + crimes.size());
            int crimesInserted = 0;
            for (Crime c : crimes) {
                if (isCrimeUnique(c)) {
//                    Log.d(TAG, "Unique crime! Inserting...");
                    Uri uri = getContentResolver().insert(CrimeProvider.CONTENT_URI,
                            getContentValues(c));
                    if (uri != null) {
                        crimesInserted++;
                    }
                }
            }
            return crimesInserted;
        }

        @Override
        protected void onPostExecute(Integer crimesNum) {
            Log.d(TAG, "Crimes inserted into DB: " + crimesNum);
        }

        private boolean isCrimeUnique(Crime crime) {
            Cursor cursor;
            String[] projection = {CrimeProvider.CrimeColumns.COL_CRIME_ID};
            String selection = CrimeProvider.CrimeColumns.COL_CRIME_ID + " = ?";
            String[] selectionArgs = {crime.getId()};
            cursor = getContentResolver().query(CrimeProvider.CONTENT_URI,
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

        private ContentValues getContentValues(Crime crime) {
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
    }
}
