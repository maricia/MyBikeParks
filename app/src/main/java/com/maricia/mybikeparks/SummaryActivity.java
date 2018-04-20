package com.maricia.mybikeparks;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//
public class SummaryActivity extends AppCompatActivity{

    final static String TAG = "SummaryActivity";
    TextView readFileTextView;  //readfile view
    TextView totalTimeTextView;  //total time view
    TextView dateWalkTextView; //date view
    TextView speedWalkTextView;
    TextView distanceWalkTextView;
    Button readfilebtn; //read file button
    String filename = "walkroutes"; //file name
    File file; //file for location
    public static String theDate; //shared pref return
    public static String walkTime; //shared pref return
    public static String speed;//shared pref return
    public static String distance; //shared pref return
    private ArrayList<LatLng> points;
    public static String startLat;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_summary);
        /* custom bar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        */
        setbuttons();  //set button
        checkForFile();  //check for walkroute file



    }//end onCreate

    private void setbuttons() {
        //get buttons, and check to see if walk file exists
        readfilebtn = this.findViewById(R.id.readfilebtn);
        readFileTextView = this.findViewById(R.id.readFileTextView);
        readfilebtn.setEnabled(false);
    }


 
    public void checkForFile() {
        file = getBaseContext().getFileStreamPath(filename);
       // Log.d(TAG, "readFile: fileName typeOf: " + file.getClass().getName());
        if (file.exists()) { readfilebtn.setEnabled(true);}
        else { readfilebtn.setEnabled(false);}
    }

    public void readFile(){

        String extraFile =""; //temp storage
        points = new ArrayList<>();
        int i;
        try {
            FileInputStream fis = openFileInput(filename);
            while ((i = fis.read()) !=-1){
               //TODO convert String to Map<String,ListArray>
                extraFile = new StringBuilder().append(extraFile) + Character.toString((char) i);
                      }
            Log.d(TAG, "readFile: " + extraFile.getClass().getName());
            Map<Integer, String> routeInfo = new HashMap<>();
            String[] Time = extraFile.split("TotalTime=");

            Log.d(TAG, "readFile: *****" + Time.toString() +"*****" + String.valueOf(Time));

            readFileTextView = this.findViewById(R.id.readFileTextView);
            readFileTextView.setText(extraFile);
            totalTimeTextView = this.findViewById(R.id.totalTimeTextView);
            dateWalkTextView = this.findViewById(R.id.dateWalkTextView);
            speedWalkTextView = this.findViewById(R.id.speedWalkTextView);
            distanceWalkTextView = this.findViewById(R.id.distanceWalkTextView);

            ReadFromPrefs();
            totalTimeTextView.setText(walkTime);
            dateWalkTextView.setText(theDate);
            speedWalkTextView.setText(speed);
            distanceWalkTextView.setText(distance);

            Toast.makeText(getBaseContext(), filename,Toast.LENGTH_LONG).show();

            fis.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        getRoute(extraFile);
    }//end readFile

    public void ReadFromPrefs() {

        // SharedPreferences sharedPref = this.getSharedPreferences( Context.MODE_PRIVATE);
        SharedPreferences sharedPref = this.getSharedPreferences("BikeParkMapStats",Context.MODE_PRIVATE);
       // SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences().

        startLat = sharedPref.getString("myStartLat", "0");
        String startLng = sharedPref.getString("myStartLon", "0");
        theDate = sharedPref.getString("myActivityDate","0");
        walkTime = sharedPref.getString("myStopTime", "0");
        speed = sharedPref.getString("myWalkSpeed", "0" );
        distance = sharedPref.getString("myWalkDistance","0");

        Log.d(TAG, "ReadFromPrefs: " + theDate + " " + startLat + " " + startLng + " " + walkTime);

        //read from pref example
        //String latitudeString = pref.getString("Latitude", "0");
       // double latitude = Double.parseDouble(latitudeString);
    }


    public void onClick(View v){
        Log.d(TAG, "onClick: Clicked me");
        readFile();
      //  new FilesCreations().execute();
    }

    public ArrayList<LatLng> getRoute(String file)
    {
        Pattern pattern = Pattern.compile("([+-]?\\d*\\.?\\d+),([+-]?\\d*\\.?\\d+)");
        Matcher matcher = pattern.matcher(file);
        while(matcher.find())
        {
            double lat = Double.parseDouble(matcher.group(1));
            double lng = Double.parseDouble(matcher.group(2));
            points.add(new LatLng(lat,lng));
        }
        Log.d("points", points.toString());
        return points;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_summary_settings, menu);
        Log.d(TAG, "onCreateOptionsMenu: here now 1");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()){
           case R.id.action_about:
                Intent intentAbout =  new Intent (this, AboutActivity.class);
                startActivity(intentAbout);
                return true;
            case R.id.action_settings:
                getFragmentManager().beginTransaction().replace(android.R.id.content, new SettingsFragment()).commit();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    /*
    private class FilesCreations extends AsyncTask<Void, Void, String > {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            checkForFile();
        }

        public void checkForFile() {
            file = getBaseContext().getFileStreamPath(filename);
            Log.d(TAG, "checkForFile: fileName typeOf: ++++++" + file.getClass().getName());
            if (file.exists())  readfilebtn.setEnabled(true);
            else  readfilebtn.setEnabled(false);
        }



        @Override
        protected String doInBackground(Void... voids) {
            readFile();
            return null;
        }


        public void readFile(){

            String extraFile =""; //temp storage
            int i;
            try {
                Log.d(TAG, "readFile: ++++++++" + extraFile);
                FileInputStream fis = openFileInput(filename);
                while ((i = fis.read()) !=-1){
                    //TODO convert String to Map<String,ListArray>
                    extraFile = new StringBuilder().append(extraFile) + Character.toString((char) i);
                }
                Log.d(TAG, "readFile: ++++" + extraFile.getClass().getName());
                Log.d(TAG, "readFile: ++++++" + extraFile);
               // Map<Integer, String> routeInfo = new HashMap<>();
               // String[] Time = extraFile.split("TotalTime=");

                fis.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }


        }//end readFile

        @Override
        protected void onPostExecute(String extraFile) {
            super.onPostExecute(extraFile);

           Log.d(TAG, "readFile: *****" + extraFile);

            readFileTextView = readFileTextView.findViewById(R.id.totalTimeTextView);
            readFileTextView.setText(extraFile);
            totalTimeTextView = totalTimeTextView.findViewById(R.id.totalTimeTextView);

            Toast.makeText(getBaseContext(), filename,Toast.LENGTH_LONG).show();

        }
    }
    */
}
