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
    private ArrayList<LatLng> points;
    boolean hasClicked = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_summary);
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
        if (file.exists()) {
            readfilebtn.setEnabled(true);
            readFile();
        }
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
            Map<Integer, String> routeInfo = new HashMap<>();
            String[] Time = extraFile.split("TotalTime=");

            getTextViews(extraFile);
            setTextViews();
            if (hasClicked) readFileTextView.setText(extraFile);
            Toast.makeText(getBaseContext(), filename,Toast.LENGTH_LONG).show();

            fis.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        getRoute(extraFile);
    }//end readFile

    private void setTextViews() {

        totalTimeTextView.setText(ReadFromPrefs.readPrefs("myStopTime", this));
        dateWalkTextView.setText(ReadFromPrefs.readPrefs("myActivityDate", this));
        speedWalkTextView.setText(ReadFromPrefs.readPrefs("myWalkSpeed", this));
        distanceWalkTextView.setText(ReadFromPrefs.readPrefs("myWalkDistance", this));

    }

    private void getTextViews(String extraFile) {

        readFileTextView = this.findViewById(R.id.readFileTextView);
        //readFileTextView.setText(extraFile);
        totalTimeTextView = this.findViewById(R.id.totalTimeTextView);
        dateWalkTextView = this.findViewById(R.id.dateWalkTextView);
        speedWalkTextView = this.findViewById(R.id.speedWalkTextView);
        distanceWalkTextView = this.findViewById(R.id.distanceWalkTextView);

    }


    public void onClick(View v){
        hasClicked=true;
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
                Intent intetSettings = new Intent(this, SettingsActivity.class);
                startActivity(intetSettings);
                //getFragmentManager().beginTransaction().replace(android.R.id.content, new SettingsFragment()).commit();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


}
