package com.maricia.mybikeparks;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class SummaryActivity extends AppCompatActivity {

    final static String TAG = "SummaryActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summary);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

       /*
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        */
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }//end onCreate

    public void readFile(){
        String filename = "walkroutes.txt";
        File file = getBaseContext().getFileStreamPath(filename);
        int i;
        String extraFile="";
        try {
            FileInputStream fis = openFileInput(filename);
            while ((i = fis.read()) !=-1){
                extraFile = new StringBuilder().append(extraFile) + Character.toString((char) i);
            }
            Toast.makeText(getBaseContext(), filename,Toast.LENGTH_LONG).show();
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

}
