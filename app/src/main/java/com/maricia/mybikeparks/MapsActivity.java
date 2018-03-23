package com.maricia.mybikeparks;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
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
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);




        PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment) getFragmentManager().findFragmentById(R.id.autocomplete_fragment);
        autocompleteFragment.setHint("Search for Address");

        //autocompleteFragment.setFilter(filter);
        autocompleteFragment.setOnPlaceSelectedListener(this);


        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkLocationPermission();
        }
        // Obtain the SupportMapFragment and get notified when the mMap is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
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

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                buildGoogleApiClient();
                mMap.setMyLocationEnabled(true);
            }

    }//end onMapReady

    public void getTextView(){
     //   locationTextField = this.findViewById(R.id.locationTextField);//floating_search_view
     //   searchButton = this.findViewById(R.id.searchButton);//floating_search_view
    }



    public void searchResults(String string){

       // InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);   //hide the keyboard when button is pressed
       // imm.hideSoftInputFromWindow(locationTextField.getWindowToken(), 0);

        //EditText locationTextField = (EditText) findViewById(R.id.locationTextField);
       // String searchLocations = locationTextField.getText().toString();    //get text from text view



        List<Address> addressList = null;
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
            case R.id.parksButton:
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



    @Override
    public void onConnected(Bundle bundle) {

        locationRequest = new LocationRequest();
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, locationRequest, this);
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
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(18));

        //stop location updates
        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

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
                    }

                } else {

                    // Permission denied, Disable the functionality that depends on this permission.
                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show();
                }
                return;
            }

            // other 'case' lines to check for other permissions this app might request.
            // You can add here other case statements according to your requirement.
        }
    }


    @Override
    public void onPlaceSelected(Place place) {


        Log.d(TAG, "onPlaceSelected: *********" + place);
        List<Address> addressList = null;
        try {
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
}

