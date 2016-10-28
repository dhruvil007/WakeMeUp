package com.catacomblabs.wakemeup;

import android.Manifest;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class ListTransHarbour extends AppCompatActivity implements Adapter.ClickListener,
        SelectorDialog.SelectorDialogListener,
        SearchView.OnQueryTextListener {

    private boolean alarmRinging;
    public static boolean cancelAlarm;
    private RecyclerView recyclerView;
    private List<Stations> dataNames;
    private Adapter adapter;
    private int stationNumber;
    private boolean isPanelShowing = false;
    private FloatingActionButton fab;
    private ViewGroup hiddenPanel;
    private RelativeLayout hiddenPanelBackground;
    private String stationName;
    private TextView currentAlarmText;
    public AlertDialog.Builder builder;
    private final String TAG = "com.legionlabs.com.catacomblabs.wakemeup.ListAndheri.TAG";
    private static final String[] names = {
            "Thane",
            "Airoli",
            "Rabale",
            "Ghansoli",
            "Kopar Khairane",
            "Turbhe",
            "Juinagar",
            "Nerul"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_trans_harbour);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        assert getSupportActionBar() != null;
        getSupportActionBar().setTitle("Trans-Harbour");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        final Context context = this;
        hiddenPanel = (ViewGroup) findViewById(R.id.hidden_panel);
        hiddenPanelBackground = (RelativeLayout) findViewById(R.id.hidden_panel_background);
        hiddenPanelBackground.setClickable(false);
        hiddenPanelBackground.setVisibility(View.GONE);
        fab = (FloatingActionButton) findViewById(R.id.current_alarm_open_fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isPanelShowing) {
                    showPanel();
                } else {
                    hidePanel();
                }
            }
        });

        builder = new AlertDialog.Builder(this);

        Button cancelAlarmButton = (Button) hiddenPanel.findViewById(R.id.cancel_alarm_button);
        cancelAlarmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentAlarmText.getText().toString().equals("No station currently selected"))
                    Toast.makeText(context, "No alarm created to be canceled", Toast.LENGTH_SHORT).show();
                else {
                    SelectorDialog dialog = new SelectorDialog();
                    SelectorDialog.cancelAlarm = true;
                    dialog.show(getFragmentManager(), "Dialog");
                }
            }
        });

        isPanelShowing = false;

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view_trans_harbour);
        dataNames = getDataNames();
        adapter = new Adapter(this, dataNames);
        adapter.setClickListener(this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_list_stations_western, menu);

        final MenuItem item = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);
        searchView.setQueryHint("Search");
        searchView.setOnQueryTextListener(this);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onBackPressed() {
        if (isPanelShowing) {
            hidePanel();
        } else
            super.onBackPressed();
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        final List<Stations> filteredModelList = filter(dataNames, newText);
        adapter.animateTo(filteredModelList);
        recyclerView.scrollToPosition(0);
        return true;
    }

    private List<Stations> filter(List<Stations> models, String query) {
        query = query.toLowerCase();

        final List<Stations> filteredModelList = new ArrayList<>();
        for (Stations model : models) {
            final String text = model.getText().toLowerCase();
            if (text.contains(query)) {
                filteredModelList.add(model);
            }
        }
        return filteredModelList;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }


    @Override
    public void itemClicked(View view, int position) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 2);
            Toast.makeText(this, "You may now set an alarm", Toast.LENGTH_SHORT).show();
        } else {
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

            if (GPSEnabled || NetworkEnabled) {
                if (position == stationNumber) {
                    alarmRinging = false;
                } else {
                    stationNumber = position;
                }
                TextView textView = (TextView) view.findViewById(R.id.text_view_list);
                stationName = textView.getText().toString();

                SelectorDialog dialog = new SelectorDialog();
                dialog.getName(stationName);
                dialog.show(getFragmentManager(), TAG);
            } else {
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
    }

    @Override
    public void onDialogNegative(DialogFragment dialog) {
        if (!SelectorDialog.cancelAlarm)
            Toast.makeText(this, "Canceled", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDialogPositive(DialogFragment dialog) {
        if (!SelectorDialog.cancelAlarm) {
            Toast.makeText(this, "Alarm set", Toast.LENGTH_SHORT).show();
            MainMenuRailway.wakeUpLocation.name = stationName;
            int loop;
            for (loop = 0; loop < names.length; loop++)
                if (names[loop].equals(stationName))
                    break;
            stationNumber = loop;
            switch (stationNumber) {
                case 0:
                    MainMenuRailway.wakeUpLocation.latitude = 19.185973;
                    MainMenuRailway.wakeUpLocation.longitude = 72.975774;
                    break;

                case 1:
                    MainMenuRailway.wakeUpLocation.latitude = 19.158556;
                    MainMenuRailway.wakeUpLocation.longitude = 72.999389;
                    break;

                case 2:
                    MainMenuRailway.wakeUpLocation.latitude = 19.136640;
                    MainMenuRailway.wakeUpLocation.longitude = 73.002801;
                    break;

                case 3:
                    MainMenuRailway.wakeUpLocation.latitude = 19.116481;
                    MainMenuRailway.wakeUpLocation.longitude = 73.006885;
                    break;

                case 4:
                    MainMenuRailway.wakeUpLocation.latitude = 19.103229;
                    MainMenuRailway.wakeUpLocation.longitude = 73.011269;
                    break;

                case 5:
                    MainMenuRailway.wakeUpLocation.latitude = 19.076028;
                    MainMenuRailway.wakeUpLocation.longitude = 73.017698;
                    break;

                case 6:
                    MainMenuRailway.wakeUpLocation.latitude = 19.056082;
                    MainMenuRailway.wakeUpLocation.longitude = 73.018243;
                    break;

                case 7:
                    MainMenuRailway.wakeUpLocation.latitude = 19.033352;
                    MainMenuRailway.wakeUpLocation.longitude = 73.018050;
                    break;
            }

            SharedPreferences sharedPreferences = getSharedPreferences("wakeUpLocation", 0);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("name", MainMenuRailway.wakeUpLocation.name);
            editor.putString("latitude", "" + MainMenuRailway.wakeUpLocation.latitude);
            editor.putString("longitude", "" + MainMenuRailway.wakeUpLocation.longitude);
            editor.commit();

            if (!BackgroundLocationCheckService.alarmRung) {
                Intent intent = new Intent(this, BackgroundLocationCheckService.class);
                startService(intent);
            }
        } else {
            SharedPreferences sharedPreferences = getSharedPreferences("wakeUpLocation", 0);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            WakeUpActivity.cancelAlarm(editor);
            SelectorDialog.cancelAlarm = false;
            currentAlarmText.setText("No station currently selected");
            hidePanel();
        }
    }

    public List<Stations> getDataNames() {
        List<Stations> data = new ArrayList<>();

        for (String stationName : names) {
            Stations current = new Stations();
            current.name = stationName;
            data.add(current);
        }
        return data;
    }

    public void hidePanel() {
        Animation bottomDown = AnimationUtils.loadAnimation(this,
                R.anim.bottom_down);
        Animation rotateFABBackward = AnimationUtils.loadAnimation(this,
                R.anim.rotate_fab_backward);
        Animation makeTransparent = AnimationUtils.loadAnimation(this,
                R.anim.make_transparent);
        hiddenPanel.startAnimation(bottomDown);
        hiddenPanel.setVisibility(View.GONE);
        hiddenPanelBackground.startAnimation(makeTransparent);
        hiddenPanelBackground.setVisibility(View.GONE);
        hiddenPanelBackground.setOnClickListener(null);
        hiddenPanelBackground.setClickable(false);
        fab.startAnimation(rotateFABBackward);
        isPanelShowing = false;
    }

    public void showPanel() {
        Animation bottomUp = AnimationUtils.loadAnimation(this,
                R.anim.bottom_up);
        Animation rotateFABForward = AnimationUtils.loadAnimation(this,
                R.anim.rotate_fab_forward);
        Animation makeOpaque = AnimationUtils.loadAnimation(this,
                R.anim.make_opaque);
        isPanelShowing = true;
        hiddenPanelBackground.startAnimation(makeOpaque);
        hiddenPanel.startAnimation(bottomUp);
        fab.startAnimation(rotateFABForward);
        hiddenPanel.setVisibility(View.VISIBLE);
        hiddenPanelBackground.setVisibility(View.VISIBLE);
        hiddenPanel.setAlpha(1.0f);
        hiddenPanelBackground.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });
        final SharedPreferences sharedPreferences = getSharedPreferences("wakeUpLocation", 0);
        currentAlarmText = (TextView) hiddenPanel.findViewById(R.id.current_alarm_text);
        String currentAlarm = sharedPreferences.getString("name", "No station currently selected");
        String noAlarm = "No station currently selected";
        if (currentAlarm.equals(noAlarm) || currentAlarm.equals("noAlarm")) {
            currentAlarmText.setText(noAlarm);
        } else {
            currentAlarm = "Current Alarm: " + currentAlarm;
            currentAlarmText.setText(currentAlarm);
        }
    }
}
