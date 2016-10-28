package com.catacomblabs.wakemeup;

import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

public class CancelService extends IntentService implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static GoogleApiClient mGoogleApiClient;
    private static PendingIntent locationIntent;

    public CancelService() {
        super("CancelService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (!mGoogleApiClient.isConnected())
            mGoogleApiClient.connect();
    }

    @Override
    public void onCreate() {
        if (BackgroundLocationCheckService.mGoogleApiClient != null)
            mGoogleApiClient = BackgroundLocationCheckService.mGoogleApiClient;
        else
            buildGoogleApiClient();
        super.onCreate();
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
        Log.e("Cancel Service", "Connected");
        Intent intent = new Intent(getApplicationContext(), LocationReceiver.class);
        locationIntent = PendingIntent.getBroadcast(getApplicationContext(), 1, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        if (mGoogleApiClient.isConnected())
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, locationIntent);
        else {
            mGoogleApiClient.connect();
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, locationIntent);
        }
        Log.e("CancelService", "Canceled");
    }

    public synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this.getApplicationContext())
                .addOnConnectionFailedListener(this)
                .addConnectionCallbacks(this)
                .addApi(LocationServices.API)
                .build();
    }
}
