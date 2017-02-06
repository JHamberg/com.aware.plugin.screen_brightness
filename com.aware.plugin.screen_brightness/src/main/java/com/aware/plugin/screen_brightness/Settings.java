package com.aware.plugin.screen_brightness;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

import com.aware.Aware;

public class Settings extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    //Plugin settings in XML @xml/preferences
    private static final Long DEFAULT_INTERVAL = 60000L;
    public static final String INTERVAL_PLUGIN_SCREEN_BRIGHTNESS = "interval_plugin_screen_brightness";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if( Aware.getSetting(this, INTERVAL_PLUGIN_SCREEN_BRIGHTNESS).length() == 0 ) {
            Aware.setSetting( this, INTERVAL_PLUGIN_SCREEN_BRIGHTNESS, DEFAULT_INTERVAL);
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Preference setting = findPreference(key);
        if( setting.getKey().equals(INTERVAL_PLUGIN_SCREEN_BRIGHTNESS) ) {
            Aware.setSetting(this, key, sharedPreferences.getLong(key, DEFAULT_INTERVAL));
        }
    }
}
