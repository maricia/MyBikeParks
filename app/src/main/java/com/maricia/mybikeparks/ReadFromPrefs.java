package com.maricia.mybikeparks;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

public class ReadFromPrefs {

    public static String theDate; //shared pref return
    public static String walkTime; //shared pref return
    public static String speed;//shared pref return
    public static String distance; //shared pref return
    public static String lineWeight;
    public static String parkMarker;
    public static String locationMarker;
    private ArrayList<LatLng> points;
    public static String startLat;
    final static String TAG = "ReadFromPrefs";




    public static String readPrefs(String key, Context context){
         SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences( context);
          return sharedPref.getString(key, null);
    }


}
