package com.example.admin.wasthereacrime.helper;

import android.animation.Animator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.design.widget.FloatingActionButton;
import android.view.View;

import com.example.admin.wasthereacrime.R;
import com.example.admin.wasthereacrime.activity.ChartActivity;
import com.google.android.gms.maps.model.LatLng;

public class HelpNotifier {

    public static void setUpAlarmFabs(final Activity activity, final LatLng latLng) {
        final FloatingActionButton[] fabs = new FloatingActionButton[3];
        fabs[0] = (FloatingActionButton) activity.findViewById(R.id.fab_alarm);
        fabs[1] = (FloatingActionButton) activity.findViewById(R.id.fab_call);
        fabs[2] = (FloatingActionButton) activity.findViewById(R.id.fab_send);
        fabs[0].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!fabs[1].isShown()) {
                    showFabs(fabs[1], fabs[2]);
                    fabs[1].animate().translationX(120).setListener(null);
                    fabs[2].animate().translationX(240).setListener(null);
                } else {
                    fabs[1].animate().translationX(0).setListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animator animation) {
                            showFabs(fabs[1], fabs[2]);
                        }

                        @Override
                        public void onAnimationCancel(Animator animation) {

                        }

                        @Override
                        public void onAnimationRepeat(Animator animation) {

                        }
                    });
                    fabs[2].animate().translationX(0);
                }

            }
        });
        fabs[1].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callAlarmNumber(activity);
                showFabs(fabs[1], fabs[2]);
            }
        });
        fabs[2].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                notifyFriend(activity, latLng);
                showFabs(fabs[1], fabs[2]);
            }
        });
    }

    private static void showFabs(FloatingActionButton fab1, FloatingActionButton fab2) {
        fab1.setVisibility(fab1.isShown() ? View.INVISIBLE : View.VISIBLE);
        fab2.setVisibility(fab2.isShown() ? View.INVISIBLE : View.VISIBLE);
    }

    private static void callAlarmNumber(Context context) {
//        Uri alarmNumber = Uri.parse("tel:" + context.getString(R.string.alarm_number));
//        Intent intent = new Intent(Intent.ACTION_DIAL, alarmNumber);
//        context.startActivity(intent);
        //TODO uncomment above delete below
        context.startActivity(new Intent(context, ChartActivity.class));
    }

    private static void notifyFriend(Context context, LatLng latLng) {
        SharedPreferences preferences = context.getSharedPreferences(
                context.getString(R.string.preferences_file_name), Context.MODE_PRIVATE);

        Uri contactNumber = Uri.parse("sms:" + preferences.getString(
                context.getString(R.string.pref_key_contact_number), ""));

        String text = context.getString(R.string.sms_content);
        Intent intent = new Intent(Intent.ACTION_VIEW, contactNumber);
        intent.putExtra("sms_body", text + " " + latLng.latitude + ", " + latLng.longitude);
        context.startActivity(intent);
    }

}
