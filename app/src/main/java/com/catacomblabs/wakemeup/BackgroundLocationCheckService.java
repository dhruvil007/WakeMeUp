package com.catacomblabs.wakemeup;

import android.Manifest;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

public class BackgroundLocationCheckService extends IntentService implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    public static final String BGSERVICE_TAG = "BgLocService";
    public static boolean alarmRung = false;
    public static GoogleApiClient mGoogleApiClient;
    public AlertDialog.Builder builder;
    private LocationRequest mLocationRequest;
    private Location lastLocation;
    public static PendingIntent locationIntent;

    public BackgroundLocationCheckService() {
        super("BackgroundLocationCheckService");
    }

    @Override
    public void onCreate() {
        buildGoogleApiClient();
        mGoogleApiClient.connect();
        //buildLocationRequest();
        super.onCreate();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.w("IntentService", "Started");
        if (mGoogleApiClient != null) {
            LocationReceiver.mGoogleApiClient = mGoogleApiClient;
            LocationReceiver.locationIntent = locationIntent;
        }
        //wbuildLocationRequest();
    }

    @Override
    public void onStart(Intent intent, int startId) {
        if (!mGoogleApiClient.isConnected())
            mGoogleApiClient.connect();
        super.onStart(intent, startId);
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.e(BGSERVICE_TAG, "Connection Suspended");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e(BGSERVICE_TAG, "Connection Failed");
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ListStationsWestern.cancelAlarm || ListStationsCentral.cancelAlarm || ListAndheri.cancelAlarm || ListPanvel.cancelAlarm) {
            Intent intent = new Intent(getApplicationContext(), LocationReceiver.class);
            locationIntent = PendingIntent.getBroadcast(getApplicationContext(), 1, intent, PendingIntent.FLAG_CANCEL_CURRENT);
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, locationIntent);
            ListStationsWestern.cancelAlarm = ListStationsCentral.cancelAlarm = ListPanvel.cancelAlarm = ListAndheri.cancelAlarm = false;
        } else if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            buildLocationRequest();
            lastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            if (lastLocation == null)
                checkForGPS();
            lastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            Intent intent = new Intent(getApplicationContext(), LocationReceiver.class);
            locationIntent = PendingIntent.getBroadcast(getApplicationContext(), 1, intent, PendingIntent.FLAG_CANCEL_CURRENT);
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, locationIntent);
        } else {
            Toast.makeText(this, "Location permissions are required,", Toast.LENGTH_SHORT).show();
        }
    }

    public synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this.getApplicationContext())
                .addOnConnectionFailedListener(this)
                .addConnectionCallbacks(this)
                .addApi(LocationServices.API)
                .build();
    }

    public void buildLocationRequest() {
        checkForGPS();
        SharedPreferences sharedPreferences = this.getApplicationContext().getSharedPreferences("wakeUpLocation", 0);
        double wakeUpLatitude = Double.parseDouble(sharedPreferences.getString("latitude", "0"));
        double wakeUpLongitude = Double.parseDouble(sharedPreferences.getString("longitude", "0"));
        int intervalMultiplier = 40;

        Log.e("BLCS_Connected", "" + mGoogleApiClient.isConnected());

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
            lastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (lastLocation == null)
            Log.e("BLCS_Location", "Not Found");
        else {
            double firstDistance = haversineDistance(lastLocation.getLatitude(), lastLocation.getLongitude(), wakeUpLatitude, wakeUpLongitude);
            if (firstDistance <= 5)
                intervalMultiplier = 30;
            else if (firstDistance > 5 && firstDistance <= 10)
                intervalMultiplier = 60;
            else if (firstDistance > 10 && firstDistance <= 20)
                intervalMultiplier = 90;
            else if (firstDistance > 20 && firstDistance <= 30)
                intervalMultiplier = 150;
            else if (firstDistance > 30 && firstDistance <= 50)
                intervalMultiplier = 210;
            else intervalMultiplier = 300;
        }
        SharedPreferences thresholdSetter = getSharedPreferences("wakeUpLocation", 0);
        SharedPreferences.Editor editor = thresholdSetter.edit();
        editor.putInt("intervalSet", intervalMultiplier);
        editor.commit();

        Log.e("BLCS_Interval", "" + intervalMultiplier + " seconds");
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setInterval(intervalMultiplier * 1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setFastestInterval(1000);
    }

    public void checkForGPS() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        boolean GPSEnabled = false;
        boolean NetworkEnabled = false;

        try {
            GPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception e) {
            Log.d("GPS Checker:", "Exception caught");
        }

        try {
            NetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception e) {
            Log.d("Network Checker:", "Exception caught");
        }

        if (!GPSEnabled && !NetworkEnabled) {
            builder = new AlertDialog.Builder(this);
            builder.setTitle("Location Services Not Available");
            builder.setMessage("Please enable location services.");
            builder.setPositiveButton("Enable", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent LocationSettingsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(LocationSettingsIntent);
                }
            });
            builder.show();
        }
    }

    private double haversineDistance(double lat1, double lon1, double lat2, double lon2) {
        final double R = 6372.8;
        double dlon = Math.toRadians(lon2 - lon1);
        double dlat = Math.toRadians(lat2 - lat1);
        lat1 = Math.toRadians(lat1);
        lat2 = Math.toRadians(lat2);
        double a = Math.pow((Math.sin(dlat / 2)), 2) + (Math.cos(lat1) * Math.cos(lat2) * Math.pow(Math.sin(dlon), 2));
        double c = 2 * (Math.asin(Math.sqrt(a)));
        return R * c;
    }
}
