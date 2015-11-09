package com.example.admin.wasthereacrime.helper;

import android.content.Context;
import android.support.v7.app.AlertDialog;

public class DialogHelper {

    public static void showAlertDialog(Context context, String title, String message) {
        AlertDialog.Builder dialogBuilder
                = new AlertDialog.Builder(context);
        dialogBuilder.setTitle(title)
                .setMessage(message);
        dialogBuilder.create().show();
    }

}