package com.stuartharrison.obdiiscanner.Activities;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.stuartharrison.obdiiscanner.Managers.dbManager;
import com.stuartharrison.obdiiscanner.Managers.httpManager;
import com.stuartharrison.obdiiscanner.Managers.prefManager;
import com.stuartharrison.obdiiscanner.Objects.Garage;
import com.stuartharrison.obdiiscanner.R;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Stuart Harrison
 * @version 1.0
 */
public class MapMainActivity extends MenuActivity  {

    //Variables
    public GoogleMap myMap;

    /**
     * When the activity is created, do this code
     * @param savedInstanceState The state of the activity
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.map_main);
        preferenceManager = new prefManager(this);
        httpManager = new httpManager(this);
        //Setup the Map and Markers once the managers and view has been established
        setupMap();
        setupMarkers();
    }

    /**
     * Method for doing initial map setup and displaying it on the screen
     */
    private void setupMap() {
        try {
            myMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
            myMap.setMyLocationEnabled(true);
            myMap.setMapType(GoogleMap.MAP_TYPE_NORMAL); //Normal map is fine for this
            LatLng myLocation = getPosition(); //Get my position
            myMap.moveCamera(CameraUpdateFactory.newLatLng(myLocation)); //Move camera to my position
            myMap.animateCamera(CameraUpdateFactory.zoomTo(14)); //Zoom in on my position
            myMap.addMarker(new MarkerOptions().position(myLocation) //Place a marker to say you are here!
                    .title("You are here!")
                    .snippet(""));
        }
        catch (Exception ex) {
            if (ex.getMessage() != null) {
                Log.e("MapMainActivity", ex.getMessage());
                httpManager.sendErrorMessageToServer("MapMainActivity", ex.getMessage());
            }
            else {
                Log.e("MapMainActivity", "Unknown Error");
                httpManager.sendErrorMessageToServer("MapMainActivity", "Unknown Error");
            }
        }
    }

    /**
     * Method for 'pulling' an array of Garage locations from the applications database and
     * displaying each as a marker on the map to the user. Called after the setupMap method
     */
    private void setupMarkers() {
        try {
            //Create db connection
            databaseManager = new dbManager(this, preferenceManager.getDatabaseName(), null, 1);
            databaseManager.dbCreate();
            HashMap<String, Garage> markerList = databaseManager.getMapsList(); //Get list of manufacturers

            if (markerList.size() == 0) {
                return;
            } //If there is no results returned by the query, just return no point continuing

            double latitude, longitude; //Setup variables
            //Foreach entry in my dictionary array
            for (Map.Entry<String, Garage> entry : markerList.entrySet()) {
                //Assign lat and lang from each Garage object
                latitude = entry.getValue().getLatitude();
                longitude = entry.getValue().getLongitude();
                //Place marker on the map at the right location with some relevant information
                myMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude))
                        .title(entry.getValue().getName())
                        .snippet(entry.getValue().getContactNumber()));
            }
        }
        catch (Exception ex) {
            Log.e("MapMainActivity", ex.getMessage());
            httpManager.sendErrorMessageToServer("MapMainActivity", ex.getMessage());
        }
    }

    /**
     * Method for getting the current position of the user based on the location services
     * manager, which is part of the phone. This could be GPS, mobile signal location, or wifi
     * location
     * @return The latitude and longitude as a LatLng object of the users current position
     */
    public LatLng getPosition() {
        try {
            //Setup location manager and criteria
            LocationManager locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
            Criteria criteria = new Criteria();
            String provider = locationManager.getBestProvider(criteria, true); //Get provider
            Location myLocation = locationManager.getLastKnownLocation(provider); //Get location

            //Get my location
            double latitude = myLocation.getLatitude();
            double longitude = myLocation.getLongitude();

            return new LatLng(latitude, longitude); //Return my location to the method caller
        }
        catch (SecurityException ex) {
            Log.e("MapMainActivity", ex.getMessage());
            httpManager.sendErrorMessageToServer("MapMainActivity", ex.getMessage());
            return new LatLng(0, 0); //Just return something, so make it 0, 0!
        }
    }
}
