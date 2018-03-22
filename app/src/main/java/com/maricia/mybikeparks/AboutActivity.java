package com.maricia.mybikeparks;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import java.text.MessageFormat;

/**
 * Created by alleman_m on 2/13/2018.
 */

public class AboutActivity extends AppCompatActivity{


    private TextView tpversion;
    private TextView tpauthor;

    int versionCode = BuildConfig.VERSION_CODE;  //build
    String versionName = BuildConfig.VERSION_NAME;   //


    @Override
    protected void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        tpversion = this.findViewById(R.id.tpversion);
        tpauthor = this.findViewById(R.id.tpauthor);

        tpversion.setText(MessageFormat.format("version:{0} Build: {1}",versionName,versionCode));

        tpauthor.setText(MessageFormat.format("Author: {0} ", String.format("Maricia Alleman")));



    }
}
