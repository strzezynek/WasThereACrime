package com.example.admin.wasthereacrime.database;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.example.admin.wasthereacrime.model.Crime;

import java.util.ArrayList;

public class InsertTask extends AsyncTask<ArrayList<Crime>, Integer, Integer> {

    private static final String TAG = InsertTask.class.getSimpleName();

    private Context context;
    private DBOperator dbOperator;

    public InsertTask(Context context) {
        this.context = context;
    }

    @Override
    protected Integer doInBackground(ArrayList<Crime>... params) {
        ArrayList<Crime> crimes = params[0];
        int crimesInserted = 0;
        dbOperator = new DBOperator();
        for (Crime c : crimes) {
            if (dbOperator.isCrimeUnique(context, c)) {
                Uri uri = context.getContentResolver().insert(CrimeProvider.CONTENT_URI,
                        dbOperator.getContentValues(c));
                if (uri != null) {
                    crimesInserted++;
                }
            } else if (!dbOperator.isCrimeUpToDate(context, c)) {
                dbOperator.updateCrime(context, c);
            }
        }
        return crimesInserted;
    }

    @Override
    protected void onPostExecute(Integer crimesNum) {
        Log.d(TAG, "Crimes inserted into DB: " + crimesNum);
    }
}