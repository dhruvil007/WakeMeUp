package com.catacomblabs.wakemeup;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

public class LocationReceiver extends BroadcastReceiver implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private Context context;
    public double currentLatitude;
    public double currentLongitude;
    public static boolean alarmRung = false;
    public static GoogleApiClient mGoogleApiClient;
    public static PendingIntent locationIntent;
    public double distanceThreshold;
    private boolean cancel;

    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;

        SharedPreferences sharedPreferences = context.getSharedPreferences("wakeUpLocation", 0);
        int interval = sharedPreferences.getInt("intervalSet", 30);
        if (interval == 30)
            distanceThreshold = 1.5;
        else if (interval == 60)
            distanceThreshold = 2.0;
        else if (interval == 90)
            distanceThreshold = 3.0;
        else if (interval == 150)
            distanceThreshold = 4.0;
        else if (interval == 210)
            distanceThreshold = 4.5;
        else if (interval == 300)
            distanceThreshold = 5.0;
        else
            distanceThreshold = 6.0;

        Bundle bundle = intent.getExtras();
        for (String key : bundle.keySet()) {
            try {
                if (key.equals("com.google.android.gms.location.EXTRA_LOCATION_RESULT")) {
                    LocationResult locationResult = (LocationResult) bundle.get(key);
                    Location location = locationResult.getLastLocation();
                    handleNewLocation(location);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (BackgroundLocationCheckService.mGoogleApiClient != null) {
            mGoogleApiClient = BackgroundLocationCheckService.mGoogleApiClient;
            locationIntent = BackgroundLocationCheckService.locationIntent;
        }
    }

    public void handleNewLocation(Location location) {
        SharedPreferences sharedPreferences = context.getApplicationContext().getSharedPreferences("wakeUpLocation", 0);
        if (sharedPreferences.getBoolean("existingName", false))
            LocationServices.FusedLocationApi.removeLocationUpdates(BackgroundLocationCheckService.mGoogleApiClient,
                    BackgroundLocationCheckService.locationIntent);

        currentLatitude = location.getLatitude();
        currentLongitude = location.getLongitude();

        double wakeUpLatitude = Double.parseDouble(sharedPreferences.getString("latitude", "0"));
        double wakeUpLongitude = Double.parseDouble(sharedPreferences.getString("longitude", "0"));
        double wakeUpDistance = haversineDistance(currentLatitude, currentLongitude, wakeUpLatitude, wakeUpLongitude);
        Log.e("LR", "Distnce = " + wakeUpDistance);
        if (wakeUpDistance == 0.0 || alarmRung || wakeUpDistance > 10000) {
            alarmRung = false;
            if (mGoogleApiClient == null) {
                buildGoogleApiClient();
                mGoogleApiClient.connect();

                Intent intent = new Intent(context.getApplicationContext(), LocationReceiver.class);
                locationIntent = PendingIntent.getBroadcast(context.getApplicationContext(), 1, intent, PendingIntent.FLAG_CANCEL_CURRENT);
                cancel = true;
            } else {
                LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, locationIntent);
            }
        }
        if (wakeUpDistance < distanceThreshold && !alarmRung && wakeUpDistance != 0.0) {
            if (BackgroundLocationCheckService.mGoogleApiClient != null)
                LocationServices.FusedLocationApi.removeLocationUpdates(BackgroundLocationCheckService.mGoogleApiClient,
                        BackgroundLocationCheckService.locationIntent);
            else {
                cancel = true;
            }
            Intent intent = new Intent(context, WakeUpActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            context.startActivity(intent);
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

    public synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(context.getApplicationContext())
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
    public void onConnected(@Nullable Bundle bundle) {
        if (cancel) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient,
                    locationIntent);
            cancel = false;
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }
}
