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

public class ListStationsCentral extends AppCompatActivity implements Adapter.ClickListener,
        SelectorDialog.SelectorDialogListener,
        SearchView.OnQueryTextListener {

    private boolean alarmRinging;
    public static boolean cancelAlarm;
    private RecyclerView recyclerView;
    private List<Stations> dataName;
    private Adapter adapter;
    private int stationNumber;
    private boolean isPanelShowing = false;
    private FloatingActionButton fab;
    private ViewGroup hiddenPanel;
    private RelativeLayout hiddenPanelBackground;
    private String stationName;
    private TextView currentAlarmText;
    public AlertDialog.Builder builder;
    private final String TAG = "com.legionlabs.com.catacomblabs.wakemeup.ListStationsCentral.TAG";
    public static final String[] names = {
            "CST",
            "Masjid",
            "Sandhurst Road",
            "Byculla",
            "Chinchpokli",
            "Currey Road",
            "Parel",
            "Dadar",
            "Matunga",
            "Sion",
            "Kurla",
            "Vidyavihar",
            "Ghatkopar",
            "Vikhroli",
            "Kanjur Marg",
            "Bhandup",
            "Nahur",
            "Mulund",
            "Thane",
            "Kalva",
            "Mumbra",
            "Diva",
            "Kopar",
            "Dombivli",
            "Thakurli",
            "Kalyan",
            "Vithalwadi",
            "Ulhas Nagar",
            "Ambernath",
            "Badlapur",
            "Vangani",
            "Shelu",
            "Neral",
            "Bhivpuri Road",
            "Karjat",
            "Palasdhari",
            "Kelavli",
            "Dolavli",
            "Lowjee",
            "Khopoli",
            "Shahad",
            "Ambivli",
            "Titwala",
            "Khadavli",
            "Vasind",
            "Asangaon",
            "Atgaon",
            "Khardi",
            "Kasara"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_stations_central);

        SharedPreferences sharedPreferences = getSharedPreferences("wakeUpLocation", 0);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        assert getSupportActionBar() != null;
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Central");

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

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view_central);
        dataName = getDataNames();
        adapter = new Adapter(this, dataName);
        adapter.setClickListener(this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        MainMenuRailway.fromListActivity = true;
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
        final List<Stations> filteredModelList = filter(dataName, newText);
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
                    MainMenuRailway.wakeUpLocation.latitude = 18.976635;
                    MainMenuRailway.wakeUpLocation.longitude = 72.832640;
                    break;

                case 4:
                    MainMenuRailway.wakeUpLocation.latitude = 18.986967;
                    MainMenuRailway.wakeUpLocation.longitude = 72.832796;
                    break;

                case 5:
                    MainMenuRailway.wakeUpLocation.latitude = 18.994089;
                    MainMenuRailway.wakeUpLocation.longitude = 72.832860;
                    break;

                case 6:
                    MainMenuRailway.wakeUpLocation.latitude = 19.009123;
                    MainMenuRailway.wakeUpLocation.longitude = 72.837645;
                    break;

                case 7:
                    MainMenuRailway.wakeUpLocation.latitude = 19.018059;
                    MainMenuRailway.wakeUpLocation.longitude = 72.843653;
                    break;

                case 8:
                    MainMenuRailway.wakeUpLocation.latitude = 19.027432;
                    MainMenuRailway.wakeUpLocation.longitude = 72.850208;
                    break;

                case 9:
                    MainMenuRailway.wakeUpLocation.latitude = 19.046540;
                    MainMenuRailway.wakeUpLocation.longitude = 72.863223;
                    break;

                case 10:
                    MainMenuRailway.wakeUpLocation.latitude = 19.065414;
                    MainMenuRailway.wakeUpLocation.longitude = 72.879768;
                    break;

                case 11:
                    MainMenuRailway.wakeUpLocation.latitude = 19.079539;
                    MainMenuRailway.wakeUpLocation.longitude = 72.897590;
                    break;

                case 12:
                    MainMenuRailway.wakeUpLocation.latitude = 19.111911;
                    MainMenuRailway.wakeUpLocation.longitude = 72.928220;
                    break;

                case 13:
                    MainMenuRailway.wakeUpLocation.latitude = 19.128873;
                    MainMenuRailway.wakeUpLocation.longitude = 72.928119;
                    break;

                case 14:
                    MainMenuRailway.wakeUpLocation.latitude = 19.142386;
                    MainMenuRailway.wakeUpLocation.longitude = 72.937773;
                    break;

                case 15:
                    MainMenuRailway.wakeUpLocation.latitude = 19.142386;
                    MainMenuRailway.wakeUpLocation.longitude = 72.937773;
                    break;

                case 16:
                    MainMenuRailway.wakeUpLocation.latitude = 19.142386;
                    MainMenuRailway.wakeUpLocation.longitude = 72.937773;
                    break;

                case 17:
                    MainMenuRailway.wakeUpLocation.latitude = 18.942641;
                    MainMenuRailway.wakeUpLocation.longitude = 72.835981;
                    break;

                case 18:
                    MainMenuRailway.wakeUpLocation.latitude = 19.186408;
                    MainMenuRailway.wakeUpLocation.longitude = 72.975511;
                    break;

                case 19:
                    MainMenuRailway.wakeUpLocation.latitude = 18.942641;
                    MainMenuRailway.wakeUpLocation.longitude = 72.835981;
                    break;

                case 20:
                    MainMenuRailway.wakeUpLocation.latitude = 19.195310;
                    MainMenuRailway.wakeUpLocation.longitude = 72.996735;
                    break;

                case 21:
                    MainMenuRailway.wakeUpLocation.latitude = 19.190218;
                    MainMenuRailway.wakeUpLocation.longitude = 73.023074;
                    break;

                case 22:
                    MainMenuRailway.wakeUpLocation.latitude = 19.188482;
                    MainMenuRailway.wakeUpLocation.longitude = 73.041692;
                    break;

                case 23:
                    MainMenuRailway.wakeUpLocation.latitude = 19.210787;
                    MainMenuRailway.wakeUpLocation.longitude = 73.076956;
                    break;

                case 24:
                    MainMenuRailway.wakeUpLocation.latitude = 19.218294;
                    MainMenuRailway.wakeUpLocation.longitude = 73.086875;
                    break;

                case 25:
                    MainMenuRailway.wakeUpLocation.latitude = 19.218294;
                    MainMenuRailway.wakeUpLocation.longitude = 73.086875;
                    break;

                case 26:
                    MainMenuRailway.wakeUpLocation.latitude = 19.228550;
                    MainMenuRailway.wakeUpLocation.longitude = 73.148895;
                    break;

                case 27:
                    MainMenuRailway.wakeUpLocation.latitude = 19.218104;
                    MainMenuRailway.wakeUpLocation.longitude = 73.163086;
                    break;

                case 28:
                    MainMenuRailway.wakeUpLocation.latitude = 19.210157;
                    MainMenuRailway.wakeUpLocation.longitude = 73.184431;
                    break;

                case 29:
                    MainMenuRailway.wakeUpLocation.latitude = 19.166657;
                    MainMenuRailway.wakeUpLocation.longitude = 73.239503;
                    break;

                case 30:
                    MainMenuRailway.wakeUpLocation.latitude = 19.094430;
                    MainMenuRailway.wakeUpLocation.longitude = 73.300676;
                    break;

                case 31:
                    MainMenuRailway.wakeUpLocation.latitude = 19.063454;
                    MainMenuRailway.wakeUpLocation.longitude = 73.317739;
                    break;

                case 32:
                    MainMenuRailway.wakeUpLocation.latitude = 19.026743;
                    MainMenuRailway.wakeUpLocation.longitude = 73.318381;
                    break;

                case 33:
                    MainMenuRailway.wakeUpLocation.latitude = 18.969997;
                    MainMenuRailway.wakeUpLocation.longitude = 73.331628;
                    break;

                case 34:
                    MainMenuRailway.wakeUpLocation.latitude = 18.910476;
                    MainMenuRailway.wakeUpLocation.longitude = 73.321285;
                    break;

                case 35:
                    MainMenuRailway.wakeUpLocation.latitude = 18.884283;
                    MainMenuRailway.wakeUpLocation.longitude = 73.320757;
                    break;

                case 36:
                    MainMenuRailway.wakeUpLocation.latitude = 18.845682;
                    MainMenuRailway.wakeUpLocation.longitude = 73.318812;
                    break;

                case 37:
                    MainMenuRailway.wakeUpLocation.latitude = 18.834290;
                    MainMenuRailway.wakeUpLocation.longitude = 73.320023;
                    break;

                case 38:
                    MainMenuRailway.wakeUpLocation.latitude = 18.808655;
                    MainMenuRailway.wakeUpLocation.longitude = 73.335409;
                    break;

                case 39:
                    MainMenuRailway.wakeUpLocation.latitude = 18.789615;
                    MainMenuRailway.wakeUpLocation.longitude = 73.344882;
                    break;

                case 40:
                    MainMenuRailway.wakeUpLocation.latitude = 19.244376;
                    MainMenuRailway.wakeUpLocation.longitude = 73.158343;
                    break;

                case 41:
                    MainMenuRailway.wakeUpLocation.latitude = 19.267759;
                    MainMenuRailway.wakeUpLocation.longitude = 73.171722;
                    break;

                case 42:
                    MainMenuRailway.wakeUpLocation.latitude = 19.296851;
                    MainMenuRailway.wakeUpLocation.longitude = 73.203338;
                    break;

                case 43:
                    MainMenuRailway.wakeUpLocation.latitude = 19.356824;
                    MainMenuRailway.wakeUpLocation.longitude = 73.218973;
                    break;

                case 44:
                    MainMenuRailway.wakeUpLocation.latitude = 19.406495;
                    MainMenuRailway.wakeUpLocation.longitude = 73.267669;
                    break;

                case 45:
                    MainMenuRailway.wakeUpLocation.latitude = 19.439554;
                    MainMenuRailway.wakeUpLocation.longitude = 73.307934;
                    break;

                case 46:
                    MainMenuRailway.wakeUpLocation.latitude = 19.502263;
                    MainMenuRailway.wakeUpLocation.longitude = 73.329890;
                    break;

                case 47:
                    MainMenuRailway.wakeUpLocation.latitude = 19.580436;
                    MainMenuRailway.wakeUpLocation.longitude = 73.393928;
                    break;

                case 48:
                    MainMenuRailway.wakeUpLocation.latitude = 19.648373;
                    MainMenuRailway.wakeUpLocation.longitude = 73.473195;
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

    public static List<Stations> getDataNames() {
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
