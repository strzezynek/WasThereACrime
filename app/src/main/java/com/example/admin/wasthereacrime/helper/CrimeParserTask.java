package com.example.admin.wasthereacrime.helper;

import android.content.Context;
import android.os.AsyncTask;

import com.example.admin.wasthereacrime.WasThereACrimeApp;
import com.example.admin.wasthereacrime.activity.MapsActivity;
import com.example.admin.wasthereacrime.database.InsertTask;
import com.example.admin.wasthereacrime.model.Crime;

import java.util.ArrayList;

public class CrimeParserTask extends AsyncTask<String, Integer, ArrayList<Crime>> {

    private Context context;
    private OnRespondParsedListener respondParsedListener;

    public CrimeParserTask(Context context) {
        this.context = context;
    }

    @Override
    protected ArrayList<Crime> doInBackground(String... params) {
        return StringParser.parseCrimeApiResponse(params[0]);
    }

    @Override
    protected void onPostExecute(ArrayList<Crime> crimes) {
        respondParsedListener = (MapsActivity) context;
        respondParsedListener.onRespondParsed(crimes);
    }

    public interface OnRespondParsedListener {
        void onRespondParsed(ArrayList<Crime> crimes);
    }
}
