package com.maricia.mybikeparks;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.arlib.floatingsearchview.FloatingSearchView;
import com.arlib.floatingsearchview.suggestions.model.SearchSuggestion;
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
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, PlaceSelectionListener,
        LocationListener {

    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest locationRequest;
    private Location lastLocation;
    private Marker currLocationMarker;

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    private Button searchButton;
    private TextView locationTextField;
    int PROXIMITY_RADIUS = 10000;
    double latitude;
    double longitude;
    private static final String TAG = "MapsActivity";
    private FloatingSearchView mSearchView;
    private PlaceAutocompleteFragment autocompleteFragment;
    private Boolean followUser; // This boolean will toggle if the map moves when the user's location changes
    private LocationManager mLocationManager;
    private long UPDATE_INTERVAL = 1000 * 60; // 60 Seconds
    private long FASTEST_INTERVAL = 1000; // 1 Seconds
    boolean dolocationupdate = false; //only update my location when I tell you too

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);



        autocompleteFragment = (PlaceAutocompleteFragment) getFragmentManager().findFragmentById(R.id.autocomplete_fragment);
        autocompleteFragment.setHint("Enter Address, City or Zip Code");
        autocompleteFragment.setOnPlaceSelectedListener(this);
        autocompleteFragment.setFilter( new AutocompleteFilter.Builder().setCountry("US").build()); //I don't think this is working



        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkLocationPermission();
        }
        // Obtain the SupportMapFragment and get notified when the mMap is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        startLocationUpdates();
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
        followUser = true;
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
            buildGoogleApiClient();
            getLastKnownLocation();
        }

    }//end onMapReady

    //to handle search button when user clicks on it
    public void onClick(View v){

        //check what button is being pressed
        Object dataTransfer[] = new Object[2];
        GetNearbyPlacesData getNearbyPlacesData = new GetNearbyPlacesData();

        switch (v.getId()){//searchButton

            /*
            case R.id.floating_search_view:
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);   //hide the keyboard when button is pressed
                imm.hideSoftInputFromWindow(locationTextField.getWindowToken(), 0);

                EditText locationTextField = (EditText) findViewById(R.id.locationTextField);
                String searchLocations = locationTextField.getText().toString();    //get text from text view

                List<Address> addressList = null;
                try {
                    if (!searchLocations.equals("")) {
                        //use geocoder class to get names
                        Geocoder geocoder = new Geocoder(this);
                        addressList = geocoder.getFromLocationName(searchLocations, 5);      //returns as array of addresses maybe a place name, address, or airport code
                          if(addressList != null){
                              //put a marker on all the places searched
                              for (int i = 0; i < addressList.size(); i++) {
                                  Address myAddress = addressList.get(i);
                                  LatLng latLng = new LatLng(myAddress.getLatitude(), myAddress.getLongitude());
                                  MarkerOptions markerOptions = new MarkerOptions();
                                  markerOptions.position(latLng);
                                  markerOptions.title(lastLocation.toString());
                                  mMap.addMarker(markerOptions);
                                  markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
                                  //optional - if not then camera will go to last place listed on mMap
                                  mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
                              }
                          }
                    }//end if
                } catch (IOException e) {
                    e.printStackTrace();
                }
                    break;
                    */
            default:Toast.makeText(this,v.getId()+"", Toast.LENGTH_LONG).show();
            case R.id.parksButton:
                mMap.clear();
                String park = "park";
                String url = getUrl(latitude, longitude, "park");

                dataTransfer[0] = mMap;
                dataTransfer[1] = url;
                getNearbyPlacesData.execute(dataTransfer);
                Log.d(TAG, "onClick: " );
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
        Log.d(TAG, "onConnected: startlocation *****" + locationRequest.toString());
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            startLocationUpdates();
        }


    }

    @Override
    public void onConnectionSuspended(int i) {

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
        if (followUser) { // Todo: Find a way to detect if the user moves the map and toggle followUser if they are more interested in looking at another location.
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(18));
        }

        //stop location updates todo: delete this if statement or move it somewhere more appropriate
        /*
        this if statement removes the location updates we need in order to track the user
        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }*/
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

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

    @Override
    public void onPlaceSelected(Place place) {


        Log.d(TAG, "onPlaceSelected: *********" + place);
        List<Address> addressList;
        try {
            //Just wanna make sure this if statement works. can you use .equals between a Place object and a String
            if (!place.equals("")) {
                //use geocoder class to get names
                Geocoder geocoder = new Geocoder(this);
                addressList = geocoder.getFromLocationName(place.getName().toString(), 5);      //returns as array of addresses maybe a place name, address, or airport code
                Log.d(TAG, "onPlaceSelected: address list: " + addressList);

                if(addressList != null){
                    //put a marker on all the places searched
                    for (int i = 0; i < addressList.size(); i++) {
                        Address myAddress = addressList.get(i);
                        LatLng latLng = new LatLng(myAddress.getLatitude(), myAddress.getLongitude());
                        MarkerOptions markerOptions = new MarkerOptions();
                        markerOptions.position(latLng);
                        markerOptions.title(lastLocation.toString());
                        mMap.addMarker(markerOptions);
                        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
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
    public void onError(Status status) {

        Toast.makeText(this, "OOPS SOMETHING WENT WRONG", Toast.LENGTH_LONG).show();
    }

    public void getTextView(){
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

        Log.d(TAG, "getLastKnownLocation: " + locationClient.getLastLocation().toString());
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
        }); }

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

    public void searchResults(String string){

        // InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);   //hide the keyboard when button is pressed
        // imm.hideSoftInputFromWindow(locationTextField.getWindowToken(), 0);

        //EditText locationTextField = (EditText) findViewById(R.id.locationTextField);
        // String searchLocations = locationTextField.getText().toString();    //get text from text view



        List<Address> addressList;
        try {
            if (!string.equals("")) {
                //use geocoder class to get names
                Geocoder geocoder = new Geocoder(this);
                addressList = geocoder.getFromLocationName(string, 5);      //returns as array of addresses maybe a place name, address, or airport code
                if(addressList != null){
                    //put a marker on all the places searched
                    for (int i = 0; i < addressList.size(); i++) {
                        Address myAddress = addressList.get(i);
                        LatLng latLng = new LatLng(myAddress.getLatitude(), myAddress.getLongitude());
                        MarkerOptions markerOptions = new MarkerOptions();
                        markerOptions.position(latLng);
                        markerOptions.title(lastLocation.toString());
                        mMap.addMarker(markerOptions);
                        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
                        //optional - if not then camera will go to last place listed on mMap
                        mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
                    }
                }
            }//end if
        } catch (IOException e) {
            e.printStackTrace();
        }
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


        if(dolocationupdate) {
            Log.d("MapDemoActivity", "WHAT AM I DOING");
            // We check if the system is capable of sending/receiving location requests
            SettingsClient settingsClient = LocationServices.getSettingsClient(this);
            settingsClient.checkLocationSettings(locationSettingsRequest);

            if (checkLocationPermission()) {

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
}

