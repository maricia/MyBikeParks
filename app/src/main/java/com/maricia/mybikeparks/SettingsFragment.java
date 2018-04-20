package com.maricia.mybikeparks;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;

public class SettingsFragment extends PreferenceFragment {

    String theDate;
    String walkTime;
    String speed;
    String distance;

    final static String TAG = "SettingsFragment";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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

        Log.d(TAG, "ReadFromPrefs: " + theDate + " " + startLat + " " + startLng + " " + walkTime);

        //read from pref example
        //String latitudeString = pref.getString("Latitude", "0");
        // double latitude = Double.parseDouble(latitudeString);
       }

    }
