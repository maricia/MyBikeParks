package com.maricia.mybikeparks;

import android.Manifest;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Looper;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewDebug;
import android.widget.Chronometer;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.maps.GoogleMap.OnCameraMoveStartedListener;

import com.google.android.gms.tasks.Task;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//FragmentActivity
public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks, OnCameraMoveStartedListener,
        GoogleApiClient.OnConnectionFailedListener, PlaceSelectionListener,
        LocationListener, SaveTrackDialogFragment.NoticeDialogListener{

    private GoogleMap mMap;                    //googlemap
    private GoogleApiClient mGoogleApiClient;  //googleApiclient
    private LocationRequest locationRequest;   //location
    private Location lastLocation;             //last location
    private Marker currLocationMarker;         //current location marker
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    private TextView locationTextField;
    int PROXIMITY_RADIUS = 10000;
    double latitude;
    double longitude;
    private static final String TAG = "MapsActivity";
    private PlaceAutocompleteFragment autocompleteFragment;
    private Boolean isFollowing; // This boolean will toggle if the map moves when the user's location changes
    private Boolean isCentering; //an annoying workaround to not having access to the zoom controls T.T
    private Boolean isTracking; // flag that checks for if we need to log the user's position
    private ArrayList<LatLng> points;
    private PolylineOptions polylineOptions;
    private Polyline polyline;
    private LocationManager mLocationManager;
    private long UPDATE_INTERVAL = 1000; // 1 minute 60000
    private long FASTEST_INTERVAL = 1000; // 30 Seconds 30000
    private int count = 0; //count the number of saved files
    private Chronometer timeKeeper; //timer for activity
    public String howLong; //activity time
    public double trackDistance;
    public Integer startCount = 0;
    public static final  String BileParkMapStats_PREFERENCES = "BikeParkMapStats";
    private String todaysDate;
    public String activityDate;
    boolean isStopping = false;
    boolean isWalking = false;
    public static final String myStartLat = "myStartLat", myStopLat = "myStopLat",myStartLon = "myStartLon",myStopLon = "myStopLon",myStartTime = "myStartTime",myStopTime = "myStopTime",myActivityDate = "myActivityDate"
            ,myWalkSpeed = "myWalkSpeed",mywalkDistance="myWalkDistance", myLineWeight = "myLineWeight", myColorValue = "myColorValue",
            myParkMarkerColor="myParkMarkerColor", myMarkerColor="myMarkerColor";
   private Integer lineWeight = 5; //this is for preferences
   private Integer colorValue = Color.BLUE; //this is for preferences
   private Float markerColor = BitmapDescriptorFactory.HUE_RED;
   private Float parkMarkerColor = BitmapDescriptorFactory.HUE_GREEN;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
       // Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
     //   setSupportActionBar(toolbar);
        //google search bar
        addSearch();
        locationPermision();
        startMap();
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

    }//end onCreate
    /*
    starts the map activity and the background thread
     */
    private void startMap() {
        // Obtain the SupportMapFragment and get notified when the mMap is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        startLocationUpdates();
    }//end startMap

    private void locationPermision() {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkLocationPermission();
        }
    }//end locationPermision

    /*
    Google search bar
     */
    private void addSearch() {
        autocompleteFragment = (PlaceAutocompleteFragment)  getFragmentManager().findFragmentById(R.id.autocomplete_fragment);
        autocompleteFragment.setHint("Search here");
        autocompleteFragment.setFilter( new AutocompleteFilter.Builder().setTypeFilter(Place.TYPE_COUNTRY).setCountry("US").build());
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                isFollowing = false;
                mMap.clear();
                placeMarker(place);
            }
            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.i(TAG, "An error occurred: " + status);
            }
        });
    }//end addSearch
    /*
    marker for places from the search menu
     */
    private void placeMarker(Place place) {
        CharSequence name = place.getName();
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(place.getLatLng());
        markerOptions.title(name.toString());
        mMap.addMarker(markerOptions);
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));
        //optional - if not then camera will go to last place listed on mMap
        mMap.animateCamera(CameraUpdateFactory.newLatLng(place.getLatLng()));
    }//end placeMarker

    /*

     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
      return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()){
            case R.id.action_activity:
                Intent intentActiveSummary = new Intent(this, SummaryActivity.class);
                startActivity(intentActiveSummary);
                return true;
            case R.id.action_Start:
                if(!isWalking) {
                    item.setIcon(R.drawable.ic_directions_walk_black_24dp);
                    isWalking=true;
                    startTiming();
                    startTracking();
                    return true;
                 }else
                {
                    item.setIcon(R.drawable.ic_directions_walk_white_24dp);
                    isWalking = false;
                    stopTiming();
                    stopTracking();
                }
                return true;
        }
       return super.onOptionsItemSelected(item);
    }


    @Override
    public void onCameraMoveStarted(int reason) {
        //This method controls when the onLocationChanged method controls the camera
        if (reason == OnCameraMoveStartedListener.REASON_GESTURE) {
            //"The user gestured on the map."
            isFollowing = false;
        } else if (reason == OnCameraMoveStartedListener.REASON_API_ANIMATION) {
            //"The user tapped something on the map."
            if (isCentering)
            {
                isCentering = false; // resets the centering flag so we know when we hit the button again
            }
            else
            {
                isFollowing = false;
            }
        }
        //else if (reason == OnCameraMoveStartedListener.REASON_DEVELOPER_ANIMATION)
        // The app moved the camera."
        // we can't really use this one because when the map pans over to the user on
        // initialization this one gets called so try to set the isFollowing flag when
        // you move the map and want it to stay

    }

    //to handle search button when user clicks on it
    public void onClick(View v){

        //check what button is being pressed
        Object dataTransfer[] = new Object[2];
        GetNearbyPlacesData getNearbyPlacesData = new GetNearbyPlacesData();
        switch (v.getId()){//searchButton
            default:Toast.makeText(this,v.getId()+"", Toast.LENGTH_LONG).show();
            case R.id.parksButton:
                isFollowing = false;
                mMap.clear();
                String park = "park";
                String url = getUrl(latitude, longitude, "park");
                dataTransfer[0] = mMap;
                dataTransfer[1] = url;
                getNearbyPlacesData.execute(dataTransfer);
                Toast.makeText(MapsActivity.this, "Parks", Toast.LENGTH_LONG).show();
                break;
        }//end switch
    }

    @Override
    public void onConnected(Bundle bundle) {

        locationRequest = new LocationRequest();
        locationRequest.setInterval(UPDATE_INTERVAL);
        locationRequest.setFastestInterval(FASTEST_INTERVAL);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            startLocationUpdates();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialog)
    {
        // User touched the dialog's positive button
        //SaveUserTracks();
        Log.d(TAG, "onDialogPositiveClick: Look has points" + points);
        //saves new file in background
        new FilesCreations().execute();
        //remove the path on the screen
        finishTracking();
    }


    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {
        // User touched the dialog's negative button
        // we just gunna delete the path
        timeKeeper.stop();
        finishTracking();
    }

    @Override
    public void onError(Status status) {

        Toast.makeText(this, "OOPS SOMETHING WENT WRONG", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onLocationChanged(Location location) {

        latitude = location.getLatitude();
        longitude = location.getLongitude();
        lastLocation = location;

        if (currLocationMarker != null) {
            currLocationMarker.remove();
        }

        //Place current location marker
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title("You Are Here");
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        currLocationMarker = mMap.addMarker(markerOptions);

        //save current location to prefences


        //move mMap camera
        if (isFollowing) { // Todo: Find a way to detect if the user moves the map and toggle isFollowing if they are more interested in looking at another location.
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(18));
        }
        if (isTracking) {

            recordPath(latLng);
        }
        //stop location updates todo: delete this if statement or move it somewhere more appropriate
        /*
        this if statement removes the location updates we need in order to track the user
        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }*/
    }

    /**
     * Manipulates the mMap once available.
     * This callback is triggered when the mMap is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.setOnCameraMoveStartedListener(this);
        isFollowing = true;
        isCentering = false;
        isTracking = false;
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
            mMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener(){
                @Override
                public boolean onMyLocationButtonClick() // listens for the Location button to be prssed
                {
                    //TODO: Any custom actions
                    isCentering = true; // lets the OnCameraMoveStartedFunction know that the Location button was pressed... why can't we have more event listeners like this one...
                    isFollowing = true; // allows the map to focus on the user's location again
                    getLastKnownLocation();
                    return true; //true consumes the event false the dafulat behavior occurs as well
                }
            });
            buildGoogleApiClient();
            getLastKnownLocation();
        }

    }//end onMapReady


    @Override
    public void onPlaceSelected(Place place) {
        //Log.d(TAG, "onPlaceSelected: *********" + place);
        List<Address> addressList;
        try {
            //Just wanna make sure this if statement works. can you use .equals between a Place object and a String
            if (!place.equals("")) {
                //use geocoder class to get names
                Geocoder geocoder = new Geocoder(this);
                addressList = geocoder.getFromLocationName(place.getName().toString(), 5);      //returns as array of addresses maybe a place name, address, or airport code
               // Log.d(TAG, "onPlaceSelected: address list: " + addressList);
                if(addressList != null){
                    //put a marker on all the places searched
                    for (int i = 0; i < addressList.size(); i++) {
                        Address myAddress = addressList.get(i);
                        LatLng latLng = new LatLng(myAddress.getLatitude(), myAddress.getLongitude());
                        MarkerOptions markerOptions = new MarkerOptions();
                        markerOptions.position(latLng);
                        markerOptions.title(lastLocation.toString());
                        mMap.addMarker(markerOptions);
                        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));
                        //optional - if not then camera will go to last place listed on mMap
                        mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
                    }
                }
            }//end if
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted. Do the
                    // contacts-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        if (mGoogleApiClient == null) {
                            buildGoogleApiClient();
                        }
                        mMap.setMyLocationEnabled(true);
                        getLastKnownLocation(); //update the map now that we have permission to do so
                    }

                } else {

                    // Permission denied, Disable the functionality that depends on this permission.
                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show();
                }
            }

            // other 'case' lines to check for other permissions this app might request.
            // You can add here other case statements according to your requirement.
        }
    }

    private double getHaversineDistance(LatLng previousLoc, LatLng loc)
    {
        //Math that i just looked up.
        double oldLat = previousLoc.latitude;
        double oldLong = previousLoc.longitude;
        double newLat = loc.latitude;
        double newLong = loc.longitude;

        //earth radius in Miles
        final double RADIUS = 3959;

        //getting displacement
        double deltaLat = Math.toRadians(newLat - oldLat);
        double deltaLon = Math.toRadians(newLong - oldLong);

        //converts the latitudes to radians
        oldLat = Math.toRadians(oldLat);
        newLat = Math.toRadians(newLat);

        double a = Math.pow(Math.sin(deltaLat / 2),2) + Math.pow(Math.sin(deltaLon / 2),2) * Math.cos(oldLat) * Math.cos(newLat);
        double c = 2 * Math.asin(Math.sqrt(a));
        return RADIUS * c;
    }


    private void getLastKnownLocation() // Lets you get the current location with a single method call.
    {
        //again jumping through hoops to make compiler happy
        FusedLocationProviderClient locationClient = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                // GPS location can be null if GPS is switched off
                if (location != null) {
                    onLocationChanged(location);
                }
                Log.d(TAG, "onSuccess: last known location" + latitude + longitude);
                //maybe save to prefernces here
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                Log.d("MapDemoActivity", "Error trying to get last GPS location");

                e.printStackTrace();
            }
        });


    }

    private String getUrl(double latitude, double longitude, String nearbyPlace ){

        Log.d(TAG, "getUrl: latitude and longitude" + latitude +", " + longitude);
        Log.d(TAG, "getUrl: radius " + PROXIMITY_RADIUS);
        Log.d(TAG, "getUrl: types" + nearbyPlace);
        StringBuilder googlePlaceUrl = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        googlePlaceUrl.append("location="+latitude+","+longitude);
        googlePlaceUrl.append("&radius="+PROXIMITY_RADIUS);
        googlePlaceUrl.append("&types="+nearbyPlace);
        googlePlaceUrl.append("&key="+"AIzaSyBu79j0aAe6zfixFxaWfLdK2ScSBOohdxA");
        googlePlaceUrl.append("&sensor=true");
        Log.d(TAG, "getUrl: my url" + googlePlaceUrl);
        return googlePlaceUrl.toString();
    }

    protected synchronized void buildGoogleApiClient() {
        //google Api client
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    public boolean checkLocationPermission(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            // Asking user if explanation is needed  why the permission is needed
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                //Prompt the user once explanation has been shown
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_LOCATION);
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_LOCATION);
            }

            return false;
        } else {
            return true;
        }
    }

    public String getCurrentDate() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat mdformat = new SimpleDateFormat("MM-dd-yyyy");
        todaysDate = mdformat.format(calendar.getTime());
        return todaysDate;
    }

    public  void startTiming() {
        Log.d(TAG, "startTiming: Stsrtnow");
        timeKeeper = (Chronometer) findViewById(R.id.timmer);
        timeKeeper.setBase(SystemClock.elapsedRealtime()); //reset
        String activityDate = getCurrentDate();
        timeKeeper.start();
        startCount ++;
    }
    public void stopTiming() {
        timeKeeper.stop();

    }

    private void finishTracking() //clear the polylines drawn while tracking;
    {
        isTracking = false;
        mMap.clear(); // ToDo: if there is a way to clear the lines without using the clear method
        getLastKnownLocation(); // we should probably switch to that


    }

    private int parseTime() //returns the number of elapsed seconds
    {

        String value = timeKeeper.getText().toString();
        String [] parts = value.split(":");

        // Wrong format, no value for you.
        if(parts.length < 2 || parts.length > 3)
            return 0;

        int seconds = 0, minutes = 0, hours = 0;

        if(parts.length == 2){
            seconds = Integer.parseInt(parts[1]);
            minutes = Integer.parseInt(parts[0]);
        }
        else if(parts.length == 3){
            seconds = Integer.parseInt(parts[2]);
            minutes = Integer.parseInt(parts[1]);
            hours = Integer.parseInt(parts[0]);
        }

        return seconds + (minutes*60) + (hours*3600);
    }

    private void recordPath(LatLng loc) // couple lines of code that draws users position over time
    {
        LatLng previousLoc;
        if (!points.isEmpty())
        {
            previousLoc = points.get(points.size()-1);
            trackDistance += getHaversineDistance(previousLoc,loc);
        }
        points.add(loc);
        polylineOptions.add(loc);
        polyline = mMap.addPolyline(polylineOptions);

    }


    private void stopTracking() //creates a Dialog box to get user's choice about what to do about the path traveled
    {
        if (!isTracking) return;
        isStopping = true;
        saveLocationToPreferences(latitude, longitude, isStopping ,trackDistance/parseTime()*3600,trackDistance, lineWeight, colorValue, markerColor);
        ArrayList<LatLng> myLocation = points;  //points
        DialogFragment newFragment = new SaveTrackDialogFragment();
        newFragment.show(getFragmentManager(), "saveDialog");

    }

    private void startTracking() // initialize everything needed for the recordPath Function
    {
        //maybe save to prefences here
        saveLocationToPreferences(latitude, longitude, isStopping,0,trackDistance, lineWeight, colorValue,markerColor );
        isTracking = true;
        points = new ArrayList<LatLng>();
        trackDistance =0;
        polylineOptions = new PolylineOptions();
        polylineOptions.color(colorValue);
        polylineOptions.width(lineWeight);

    }
   //save distance and time to Preferences
   //high jacking this method cause this seems like where the action is happening
    private void saveLocationToPreferences(double latitude, double longitude, boolean isStopping, double walkSpeed, double walkDistance, int lineWeight, int colorValues, float locationMarkerColor){

        SharedPreferences sharedPref = this.getSharedPreferences("com.maricia.mybikeparks_preferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        activityDate = getCurrentDate();
        if(!isStopping){
            editor.putString(myStartLat, Double.valueOf(latitude).toString());
            editor.putString(myStartLon, Double.valueOf(longitude).toString());
            editor.putString(myActivityDate, activityDate); //date of activity
            editor.putString(myWalkSpeed,Double.valueOf(walkSpeed).toString());
            editor.putString(mywalkDistance, Double.valueOf(walkDistance).toString());
            editor.putString(myLineWeight, Integer.valueOf(lineWeight).toString());
            editor.putString(myColorValue, Integer.valueOf(colorValue).toString());
            editor.putString(myMarkerColor, Float.valueOf(markerColor).toString());
            editor.commit();
        }else {
            //this happens at stop
            editor.putString(myStopLat, Double.valueOf(latitude).toString());
            editor.putString(myStopLon, Double.valueOf(longitude).toString());
            try
            {
                String temp = Double.valueOf(walkDistance).toString(); //using this to trunicate the distance
                editor.putString(mywalkDistance, temp.substring(0,temp.indexOf(".")+3));
                temp = Double.valueOf(walkSpeed).toString();
                editor.putString(myWalkSpeed, temp.substring(0,temp.indexOf(".")+3));
            }
            catch (IndexOutOfBoundsException e)
            {
                editor.putString(mywalkDistance, "0.0");
                editor.putString(myWalkSpeed, "0.0");
            }

            howLong =  timeKeeper.getText().toString();
            //start time my always be 0
            //this may need to be converted to a different type later
            editor.putString(myStopTime, howLong);
            editor.commit();
        }


    }


    protected void startLocationUpdates() {
        //The Location requests and makes it start recieving updates
        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(UPDATE_INTERVAL);
        locationRequest.setFastestInterval(FASTEST_INTERVAL);
        locationRequest.setSmallestDisplacement(80); //todo maybe this should be a preference toggle? Battery saver mode?


        // we build  a location settings request object using a builder based on the above
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(locationRequest);
        LocationSettingsRequest locationSettingsRequest = builder.build();

        Log.d("MapDemoActivity", "WHAT AM I DOING");
        // We check if the system is capable of sending/receiving location requests
        SettingsClient settingsClient = LocationServices.getSettingsClient(this);
        settingsClient.checkLocationSettings(locationSettingsRequest);

        if (checkLocationPermission())
        {
            //the LocationServices here isn't in the tutorial i am reading but this was the only way to build without errors
            final Task<Void> voidTask = LocationServices.getFusedLocationProviderClient(this).requestLocationUpdates(locationRequest, new LocationCallback() {
                        @Override
                        public void onLocationResult(LocationResult locationResult) {
                            System.out.print("what");
                            onLocationChanged(locationResult.getLastLocation());
                        }
                    }, //that looper is in the getFused...method above... just looks weird down there
                    Looper.myLooper());
        }
    }


    private class FilesCreations extends AsyncTask<Void, Void, String >{

        final static String TAG = "FilesCreations";
        String totalTime = new String(); //stores total time of activity

        //save points to a locale memory space - later this need to be updated to a database of some sort

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            howLong =  timeKeeper.getText().toString();
           // Toast.makeText(getApplicationContext(), "howLong: " + howLong, Toast.LENGTH_LONG).show();
            timeKeeper.stop();

        }

        private boolean fileExist(String filename){
            File file = getBaseContext().getFileStreamPath(filename);
            return file.exists();
        }

        private String fileNames(){
            String filename = "walkroutes";
           if(!fileExist(filename)){
               filename = "walkroutes";
           }
            return filename;
        }

        private void saveUserTracks(){

            FileOutputStream ops;
            String filename = fileNames();
            Map<String, String> walktime = new HashMap<>();
            String t = "";
            //save locations hopefully I will be able to do a layout on the map
            //TODO display map overlay using saves latlng in saved file
            //keep track of everytime you push start - this would be the number of locations
            //only makes one file and appends each time you walk on the end of the file.
            //this file is located under files
            try {
                t = String.valueOf(points);
                walktime.put("TotalTime",howLong);
                walktime.put("Distance", ""+trackDistance);
                walktime.put("Speed",trackDistance/(parseTime())*3600+"");
                walktime.put("Routes", t);
                ops = openFileOutput(filename, MODE_APPEND);
                //writing a string, "walktime" to the "walkroutes" file
                ops.write(walktime.toString().getBytes());
                ops.close();
            } catch (IOException e)
            {
                e.printStackTrace();
            }
        }

        @Override
        protected String doInBackground(Void... voids) {

            saveUserTracks();
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            //Toast.makeText(getApplicationContext(), "file made ", Toast.LENGTH_LONG).show();
        }
    }//end





}