package com.example.admin.wasthereacrime.activity;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.admin.wasthereacrime.R;
import com.example.admin.wasthereacrime.WasThereACrimeApp;
import com.example.admin.wasthereacrime.helper.HelpNotifier;
import com.example.admin.wasthereacrime.helper.StringParser;
import com.example.admin.wasthereacrime.model.Crime;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.places.PlacePhotoMetadata;
import com.google.android.gms.location.places.PlacePhotoMetadataBuffer;
import com.google.android.gms.location.places.PlacePhotoMetadataResult;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class CrimeActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = CrimeActivity.class.getSimpleName();

    private static final double RADIUS_PLACES = 100.0;

    private WasThereACrimeApp app;

    private TextView dateText;
    private TextView latitudeText;
    private TextView longitudeText;
    private ImageView placeImage;

    private LatLng placeCoords;

    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crime);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        app = (WasThereACrimeApp) getApplication();

        Bundle extras = getIntent().getExtras();
        Crime crime = extras.getParcelable("crime");
        getSupportActionBar().setTitle(crime.getDescription());

        dateText = (TextView) findViewById(R.id.crime_date_text);
        latitudeText = (TextView) findViewById(R.id.lat_text);
        longitudeText = (TextView) findViewById(R.id.lng_text);
        placeImage = (ImageView) findViewById(R.id.place_image);

        dateText.setText(crime.getDatetime());

        placeCoords = new LatLng(crime.getLatitude(), crime.getLongitude());

        latitudeText.setText(String.valueOf(crime.getLatitude()));
        longitudeText.setText(String.valueOf(crime.getLongitude()));

        setUpFabs();

        buildGoogleApiClient();
    }

    private synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    private void setUpFabs() {
            HelpNotifier.setUpAlarmFabs(this, app.getMyLocation());
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    private void getPlacePhoto() {
        String searchUrl = StringParser.preparePlacesApiUrl(CrimeActivity.this, placeCoords,
                RADIUS_PLACES);

        new PlacePhotoTask().execute(searchUrl.toString());
    }

    private void parseJSON(String json) {
        try {
            JSONObject jsonObject = new JSONObject(json);
            JSONArray jsonArray = jsonObject.getJSONArray("results");
            JSONObject placeJson = jsonArray.getJSONObject(1);
            String placeId = placeJson.getString("place_id");

            new PhotoTask(placeImage.getWidth(), placeImage.getHeight()) {

                @Override
                protected void onPostExecute(AttributedPhoto attributedPhoto) {
                    if (attributedPhoto != null) {
                        placeImage.setImageBitmap(attributedPhoto.bitmap);
                    }
                }
            }.execute(placeId);

        } catch(JSONException e ){
            e.printStackTrace();
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        getPlacePhoto();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "Error ");
    }

    private class PlacePhotoTask extends AsyncTask<String, Integer, String> {

        @Override
        protected String doInBackground(String... placesUrl) {
            RequestQueue requestQueue = Volley.newRequestQueue(CrimeActivity.this);

            for (String placeUrl : placesUrl) {
                StringRequest placeRequest = new StringRequest(Request.Method.GET, placeUrl,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                parseJSON(response);
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                error.printStackTrace();
                            }
                        }) {

                };

                requestQueue.add(placeRequest);
            }
            return null;
        }
    }

    abstract class PhotoTask extends AsyncTask<String, Void, PhotoTask.AttributedPhoto> {

        private final int mHeight;
        private final int mWidth;

        public PhotoTask(int width, int height) {
            mHeight = height;
            mWidth = width;
        }

        @Override
        protected AttributedPhoto doInBackground(String... params){
            if (params.length != 1) {
                return null;
            }
            final String placeId = params[0];
            AttributedPhoto attributedPhoto = null;

            PlacePhotoMetadataResult result = Places.GeoDataApi
                    .getPlacePhotos(mGoogleApiClient, placeId).await();

            if (result.getStatus().isSuccess()) {
                PlacePhotoMetadataBuffer photoMetadataBuffer = result.getPhotoMetadata();
                if (photoMetadataBuffer.getCount() > 0 && !isCancelled()) {
                    PlacePhotoMetadata photo = photoMetadataBuffer.get(0);
                    CharSequence attribution = photo.getAttributions();
                    Bitmap image = photo.getScaledPhoto(mGoogleApiClient, mWidth, mHeight).await()
                            .getBitmap();

                    attributedPhoto = new AttributedPhoto(attribution, image);
                }
                photoMetadataBuffer.release();
            }
            return attributedPhoto;
        }

        @Override
        protected void onPostExecute(AttributedPhoto photo) {
            if (photo != null) {
                placeImage.setImageBitmap(photo.bitmap);
            }
        }

        protected class AttributedPhoto {
            public final CharSequence attribution;

            public final Bitmap bitmap;

            public AttributedPhoto(CharSequence attribution, Bitmap bitmap) {
                this.attribution = attribution;
                this.bitmap = bitmap;
            }
        }
    }
}
