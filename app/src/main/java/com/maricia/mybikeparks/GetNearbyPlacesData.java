package com.maricia.mybikeparks;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;



/**
 * Created by maricia on 3/21/2018.
 */

public class GetNearbyPlacesData extends AsyncTask<Object, String, String>{

    String TAG = "GetNearbyPlacesData";
    String googlePlacesData;
    GoogleMap map;
    String url;
    RecyclerView mRecyclerView;
    private Context context ;


    @Override
    protected String doInBackground(Object... objects) {
      //  Log.d(TAG, "doInBackground: here I am **************" );
        map = (GoogleMap) objects[0];
        url = (String)objects[1];
        try {

            DownloadUrl downloadUrl = new DownloadUrl();
            googlePlacesData = downloadUrl.readUrl(url);
        //    Log.d(TAG, "doInBackground: " + googlePlacesData.toString());

        } catch (IOException e) {
            e.printStackTrace();
        }


        return googlePlacesData;
    }

    @Override
    protected void onPostExecute(String s) {

        List<HashMap<String, String>> nearbyPlaceList;
        DataParser parser = new DataParser();
        nearbyPlaceList = parser.parse(s);
        showNearbyPlaces(nearbyPlaceList);
    }


    private void showNearbyPlaces(List<HashMap<String,String>> nearbyPlaceList) {

        //create marker options here for the parks
        for (int i = 0; i<nearbyPlaceList.size(); i++){
            MarkerOptions markerOptions = new MarkerOptions();
            HashMap<String, String> googlePlace = nearbyPlaceList.get(i);
            String placeName = googlePlace.get("place_name");
            String vicinity = googlePlace.get("vicinity");
            double lat = Double.parseDouble(googlePlace.get("lat"));
            double lng = Double.parseDouble(googlePlace.get("lng"));
            LatLng latLng = new LatLng(lat, lng);
            markerOptions.position(latLng);
            markerOptions.title(placeName + " " + vicinity);
            //TODO read from prefences to get values
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
            map.addMarker(markerOptions);
            //optional - if not then camera will go to last place listed on map
            //map.animateCamera(CameraUpdateFactory.newLatLng(latLng));
            //map.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            map.animateCamera(CameraUpdateFactory.zoomTo(15));
        }
    }




}
