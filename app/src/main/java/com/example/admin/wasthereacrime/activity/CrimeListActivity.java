package com.example.admin.wasthereacrime.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.admin.wasthereacrime.R;
import com.example.admin.wasthereacrime.WasThereACrimeApp;
import com.example.admin.wasthereacrime.adapter.CrimeAdapter;
import com.example.admin.wasthereacrime.helper.HelpNotifier;
import com.example.admin.wasthereacrime.model.Crime;

import java.util.ArrayList;
import java.util.List;

public class CrimeListActivity extends AppCompatActivity {

    private static final String TAG = CrimeListActivity.class.getSimpleName();

    private WasThereACrimeApp app;

    private ListView crimeListView;
    private CrimeAdapter crimeAdapter;

    private List<Crime> crimeList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crime_list);

        app = (WasThereACrimeApp) getApplication();
        crimeList = app.getCrimes();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        crimeListView = (ListView) findViewById(R.id.crime_list);
        crimeAdapter = new CrimeAdapter(this, R.layout.item_crime, crimeList);
        crimeListView.setAdapter(crimeAdapter);
        crimeAdapter.notifyDataSetChanged();
        crimeListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(CrimeListActivity.this, CrimeActivity.class);
                //TODO pass Crime object
                intent.putExtra("crime", crimeList.get(position));
                startActivity(intent);
            }
        });

        setUpFabs();
    }

    private void setUpFabs() {
        HelpNotifier.setUpAlarmFabs(this, app.getMyLocation());
    }
}
