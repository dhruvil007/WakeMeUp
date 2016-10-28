package com.catacomblabs.wakemeup;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

public class MainMenuRailway extends AppCompatActivity {

    public static WakeUpLocation wakeUpLocation;
    public AlertDialog.Builder builder;
    public boolean dontShowIntro;
    public static boolean fromListActivity = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);
        dontShowIntro = getIntent().getBooleanExtra("dontShowIntro", false);
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                SharedPreferences getPrefs = PreferenceManager
                        .getDefaultSharedPreferences(getBaseContext());
                boolean isFirstStart = getPrefs.getBoolean("firstStart", true);
                if (isFirstStart && !dontShowIntro && ! fromListActivity) {
                    Intent i = new Intent(MainMenuRailway.this, IntroActivity.class);
                    startActivity(i);

                    SharedPreferences.Editor e = getPrefs.edit();
                    e.putBoolean("firstStart", false);
                    e.apply();
                }
            }
        });

        t.start();

        Toolbar toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        BackgroundLocationCheckService.alarmRung = false;

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (sharedPreferences.getBoolean("check_box_intro", false)) {
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
            SharedPreferences.Editor editor = sp.edit();
            editor.putBoolean("firstStart", true);
            editor.apply();
        }

        wakeUpLocation = new WakeUpLocation();

        /*if ((ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }*/

        checkForGPS();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        else if (id == R.id.intro_button) {
            Intent introIntent = new Intent(this, IntroActivity.class);
            introIntent.putExtra("startedFromMenu", true);
            startActivity(introIntent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    Toast.makeText(this, "Fine Location ", Toast.LENGTH_SHORT).show();
                break;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    public void sendToListStationsW(View v) {
        Intent intent = new Intent(this, ListStationsWestern.class);
        startActivity(intent);
    }

    public void sendToLineSelectH(View v) {
        Intent intent = new Intent(this, ListStationsHarbour.class);
        startActivity(intent);
    }

    public void sendToListStationsC(View v) {
        Intent intent = new Intent(this, ListStationsCentral.class);
        startActivity(intent);
    }

    public void checkForGPS() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        boolean GPSEnabled = false;
        boolean NetworkEnabled = false;

        try {
            GPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception e) {
        }

        try {
            NetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception e) {
        }

        if (!GPSEnabled || !NetworkEnabled) {
            builder = new AlertDialog.Builder(this);
            builder.setTitle("Location Services Not Available");
            builder.setMessage("Please enable location services.");
            builder.setPositiveButton("ENABLE", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent LocationSettingsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(LocationSettingsIntent);
                }
            });
            builder.show();
        }
    }

    public void blankOnClick(View v) {
    }

}
