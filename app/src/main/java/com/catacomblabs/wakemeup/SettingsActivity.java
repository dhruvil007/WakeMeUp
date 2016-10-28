package com.catacomblabs.wakemeup;

import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

public class SettingsActivity extends AppCompatActivity {

    public static String ringtoneSummary;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        getFragmentManager().beginTransaction().replace(R.id.settings_fragment, new settingsFragment()).commit();

        Toolbar toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);

        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setTitle("Settings");

        final Drawable upArrow = ContextCompat.getDrawable(this, R.drawable.abc_ic_ab_back_mtrl_am_alpha);
        upArrow.setColorFilter(ContextCompat.getColor(this, R.color.white), PorterDuff.Mode.SRC_ATOP);
        getSupportActionBar().setHomeAsUpIndicator(upArrow);

        SharedPreferences preference = PreferenceManager.getDefaultSharedPreferences(this);
        Uri ring = Uri.parse(preference.getString("ringtone_picker", "default"));
        Ringtone ringtone = RingtoneManager.getRingtone(this, ring);
        ringtoneSummary = ringtone.getTitle(this);
    }

    public static class settingsFragment extends PreferenceFragment {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);

            final RingtonePreference ringtonePreference = (RingtonePreference) findPreference("ringtone_picker");
            if (ringtoneSummary.equals("Unknown ringtone"))
                ringtoneSummary = "None";
            else if (ringtoneSummary.equals("default"))
                ringtoneSummary = "Default ringtone";
            ringtonePreference.setSummary(SettingsActivity.ringtoneSummary);

            ringtonePreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    Uri ring = Uri.parse(newValue.toString());
                    Ringtone ringtone = RingtoneManager.getRingtone(getActivity(), ring);
                    ringtoneSummary = ringtone.getTitle(getActivity());

                    RingtonePreference ringtonePreference = (RingtonePreference) findPreference("ringtone_picker");
                    if (ringtoneSummary.equals("Unknown ringtone"))
                        ringtoneSummary = "None";
                    ringtonePreference.setSummary(ringtoneSummary);
                    return true;
                }
            });
        }
    }
}
