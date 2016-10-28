package com.catacomblabs.wakemeup;

import android.Manifest;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GestureDetectorCompat;
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

public class ListStationsWestern extends AppCompatActivity implements Adapter.ClickListener,
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
    private GestureDetectorCompat gestureDetector;
    public AlertDialog.Builder builder;
    private final String TAG = "com.legionlabs.com.catacomblabs.wakemeup.ListStationsWestern.TAG";
    public static final String[] names = {
            "Churchgate",
            "Marine Lines",
            "Charni Road",
            "Grant Road",
            "Mumbai Central",
            "Mahalaxmi",
            "Lower Parel",
            "Elphinstone",
            "Dadar",
            "Matunga Road",
            "Mahim",
            "Bandra",
            "Khar Road",
            "Santacruz",
            "Vile Parle",
            "Andheri",
            "Jogeshwari",
            "Goregaon",
            "Malad",
            "Kandivali",
            "Borivali",
            "Dahisar",
            "Mira Road",
            "Bhayandar",
            "Naigaon",
            "Vasai Road",
            "Nalla Sopara",
            "Virar",
            "Vaitarna",
            "Saphale",
            "Kelve Road",
            "Palghar",
            "Umroli",
            "Boisar",
            "Vangaon",
            "Dahanu Road"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_stations_western);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        assert getSupportActionBar() != null;
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Western");

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

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view_western);
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
                    MainMenuRailway.wakeUpLocation.latitude = 18.9353;
                    MainMenuRailway.wakeUpLocation.longitude = 72.8273;
                    break;
                case 1:
                    MainMenuRailway.wakeUpLocation.latitude = 18.9447;
                    MainMenuRailway.wakeUpLocation.longitude = 72.8244;
                    break;
                case 2:
                    MainMenuRailway.wakeUpLocation.latitude = 18.9516;
                    MainMenuRailway.wakeUpLocation.longitude = 72.8186;
                    break;
                case 3:
                    MainMenuRailway.wakeUpLocation.latitude = 18.9634;
                    MainMenuRailway.wakeUpLocation.longitude = 72.8161;
                    break;
                case 4:
                    MainMenuRailway.wakeUpLocation.latitude = 18.9697;
                    MainMenuRailway.wakeUpLocation.longitude = 72.8194;
                    break;
                case 5:
                    MainMenuRailway.wakeUpLocation.latitude = 18.9825;
                    MainMenuRailway.wakeUpLocation.longitude = 72.8242;
                    break;
                case 6:
                    MainMenuRailway.wakeUpLocation.latitude = 18.9955;
                    MainMenuRailway.wakeUpLocation.longitude = 72.8302;
                    break;
                case 7:
                    MainMenuRailway.wakeUpLocation.latitude = 19.0075;
                    MainMenuRailway.wakeUpLocation.longitude = 72.8360;
                    break;
                case 8:
                    MainMenuRailway.wakeUpLocation.latitude = 19.0184;
                    MainMenuRailway.wakeUpLocation.longitude = 72.8432;
                    break;
                case 9:
                    MainMenuRailway.wakeUpLocation.latitude = 19.0307;
                    MainMenuRailway.wakeUpLocation.longitude = 72.8529;
                    break;
                case 10:
                    MainMenuRailway.wakeUpLocation.latitude = 19.0407;
                    MainMenuRailway.wakeUpLocation.longitude = 72.8469;
                    break;
                case 11:
                    MainMenuRailway.wakeUpLocation.latitude = 19.0544;
                    MainMenuRailway.wakeUpLocation.longitude = 72.8406;
                    break;
                case 12:
                    MainMenuRailway.wakeUpLocation.latitude = 19.0686;
                    MainMenuRailway.wakeUpLocation.longitude = 72.8400;
                    break;
                case 13:
                    MainMenuRailway.wakeUpLocation.latitude = 19.0817;
                    MainMenuRailway.wakeUpLocation.longitude = 72.8414;
                    break;
                case 14:
                    MainMenuRailway.wakeUpLocation.latitude = 19.0996;
                    MainMenuRailway.wakeUpLocation.longitude = 72.8440;
                    break;
                case 15:
                    MainMenuRailway.wakeUpLocation.latitude = 19.1192;
                    MainMenuRailway.wakeUpLocation.longitude = 72.8469;
                    break;
                case 16:
                    MainMenuRailway.wakeUpLocation.latitude = 19.1365;
                    MainMenuRailway.wakeUpLocation.longitude = 72.8490;
                    break;
                case 17:
                    MainMenuRailway.wakeUpLocation.latitude = 19.1648;
                    MainMenuRailway.wakeUpLocation.longitude = 72.8495;
                    break;
                case 18:
                    MainMenuRailway.wakeUpLocation.latitude = 19.1870;
                    MainMenuRailway.wakeUpLocation.longitude = 72.8489;
                    break;
                case 19:
                    MainMenuRailway.wakeUpLocation.latitude = 19.2045;
                    MainMenuRailway.wakeUpLocation.longitude = 72.8520;
                    break;
                case 20:
                    MainMenuRailway.wakeUpLocation.latitude = 19.2290;
                    MainMenuRailway.wakeUpLocation.longitude = 72.8573;
                    break;
                case 21:
                    MainMenuRailway.wakeUpLocation.latitude = 19.2501;
                    MainMenuRailway.wakeUpLocation.longitude = 72.8593;
                    break;
                case 22:
                    MainMenuRailway.wakeUpLocation.latitude = 19.2799;
                    MainMenuRailway.wakeUpLocation.longitude = 72.8561;
                    break;
                case 23:
                    MainMenuRailway.wakeUpLocation.latitude = 19.3114;
                    MainMenuRailway.wakeUpLocation.longitude = 72.8526;
                    break;
                case 24:
                    MainMenuRailway.wakeUpLocation.latitude = 19.3515;
                    MainMenuRailway.wakeUpLocation.longitude = 72.8463;
                    break;
                case 25:
                    MainMenuRailway.wakeUpLocation.latitude = 19.3824;
                    MainMenuRailway.wakeUpLocation.longitude = 72.8322;
                    break;
                case 26:
                    MainMenuRailway.wakeUpLocation.latitude = 19.4154;
                    MainMenuRailway.wakeUpLocation.longitude = 72.8613;
                    break;
                case 27:
                    MainMenuRailway.wakeUpLocation.latitude = 19.4553;
                    MainMenuRailway.wakeUpLocation.longitude = 72.8120;
                    break;
                case 28:
                    MainMenuRailway.wakeUpLocation.latitude = 19.5187;
                    MainMenuRailway.wakeUpLocation.longitude = 72.8500;
                    break;
                case 29:
                    MainMenuRailway.wakeUpLocation.latitude = 19.5778;
                    MainMenuRailway.wakeUpLocation.longitude = 72.8192;
                    break;
                case 30:
                    MainMenuRailway.wakeUpLocation.latitude = 19.6254;
                    MainMenuRailway.wakeUpLocation.longitude = 72.7910;
                    break;
                case 31:
                    MainMenuRailway.wakeUpLocation.latitude = 19.7000;
                    MainMenuRailway.wakeUpLocation.longitude = 72.7700;
                    break;
                case 32:
                    MainMenuRailway.wakeUpLocation.latitude = 19.7552;
                    MainMenuRailway.wakeUpLocation.longitude = 72.7606;
                    break;
                case 33:
                    MainMenuRailway.wakeUpLocation.latitude = 19.8000;
                    MainMenuRailway.wakeUpLocation.longitude = 72.7500;
                    break;
                case 34:
                    MainMenuRailway.wakeUpLocation.latitude = 19.8667;
                    MainMenuRailway.wakeUpLocation.longitude = 72.7500;
                    break;
                case 35:
                    MainMenuRailway.wakeUpLocation.latitude = 19.9700;
                    MainMenuRailway.wakeUpLocation.longitude = 72.7300;
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

            Intent intent = new Intent(this, CancelService.class);
            startService(intent);

            WakeUpActivity.cancelAlarm(editor);
            SelectorDialog.cancelAlarm = false;
            String noAlarm = "No station currently selected";
            currentAlarmText.setText(noAlarm);
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
        long time = AnimationUtils.currentAnimationTimeMillis();

        Animation bottomUp = AnimationUtils.loadAnimation(this,
                R.anim.bottom_up);
        Animation rotateFabForward = AnimationUtils.loadAnimation(this,
                R.anim.rotate_fab_forward);
        Animation makeOpaque = AnimationUtils.loadAnimation(this,
                R.anim.make_opaque);

        bottomUp.setStartTime(time);
        rotateFabForward.setStartTime(time);
        makeOpaque.setStartTime(time);

        isPanelShowing = true;

        fab.startAnimation(rotateFabForward);

        if (Build.VERSION.SDK_INT >= 21)
            hiddenPanel.setElevation(8);
        hiddenPanel.startAnimation(bottomUp);
        hiddenPanel.setVisibility(View.VISIBLE);
        hiddenPanel.setAlpha(1.0f);

        hiddenPanelBackground.setVisibility(View.VISIBLE);
        hiddenPanelBackground.startAnimation(makeOpaque);
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