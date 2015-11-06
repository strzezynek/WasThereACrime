package com.example.admin.wasthereacrime.activity;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.example.admin.wasthereacrime.R;
import com.example.admin.wasthereacrime.database.CrimeProvider;
import com.example.admin.wasthereacrime.helper.StringParser;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ChartActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = ChartActivity.class.getSimpleName();

    private BarChart crimesChart;
    private boolean showMonths = false;
    private Cursor cursor;

    String maxValueKey = "";
    int maxValue = 0;

    private String[] months = {"January", "February", "March", "April", "May", "June", "July",
                                "August", "September", "October", "November", "December"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        setContentView(R.layout.activity_chart);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        crimesChart = (BarChart) findViewById(R.id.crimes_chart);
        crimesChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM_INSIDE);
        crimesChart.getXAxis().setLabelRotationAngle(90);
        getLoaderManager().initLoader(1, null, this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        menu.add("Days").setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        menu.add("Months").setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        String itemName = item.getTitle().toString();

        if (itemName.equals("Days")) {
            if (showMonths) {
                showMonths = false;
                setData();
            }
        } else if (itemName.equals("Months")) {
            if (!showMonths) {
                showMonths = true;
                setData();
            }
        }

        return false;
    }

    @Override
    protected void onStart() {
        Log.d(TAG, "onStart");
        super.onStart();
    }

    protected void onResume() {
        Log.d(TAG, "onResume");
        super.onResume();
    }

    protected void onPause() {
        Log.d(TAG, "onPause");
        super.onPause();
    }

    protected void onStop() {
        Log.d(TAG, "onStop");
        cursor.close();
        super.onStop();
    }

    protected void onRestart() {
        Log.d(TAG, "onRestart");
        super.onRestart();
    }

    private void setData() {
        Map<String, Integer> map = new HashMap<String, Integer>();

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            String date = cursor.getString(cursor.getColumnIndexOrThrow(
                    CrimeProvider.CrimeColumns.COL_DATE));
            if (showMonths) {
                date = months[Integer.parseInt(StringParser.extractMonth(date))];
            }
            if (map.containsKey(date)) {
                map.put(date, map.get(date)+1);
            } else {
                map.put(date, 1);
            }
            cursor.moveToNext();
        }

        lookForMaxValue(map);

        List<String> xValues = new ArrayList<String>(map.keySet());
        Collections.sort(xValues);

        ArrayList<BarEntry> yValues = new ArrayList<BarEntry>();
        for (int i = 0; i < xValues.size(); i++) {
            yValues.add(new BarEntry(map.get(xValues.get(i)), i));
        }

        BarDataSet set = new BarDataSet(yValues, "Crimes set");
        set.setBarSpacePercent(35f);

        ArrayList<BarDataSet> dataSets = new ArrayList<BarDataSet>();
        dataSets.add(set);

        BarData data = new BarData(xValues, dataSets);
        data.setValueTextSize(10f);

        crimesChart.setData(data);
        crimesChart.invalidate();

        Log.d(TAG, "Max key: " + maxValueKey + " max value: " + maxValue);
        crimesChart.highlightValue(xValues.indexOf(maxValueKey), 0);
    }

    private void lookForMaxValue(Map<String, Integer> map) {

        Iterator it = map.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Integer> pair = (Map.Entry<String, Integer>) it.next();
            String key = pair.getKey();
            int num = map.get(key);
            if (num >= maxValue) {
                maxValueKey = key;
                maxValue = num;
            }
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {CrimeProvider.CrimeColumns.COL_DATE};
        String selection = null;
        String[] selectionArgs = null;
        return new CursorLoader(ChartActivity.this, CrimeProvider.CONTENT_URI, projection, selection, selectionArgs,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        cursor = data;
        setData();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        cursor = null;
    }
}
