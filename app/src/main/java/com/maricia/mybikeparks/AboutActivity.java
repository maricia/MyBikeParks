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

        TextView title = this.findViewById(R.id.title);
        TextView tpversion = this.findViewById(R.id.tpversion);
        TextView tpauthor = this.findViewById(R.id.authorTextView2);
        TextView tpauthor2 = this.findViewById(R.id.authorTextView);
        TextView tpauthor3 = this.findViewById(R.id.authorTextView3);
        TextView tpauthor4 = this.findViewById(R.id.authorTextView4);

        tpversion.setText(MessageFormat.format("version:{0} Build: {1}",versionName,versionCode));

        title.setText(String.format("Authors:"));
        tpauthor.setText(String.format("Maricia Alleman"));
        tpauthor2.setText(String.format("Joe Wolfe"));
        tpauthor3.setText(String.format("LaTosha"));
        tpauthor4.setText(String.format("Judy Carmody"));



    }
}
