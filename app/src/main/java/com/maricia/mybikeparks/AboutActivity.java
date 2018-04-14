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

        TextView tpversion = this.findViewById(R.id.tpversion);
        TextView tpauthor = this.findViewById(R.id.tpauthor);
        TextView tpauthor2 = this.findViewById(R.id.tpauthor2);
        TextView tpauthor3 = this.findViewById(R.id.tpauthor3);
        TextView tpauthor4 = this.findViewById(R.id.tpauthor4);

        tpversion.setText(MessageFormat.format("version:{0} Build: {1}",versionName,versionCode));

        tpauthor.setText(MessageFormat.format("Authors: {0} ", String.format("Maricia Alleman")));
        tpauthor2.setText(String.format("Joe Wolfe"));
        tpauthor3.setText(String.format("LaTosha"));
        tpauthor4.setText(String.format("Judy Carmody"));



    }
}
