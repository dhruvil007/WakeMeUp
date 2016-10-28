package com.catacomblabs.wakemeup;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

public class WakeUpActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private boolean alarmRinging;
    private static boolean cancel = false;
    private Context context;
    private static GoogleApiClient mGoogleApiClient;
    private static PendingIntent locationIntent;

    @Override
    public void onBackPressed() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED + WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

        if (BackgroundLocationCheckService.mGoogleApiClient == null) {
            buildGoogleApiClient();
            mGoogleApiClient.connect();
            Intent intent = new Intent(getApplicationContext(), LocationReceiver.class);
            locationIntent = PendingIntent.getBroadcast(getApplicationContext(), 1, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        } else {
            mGoogleApiClient = BackgroundLocationCheckService.mGoogleApiClient;
            locationIntent = BackgroundLocationCheckService.locationIntent;
        }

        if (mGoogleApiClient == null) {
            buildGoogleApiClient();
            mGoogleApiClient.connect();
        }

        setContentView(R.layout.activity_wake_up);

        context = this;

        SharedPreferences sharedPreferences2 = getSharedPreferences("wakeUpLocation", 0);
        String name = sharedPreferences2.getString("name", "your desitination");

        if (name.equals("No station currently selected"))
            name = "your destination";

        TextView locationText = (TextView) findViewById(R.id.location_text);
        name = "You will reach " + name + " soon";
        locationText.setText(name);
        final Ringtone ringtone;
        if (getRingtone().equals("default")) {
            Uri alarmRing;
            alarmRing = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            if (alarmRing == null)
                alarmRing = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
            ringtone = RingtoneManager.getRingtone(getApplicationContext(), alarmRing);
        } else {
            Uri ringtoneUri = Uri.parse(getRingtone());
            ringtone = RingtoneManager.getRingtone(this, ringtoneUri);
        }

        if (Build.VERSION.SDK_INT <= 21) {
            ringtone.setStreamType(AudioManager.STREAM_ALARM);
        } else {
            AudioAttributes.Builder builder = new AudioAttributes.Builder();
            builder.setUsage(AudioAttributes.USAGE_ALARM);
            ringtone.setAudioAttributes(builder.build());
        }
        if (!ringtone.isPlaying())
            ringtone.play();
        alarmRinging = true;

        final Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        long[] pattern = {0, 900, 1000};
        vibrator.vibrate(pattern, 0);

        if (Build.VERSION.SDK_INT >= 21) {
            Window window = getWindow();
            window.setNavigationBarColor(ContextCompat.getColor(this, R.color.colorPrimary));
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimary));
        }


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setBackgroundColor(Color.parseColor("#F44336"));
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ringtone.isPlaying())
                    ringtone.stop();
                vibrator.cancel();
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_HOME);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.putExtra("dontShowIntro", true);
                startActivity(intent);
            }
        });
        SharedPreferences sharedPreferences = getSharedPreferences("wakeUpLocation", 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        cancelAlarm(editor);
    }

    private String getRingtone() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        return sharedPreferences.getString("ringtone_picker", "default");
    }

    public static void cancelAlarm(SharedPreferences.Editor editor) {
        editor.putString("name", "No station currently selected");
        editor.putString("latitude", "000000");
        editor.putString("longitude", "000000");
        editor.commit();
        cancel = true;
        LocationReceiver.alarmRung = true;
    }

    public synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this.getApplicationContext())
                .addOnConnectionFailedListener(this)
                .addConnectionCallbacks(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (cancel) {
            if (mGoogleApiClient.isConnected())
                LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, locationIntent);
            else {
                mGoogleApiClient.connect();
                LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, locationIntent);
            }
            cancel = false;
        }
    }
}