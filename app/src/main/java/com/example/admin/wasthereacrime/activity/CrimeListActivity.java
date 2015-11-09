package com.example.admin.wasthereacrime.activity;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.admin.wasthereacrime.R;
import com.example.admin.wasthereacrime.WasThereACrimeApp;
import com.example.admin.wasthereacrime.adapter.CrimeAdapter;
import com.example.admin.wasthereacrime.adapter.CrimeCursorAdapter;
import com.example.admin.wasthereacrime.database.CrimeProvider;
import com.example.admin.wasthereacrime.helper.HelpNotifier;
import com.example.admin.wasthereacrime.model.Crime;

import java.util.ArrayList;
import java.util.List;

public class CrimeListActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = CrimeListActivity.class.getSimpleName();

    private static final String EXTRA_CONNECTION_SUCCESS = "connection_success";
    private static final String EXTRA_CRIME = "crime";

    private WasThereACrimeApp app;

    private ListView crimeListView;
    private Cursor cursor = null;

    boolean connectionSuccess = true;

    private List<Crime> crimeList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crime_list);

        app = (WasThereACrimeApp) getApplication();
        crimeList = app.getCrimes();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        connectionSuccess = getIntent().getBooleanExtra(EXTRA_CONNECTION_SUCCESS, true);

        crimeListView = (ListView) findViewById(R.id.crime_list);
        if (connectionSuccess) {
            CrimeAdapter crimeAdapter = new CrimeAdapter(this, R.layout.item_crime, crimeList);
            crimeListView.setAdapter(crimeAdapter);
            crimeAdapter.notifyDataSetChanged();
        } else {
            getLoaderManager().initLoader(1, null, this);
        }

        crimeListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(CrimeListActivity.this, CrimeActivity.class);
                //TODO pass Crime object (DONE)
                if (connectionSuccess) {
                    intent.putExtra(EXTRA_CRIME, crimeList.get(position));
                } else {
                    cursor.moveToPosition(position);
                    intent.putExtra(EXTRA_CRIME, Crime.getCrimeFromCursor(cursor));
                }
                startActivity(intent);
            }
        });

        setUpFabs();
    }

    private void setUpFabs() {
        HelpNotifier.setUpAlarmFabs(this, app.getMyLocation());
    }

    public void onStop() {
        super.onStop();
        if (cursor != null) {
            cursor.close();
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(CrimeListActivity.this, CrimeProvider.CONTENT_URI, null, null,
                null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        cursor = data;
        CrimeCursorAdapter crimeCursorAdapter = new CrimeCursorAdapter(CrimeListActivity.this,
                cursor);
        crimeListView.setAdapter(crimeCursorAdapter);
        crimeCursorAdapter.notifyDataSetChanged();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        cursor = null;
    }
}
