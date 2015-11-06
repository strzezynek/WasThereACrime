package com.example.admin.wasthereacrime.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.admin.wasthereacrime.R;

public class ContactNumberActivity extends AppCompatActivity {

    private static final String TAG = ContactNumberActivity.class.getSimpleName();

    private EditText numberEdit;
    private Button saveNumberBtn;

    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_number);

        //Retrieving views from layout
        numberEdit = (EditText) findViewById(R.id.contact_number_edit);
        saveNumberBtn = (Button) findViewById(R.id.save_number_btn);
        saveNumberBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isNumberValid()) {
                    saveContactNumber();
                    startNextActivity();
                }
            }
        });

        //Retrieving contact number from shared preferences
        preferences = getSharedPreferences(getString(R.string.preferences_file_name), Context.MODE_PRIVATE);
        String contactNum = preferences.getString(getString(R.string.pref_key_contact_number), null);

        //Setting numberEdit if contactNumber exists
        if (contactNum != null) {
//            startNextActivity();
            numberEdit.setText(contactNum);
        }
    }

    private void saveContactNumber() {
        //Saving contact number in shared preferences
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(getString(R.string.pref_key_contact_number), numberEdit.getText().toString());
        editor.commit();
    }

    private boolean isNumberValid() {
        //Validating number
        //TODO validate the number
        return true;
    }

    private void startNextActivity() {
        //Starting CrimeDetailsActivity
        Intent intent = new Intent(ContactNumberActivity.this, CrimeDetailsActivity.class);
        startActivity(intent);
        finish();
    }
}
