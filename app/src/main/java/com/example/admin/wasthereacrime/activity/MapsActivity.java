package com.example.admin.wasthereacrime.activity;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.AsyncTask;
import android.support.design.widget.FloatingActionButton;
import android.os.Bundle;
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
import com.example.admin.wasthereacrime.database.CrimeProvider;
import com.example.admin.wasthereacrime.database.DBOperator;
import com.example.admin.wasthereacrime.database.InsertTask;
import com.example.admin.wasthereacrime.helper.CrimeParserTask;
import com.example.admin.wasthereacrime.helper.DialogHelper;
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

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback,
        LoaderManager.LoaderCallbacks<Cursor>, CrimeParserTask.OnRespondParsedListener {

    private static final String TAG = MapsActivity.class.getSimpleName();

    private static final String EXTRA_CONNECTION_SUCCESS = "connection_success";
    private static final String EXTRA_CHOOSING_LOCATION = "choosing_location";
    private static final String EXTRA_START_DATE = "start_date";
    private static final String EXTRA_END_DATE = "end_date";
    private static final String EXTRA_LATITUDE = "latitude";
    private static final String EXTRA_LONGITUDE = "longitude";

    private static final int RADIUS_IN_METERS = 5000;

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

    private Cursor crimeCursor = null;
    private DBOperator dbOperator = null;
    private boolean connectionSuccess = true;

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
                intent.putExtra(EXTRA_CONNECTION_SUCCESS, connectionSuccess);
                startActivityForResult(intent, 100);
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        if (crimeCursor != null) {
            crimeCursor.close();
        }
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

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMyLocationEnabled(true);

        UiSettings mapSettings = mMap.getUiSettings();
        mapSettings.setZoomControlsEnabled(true);
        mapSettings.setMapToolbarEnabled(true);
        mapSettings.setAllGesturesEnabled(true);

        addChosenLocationMarker();
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                marker.remove();
                latitude = latLng.latitude;
                longitude = latLng.longitude;

                if (!choosingLocation) {
                    getNewCrimeData();
                }

                addChosenLocationMarker();
            }
        });

        if (!choosingLocation) {
            getCrimeDataFromApi();
        }
    }

    private void addChosenLocationMarker() {
        LatLng latLng = new LatLng(latitude, longitude);
        marker = mMap.addMarker(new MarkerOptions().position(latLng).title("Chosen location")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
        marker.setDraggable(true);
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10));
    }

    public void markCrimesOnMap() {
        LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
        if (connectionSuccess) {
            for (Crime c : app.getCrimes()) {
                addMarkerAndCoords(c, boundsBuilder);
            }
        } else {
            crimeCursor.moveToFirst();
            while (!crimeCursor.isAfterLast()) {
                LatLng cLatLng = dbOperator.getCrimeLatLngFromCursor(crimeCursor);
                if (Crime.isCrimeInRadius(new LatLng(latitude, longitude), cLatLng,
                        RADIUS_IN_METERS)) {
                    Crime c = dbOperator.getCrimeFromCursor(crimeCursor);
                    addMarkerAndCoords(c, boundsBuilder);
                }
                crimeCursor.moveToNext();
            }
        }
        if (app.getCrimes().size() > 0 || coordinates.size() > 0) {
            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), 30));
        } else {
            DialogHelper.showAlertDialog(MapsActivity.this, "No crimes found",
                    "No crimes found in chosen area");
        }
    }

    private void addMarkerAndCoords(Crime c, LatLngBounds.Builder boundsBuilder) {
        Marker m = addMarker(c);
        boundsBuilder.include(m.getPosition());
        coordinates.add(c.getLatLng());
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

        StringRequest crimeDataRequest = new StringRequest(Request.Method.GET, apiUrl,
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
                        dbOperator = new DBOperator();
                        DialogHelper.showAlertDialog(MapsActivity.this, "Connection failed",
                                "Connection with remote API failed. Getting crime data from DB if it exists");

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
        new CrimeParserTask(MapsActivity.this).execute(response);
    }

    private String prepareUrl() {
        return StringParser.prepareCrimeApiUrl(baseUrl, startDate, endDate, String.valueOf(latitude),
                String.valueOf(longitude));
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {};
        String selection = CrimeProvider.CrimeColumns.COL_DATE + " BETWEEN ? AND ?";
        String[] selectionArgs = new String[]{StringParser.formatDateAsJulian(startDate),
                StringParser.formatDateAsJulian(endDate)};
        return new CursorLoader(MapsActivity.this, CrimeProvider.CONTENT_URI, projection, selection,
                selectionArgs, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        crimeCursor = data;
        Log.d(TAG, "Crimes retrieved: " + crimeCursor.getCount());
        markCrimesOnMap();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        crimeCursor = null;
    }

    @Override
    public void onRespondParsed(ArrayList<Crime> crimes) {
        app.addCrimes(crimes);
        new InsertTask(MapsActivity.this).execute(app.getCrimes());

        markCrimesOnMap();
    }
}
