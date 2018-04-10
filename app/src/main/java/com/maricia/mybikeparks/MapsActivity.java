package com.maricia.mybikeparks;

import android.Manifest;
import android.app.ActionBar;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Looper;
import android.os.SystemClock;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.arlib.floatingsearchview.FloatingSearchView;
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

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
//FragmentActivity
public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks, OnCameraMoveStartedListener,
        GoogleApiClient.OnConnectionFailedListener, PlaceSelectionListener,
        LocationListener, SaveTrackDialogFragment.NoticeDialogListener {

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
    private long UPDATE_INTERVAL = 1000; // 1 Seconds
    private long FASTEST_INTERVAL = 1000; // 1 Seconds
    private int count = 0; //count the number of saved files
    private Chronometer timmer; //timer for activity
    private String howLong; //activity time


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        //Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);
        //google search bar
        addSearch();
        locationPermision();
        startMap();

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
                Toast.makeText(MapsActivity.this, "YOUR DESIRED BEHAVIOUR HERE", Toast.LENGTH_SHORT).show();
                break;
            case R.id.action_Stop:
                stopTiming();
                stopTracking();
                break;
            case R.id.action_Start:
                startTiming();
                startTracking();
                break;
            case R.id.action_about:
                Intent intentAbout =  new Intent (this, AboutActivity.class);
                startActivity(intentAbout);
                break;
        }
       return super.onOptionsItemSelected(item);
    }

    private void stopTiming() {
        String howLong =  timmer.getText().toString();
        Toast.makeText(this, "howLong: " + howLong, Toast.LENGTH_LONG).show();
        timmer.stop();
        //TODO save time with the walkroute
    }

    private void startTiming() {
        Log.d(TAG, "startTiming: Stsrtnow");
      // Chronometer timmer = (Chronometer) findViewById(R.id.timmer);
       timmer = (Chronometer) findViewById(R.id.timmer);
       timmer.setBase(SystemClock.elapsedRealtime()); //reset timmer
       timmer.start(); //start
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
                Toast.makeText(MapsActivity.this, "MTB Parks", Toast.LENGTH_LONG).show();
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

    public boolean fileExist(String filename){
        Log.d(TAG, "fileExist: " + filename);
        File file = getBaseContext().getFileStreamPath(filename);
        return file.exists();
    }
    
    

    @Override
    public void onDialogPositiveClick(DialogFragment dialog)
    {
        // User touched the dialog's positive button
        //save points to a locale memory space - later this need to be updated to a database of some sort
        SaveUserTracks();
        //remove the path on the screen
        finishTracking();
    }

    private void SaveUserTracks() {

        String filename = "walkroutes";
        FileOutputStream ops;

        //if file exists already and adds one
        if(fileExist(filename)){
            count++;
            filename = new StringBuilder().append(filename).append(count).toString();
            Log.d(TAG, "onDialogPositiveClick: " + filename);
        }else{
            filename = "walkroutes";
        }
        //make new file and save locations hopefully I will be able to do a layout on the map 
        //with this saved file.
        //TODO display map overlay using corridantes in saved file
        try {
            ops = openFileOutput(filename, this.MODE_PRIVATE);
            for(int i = 0; i < points.size(); i++ ){
                Log.d(TAG, "addnewOverlayhere: uhmmm: " + points.get(i));
                ops.write(this.points.indexOf(i));
            }
            ops.close();
        } catch (IOException e)
        {
            e.printStackTrace();
        }
        
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {
        // User touched the dialog's negative button
        // we just gunna delete the path
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

        //move mMap camera
        if (isFollowing) { // Todo: Find a way to detect if the user moves the map and toggle isFollowing if they are more interested in looking at another location.
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(18));
        }
        if (isTracking) {recordPath(latLng);}
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

    public void getTextView(){
        //some kind of timmer goes here so we can see how long we have active
        //   locationTextField = this.findViewById(R.id.locationTextField);//floating_search_view
        //   searchButton = this.findViewById(R.id.searchButton);//floating_search_view
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

    private void finishTracking() //clear the polylines drawn while tracking;
    {
        isTracking = false;
        mMap.clear(); // ToDo: if there is a way to clear the lines without using the clear method
        getLastKnownLocation(); // we should probably switch to that


    }

    private void recordPath(LatLng loc) // couple lines of code that draws users position over time
    {
        points.add(loc);
        polylineOptions.add(loc);
        polyline = mMap.addPolyline(polylineOptions);
        Log.d(TAG, "recordPath: ****" + points);

    }

    private void stopTracking() //creates a Dialog box to get user's choice about what to do about the path traveled
    {
        if (!isTracking) return;
        ArrayList<LatLng> myLocation = points;  //points
        DialogFragment newFragment = new SaveTrackDialogFragment();
        newFragment.show(getFragmentManager(), "missiles");
        addnewOverlayhere(myLocation);
    }

    public void addnewOverlayhere(ArrayList<LatLng> myLocation) {
      //TODO add tracks overlay and make activity summary page

    }



    private void startTracking() // initialize everything needed for the recordPath Function
    {
        isTracking = true;
        points = new ArrayList<LatLng>();
        polylineOptions = new PolylineOptions();
        polylineOptions.color(Color.BLUE);
        polylineOptions.width(5);

    }


    protected void startLocationUpdates() {
        //The Location requests and makes it start recieving updates
        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(UPDATE_INTERVAL);
        locationRequest.setFastestInterval(FASTEST_INTERVAL);


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


}