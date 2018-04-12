package com.maricia.mybikeparks;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class SummaryActivity extends AppCompatActivity {

    final static String TAG = "SummaryActivity";
    TextView readFileTextView;  //readfile view
    TextView totalTimeTextView;  //total time view
    Button readfilebtn; //read file button
    String filename = "walkroutes"; //file name
    File file; //file for location




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summary);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setbuttons();  //set button
        checkForFile();  //check for walkroute file
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }//end onCreate

    private void setbuttons() {
        //get buttons, and check to see if walk file exists
        readfilebtn = this.findViewById(R.id.readfilebtn);
        readFileTextView = this.findViewById(R.id.readFileTextView);
    }

    public void checkForFile() {
        file = getBaseContext().getFileStreamPath(filename);
        Log.d(TAG, "readFile: fileName typeOf: " + file.getClass().getName());
        if (file.exists())  readfilebtn.setEnabled(true);
        else  readfilebtn.setEnabled(false);
    }

    public void readFile(){

        String extraFile =""; //temp storage
        int i;
        try {
            FileInputStream fis = openFileInput(filename);
            while ((i = fis.read()) !=-1){
               //TODO convert String to Map<String,ListArray>
               // extraFile = Character.toString((char) i).split(",");
               // Log.d(TAG, "readFile: extraFile type of what:" + extraFile.getClass().getName());
                //String[] TotalTime = Character.toString((char) i).split(",");
               // Log.d(TAG, "readFile: " + extraFile);
                extraFile = new StringBuilder().append(extraFile) + Character.toString((char) i);
                //Character.toString((char) i).split("{");
               // routeInfo.put("totalTime", ????);
               // routeInfo.put("location", ????);
               // List<String> arrayList = new ArrayList<String>(arrayList.asList(extraFiles.split(",")));
               // readFileTextView.setText(extraFile[i]);
            }
            Log.d(TAG, "readFile: " + extraFile.getClass().getName());
            Map<Integer, String> routeInfo = new HashMap<>();
            String[] Time = extraFile.split("TotalTime=");

            /*
            String[] latlng = extraFile.split(":");

            for(int j=0; j<latlng.length; j++){
                String loc = String.valueOf(latlng.toString().split(","));
                switch (loc){
                    case "lat/lng":
                        break;
                    case "(":
                        break;
                }

                double lat = ;
                //int value = j;
                routeInfo.put(j, LatLng[j]);
            }
            */

            Log.d(TAG, "readFile: *****" + Time.toString() +"*****" + String.valueOf(Time));

            readFileTextView = this.findViewById(R.id.readFileTextView);
            readFileTextView.setText(extraFile);
            totalTimeTextView = this.findViewById(R.id.totalTimeTextView);


            Toast.makeText(getBaseContext(), filename,Toast.LENGTH_LONG).show();

            fis.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }//end readFile



    public void onClick(View v){
        Log.d(TAG, "onClick: Clicked me");
        readFile();
    }

    private String setButton() {
        readfilebtn.setEnabled(false);
        return null;
    }

}
