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
    private String descText = "Park Walker";
    private String jobTitle = "Developer";
    String developerName1 = "Joe Wolfe";
    String developerName2 = "Maricia Alleman";
    String developerName3 = "Judy Carmody";
    String developerName4 = "";
    String results;

    TextView developerDescTextView;
    TextView developer3DescTextView ;
    TextView descriptionTextView;
    TextView developer2DescTextView;
    TextView title;
    TextView developerNameTExtView1;
    TextView developerNameTExtView2;
    TextView developerNameTExtView3;
    TextView developerNameTExtView4;

    int versionCode = BuildConfig.VERSION_CODE;  //build
    String versionName = BuildConfig.VERSION_NAME;   //


    @Override
    protected void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        largeAmountofText();
        makeTexView();
        setTextViews();


        //    title.setText(String.format("Authors:"));
     //   tpauthor2.setText(String.format("Joe Wolfe"));
     //   tpauthor.setText(String.format("Maricia Alleman"));

       // tpauthor3.setText(String.format("LaTosha"));
      //  tpauthor4.setText(String.format("Judy Carmody"));

    }

    private void setTextViews() {
        descriptionTextView.setText(descText);
        developer3DescTextView.setText("Media Manager");
        developerDescTextView.setText(jobTitle);
        developer2DescTextView.setText(jobTitle);
        tpversion.setText(MessageFormat.format("version:{0} Build: {1}",versionName,versionCode));
        developerNameTExtView1.setText(developerName1);
        developerNameTExtView2.setText(developerName2);
        developerNameTExtView3.setText(developerName3);
        developerNameTExtView4.setText(developerName4);
        descriptionTextView.setText(results);

    }

    public void makeTexView() {

        developerDescTextView = this.findViewById(R.id.developerDescTextView);
        developer3DescTextView = this.findViewById(R.id.developer3DescTextView);
        descriptionTextView = this.findViewById(R.id.descriptionTextView);
        developer2DescTextView = this.findViewById(R.id.developer2DescTextView);
        tpversion = this.findViewById(R.id.tpversion);
        developerNameTExtView1 = this.findViewById(R.id.developerNameTExtView1);
        developerNameTExtView2 = this.findViewById(R.id.developerNameTExtView2);
        developerNameTExtView3 = this.findViewById(R.id.developerNameTExtView3);
        developerNameTExtView4 = this.findViewById(R.id.developerNameTExtView4);
        descriptionTextView = this.findViewById(R.id.descriptionTextView);
    }

 public void largeAmountofText(){

        String title = "ParkWalker";
        String line1 = "Allows you to search for parks near your location.";
        String line2 = "You can also time and track your walk path.";
        String line3 = "Their are several different features:";
        String line4 = "change color of you location marker";
        String line5 = "change the color of your path";
        String line6 = "change the line thickness of your path";
        String line7 = "Park Walker is an Android App developed by";
        String line8 = "Group 1";
     String line9 = "We are a group of student at";
     String line10 = "The University of Texas of the Permian Basin";





        StringBuilder sb = new StringBuilder();
       // sb.append(title).append("\n");
        sb.append(line1).append("\n");
        sb.append(line2).append("\n");
        sb.append(line3).append("\n");
        sb.append(line4).append("\n");
        sb.append(line5).append("\n");
        sb.append(line6).append("\n");
        sb.append(line7).append("\n");
        sb.append(line8).append("\n");
        sb.append(line9).append("\n");
        sb.append(line10).append("\n");


        results = sb.toString();
 }
}
