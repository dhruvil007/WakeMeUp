package com.catacomblabs.wakemeup;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

public class ListStationsHarbour extends AppCompatActivity {

    public static final String EXTRA_MESSAGE = "com.legionlabs.com.catacomblabs.wakemeup.MESSAGE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_stations_harbour);

        Toolbar toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);

        assert getSupportActionBar() != null;
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Harbour");

        MainMenuRailway.fromListActivity = true;
    }

    public void sendToListStationsHPanvel(View v) {
        Intent intent = new Intent(this, ListPanvel.class);
        String q = "Panvel Line";
        intent.putExtra(EXTRA_MESSAGE, q);
        startActivity(intent);
    }

    public void sendToListStationsHAndheri(View v) {
        Intent intent2 = new Intent(this, ListAndheri.class);
        String q2 = "Andheri Line";
        intent2.putExtra(EXTRA_MESSAGE, q2);
        startActivity(intent2);
    }

    public void sendToListStationsHTransHarbour(View v) {
        Intent intent = new Intent(this, ListTransHarbour.class);
        startActivity(intent);
    }
}