package com.maricia.mybikeparks;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    String theDate;
    String walkTime;
    String speed;
    String distance;

    final static String TAG = "SettingsFragment";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: here now 1");
        //Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);
    }



    private void ReadFromPrefs() {

        // SharedPreferences sharedPref = this.getSharedPreferences( Context.MODE_PRIVATE);

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());//"BikeParkMapStats", Context.MODE_PRIVATE);
        //  SharedPreferences sharedPref = this.getSharedPreferences("BikeParkMapStats", Context.MODE_PRIVATE);

        String startLat = sharedPref.getString("myStartLat", "0");
        String startLng = sharedPref.getString("myStartLon", "0");
        theDate = sharedPref.getString("myActivityDate", "0");
        walkTime = sharedPref.getString("myStopTime", "0");
        speed = sharedPref.getString("myWalkSpeed", "0");
        distance = sharedPref.getString("myWalkDistance", "0");

     //   Log.d(TAG, "ReadFromPrefs: " + theDate + " " + startLat + " " + startLng + " " + walkTime);

        //read from pref example
        //String latitudeString = pref.getString("Latitude", "0");
        // double latitude = Double.parseDouble(latitudeString);
       }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences
                                                  sharedPreferences, String key) {
        if (key.equals("food_preference")) {
            Preference foodPref = findPreference(key);
            foodPref.setSummary(sharedPreferences.getString(key, ""));
        }
    }

    }
