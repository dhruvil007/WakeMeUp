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

public class ListPanvel extends AppCompatActivity implements Adapter.ClickListener,
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
    private final String TAG = "com.legionlabs.com.catacomblabs.wakemeup.ListPanvel.TAG";
    private static final String[] names = {
            "CST",
            "Masjid",
            "Sandhurst Road",
            "Dockyard Road",
            "Reay Road",
            "Cotton Green",
            "Sewri",
            "Wadala Road",
            "Guru Tegh Bahadur Nagar",
            "Chunabhatti",
            "Kurla",
            "Tilak Nagar",
            "Chembur",
            "Govandi",
            "Mankhurd",
            "Vashi",
            "Sanpada",
            "Juinagar",
            "Nerul",
            "Seawoods-Darave",
            "CBD Belapur",
            "Khargar",
            "Mansarovar",
            "Khandeshwar",
            "Panvel"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_panvel);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        assert getSupportActionBar() != null;
        getSupportActionBar().setTitle("Panvel");
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

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view_panvel);
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
                    MainMenuRailway.wakeUpLocation.latitude = 18.942641;
                    MainMenuRailway.wakeUpLocation.longitude = 72.835981;
                    break;

                case 1:
                    MainMenuRailway.wakeUpLocation.latitude = 18.952096;
                    MainMenuRailway.wakeUpLocation.longitude = 72.838262;
                    break;

                case 2:
                    MainMenuRailway.wakeUpLocation.latitude = 18.961361;
                    MainMenuRailway.wakeUpLocation.longitude = 72.839933;
                    break;

                case 3:
                    MainMenuRailway.wakeUpLocation.latitude = 18.966391;
                    MainMenuRailway.wakeUpLocation.longitude = 72.844198;
                    break;

                case 4:
                    MainMenuRailway.wakeUpLocation.latitude = 18.977258;
                    MainMenuRailway.wakeUpLocation.longitude = 72.844304;
                    break;

                case 5:
                    MainMenuRailway.wakeUpLocation.latitude = 18.986583;
                    MainMenuRailway.wakeUpLocation.longitude = 72.843265;
                    break;

                case 6:
                    MainMenuRailway.wakeUpLocation.latitude = 18.998933;
                    MainMenuRailway.wakeUpLocation.longitude = 72.854549;
                    break;

                case 7:
                    MainMenuRailway.wakeUpLocation.latitude = 19.016428;
                    MainMenuRailway.wakeUpLocation.longitude = 72.859049;
                    break;

                case 8:
                    MainMenuRailway.wakeUpLocation.latitude = 19.037965;
                    MainMenuRailway.wakeUpLocation.longitude = 72.864329;
                    break;

                case 9:
                    MainMenuRailway.wakeUpLocation.latitude = 19.051619;
                    MainMenuRailway.wakeUpLocation.longitude = 72.869021;
                    break;

                case 10:
                    MainMenuRailway.wakeUpLocation.latitude = 19.065331;
                    MainMenuRailway.wakeUpLocation.longitude = 72.879790;
                    break;

                case 11:
                    MainMenuRailway.wakeUpLocation.latitude = 19.069542;
                    MainMenuRailway.wakeUpLocation.longitude = 72.892012;
                    break;

                case 12:
                    MainMenuRailway.wakeUpLocation.latitude = 19.062587;
                    MainMenuRailway.wakeUpLocation.longitude = 72.901273;
                    break;

                case 13:
                    MainMenuRailway.wakeUpLocation.latitude = 19.055157;
                    MainMenuRailway.wakeUpLocation.longitude = 72.915458;
                    break;

                case 14:
                    MainMenuRailway.wakeUpLocation.latitude = 19.048045;
                    MainMenuRailway.wakeUpLocation.longitude = 72.931646;
                    break;

                case 15:
                    MainMenuRailway.wakeUpLocation.latitude = 19.063056;
                    MainMenuRailway.wakeUpLocation.longitude = 72.998861;
                    break;

                case 16:
                    MainMenuRailway.wakeUpLocation.latitude = 19.066077;
                    MainMenuRailway.wakeUpLocation.longitude = 73.009420;
                    break;

                case 17:
                    MainMenuRailway.wakeUpLocation.latitude = 19.055767;
                    MainMenuRailway.wakeUpLocation.longitude = 73.018209;
                    break;

                case 18:
                    MainMenuRailway.wakeUpLocation.latitude = 19.033280;
                    MainMenuRailway.wakeUpLocation.longitude = 73.018060;
                    break;

                case 19:
                    MainMenuRailway.wakeUpLocation.latitude = 19.021996;
                    MainMenuRailway.wakeUpLocation.longitude = 73.019326;
                    break;

                case 20:
                    MainMenuRailway.wakeUpLocation.latitude = 19.018925;
                    MainMenuRailway.wakeUpLocation.longitude = 73.039184;
                    break;

                case 21:
                    MainMenuRailway.wakeUpLocation.latitude = 19.026086;
                    MainMenuRailway.wakeUpLocation.longitude = 73.058619;
                    break;

                case 22:
                    MainMenuRailway.wakeUpLocation.latitude = 19.016721;
                    MainMenuRailway.wakeUpLocation.longitude = 73.080535;
                    break;

                case 23:
                    MainMenuRailway.wakeUpLocation.latitude = 19.007472;
                    MainMenuRailway.wakeUpLocation.longitude = 73.094726;
                    break;

                case 24:
                    MainMenuRailway.wakeUpLocation.latitude = 18.990913;
                    MainMenuRailway.wakeUpLocation.longitude = 73.120751;
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

        for (String stationName: names) {
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
