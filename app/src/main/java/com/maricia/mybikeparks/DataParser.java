package com.maricia.mybikeparks;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by maricia on 3/21/2018.
 */

public class DataParser {


    private static final String TAG = "DataParser";

    private HashMap<String, String> getPlace(JSONObject googlePlaceJson){
        HashMap<String,String> googlePlaceMap = new HashMap<>();
        String placeName = "name";
        String vicinity = "vicinity";
        String latitude = "";
        String longitude = "";
        String reference = "";
      //  Log.d(TAG, "getPlace: " + googlePlaceJson.toString());

        try {
            if(!googlePlaceJson.isNull("name")){

                placeName = googlePlaceJson.getString("name");
            }
            if(!googlePlaceJson.isNull(vicinity)){
                vicinity = googlePlaceJson.getString("vicinity");

            }
            latitude = googlePlaceJson.getJSONObject("geometry").getJSONObject("location").getString("lat");
            longitude = googlePlaceJson.getJSONObject("geometry").getJSONObject("location").getString("lng");
            reference = googlePlaceJson.getString("reference");

            //put in hashmap
            googlePlaceMap.put("place_name", placeName);
            googlePlaceMap.put("vicinity", vicinity);
            googlePlaceMap.put("lat",latitude);
            googlePlaceMap.put("lng", longitude);
            googlePlaceMap.put("reference", reference);

        } catch (JSONException e) {
            e.printStackTrace();
            }


        return googlePlaceMap;

    }//end HASHMAP


    private List<HashMap<String,String>> getPlaces(JSONArray jsonArray){

        int count = jsonArray.length();
        List<HashMap<String,String>> placeList = new ArrayList<>();
        HashMap<String, String> placeMap = null;
        try {
            for(int i=0; i<count; i++){

                placeMap = getPlace((JSONObject) jsonArray.get(i));
                placeList.add(placeMap);

            }
        } catch (JSONException e) {
                e.printStackTrace();
        }


        return placeList;
    }

    public List<HashMap<String,String>> parse(String jsonData){

        JSONArray jsonArray = null;
        JSONObject jsonObject = null;

        try {
            jsonObject = new JSONObject(jsonData);
            jsonArray = jsonObject.getJSONArray("results");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return getPlaces(jsonArray);
    }

}//end class
