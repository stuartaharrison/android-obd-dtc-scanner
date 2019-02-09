package com.stuartharrison.obdiiscanner.Activities;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.stuartharrison.obdiiscanner.Dialogues.InfoDialogue;
import com.stuartharrison.obdiiscanner.Managers.dbManager;
import com.stuartharrison.obdiiscanner.Managers.httpManager;
import com.stuartharrison.obdiiscanner.Managers.prefManager;
import com.stuartharrison.obdiiscanner.Objects.Updates;
import com.stuartharrison.obdiiscanner.Parser.xmlDownloader;
import com.stuartharrison.obdiiscanner.Parser.xmlParser;
import com.stuartharrison.obdiiscanner.R;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * @author Stuart Harrison
 * @version 2.0
 *
 * The superclass that inherits the main Activity class, which all other activities made in this
 * application will inherit from. This is so that any changes to the menu or the way in which we
 * check for updates, internet & bluetooth available will be implemented throughout the entire
 * application.
 */
public class MenuActivity extends AppCompatActivity {

    //Protected variables to be accessed by the sub-classes
    protected dbManager databaseManager;
    protected httpManager httpManager;
    protected prefManager preferenceManager;

    /**
     * Creates the menu bar at the top of the activity/screen
     * @param menu The menu object
     * @return returns true once all other operations have concluded
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.obd_menu, menu);
        return true;
    }

    /**
     * Fired when a menu item is selected, will perform different actions based on the item
     * selected
     * @param item The menu item which was tapped/clicked/selected
     * @return returns whether an option was selected or not
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menHome:
                Intent mainMenuInt = new Intent(this, MainActivity.class);
                this.startActivity(mainMenuInt);
                return true;
            case R.id.menSettings:
                Intent settingsMenu = new Intent(this, UserSettingsActivity.class);
                this.startActivity(settingsMenu);
                return true;
            case R.id.menAbout:
                InfoDialogue aboutDialogue = new InfoDialogue();
                aboutDialogue.setMessage(getResources().getString(R.string.AboutMessage));
                aboutDialogue.show(this.getFragmentManager(), "AboutApp");
                return true;
            case R.id.menQuit:
                this.finish();
                System.exit(0);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Method to check whether the device running the application supports Bluetooth or not
     * @return Returns true is a bluetooth adapter exists, otherwise false
     */
    public Boolean isBluetoothAdapterAvailable() {
        BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
        if (btAdapter != null) { //Check the device supports Bluetooth
            return true;
        }
        else { return false; }
    }

    /**
     * Method to check that the following are available;
     * Bluetooth is supported on the device, it is enabled, and there is a paired device selected
     * from the settings menu
     * @return Returns true if the conditions are met, otherwise false
     */
    public Boolean isBluetoothAvailable() {
        //Get shared preferences, there is no point using the prefManager class as I would
        //have to redeclare it to prevent any issues with re-declaring it inside this method
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String pairedDevice = preferences.getString("btDevice", null); //Check I have set a paired device
        if (preferences.getBoolean("btEnabled", false) && pairedDevice != null) {
            return true; //Both BT is enabled and I have a paired device set
        }
        else { return false; }
    }

    /**
     * Method to check whether the device currently running the application supports GPS and
     * locating the users position
     * @return Returns true if the feature is supported, otherwise false
     */
    public Boolean isGPSAvailable() {
        LocationManager locationManager = (LocationManager)this.getSystemService(Context.LOCATION_SERVICE);
        Boolean gps_enabled;
        Boolean network_enabled;

        try {
            //Check for GPS location, then check for mobile courier network positioning
            gps_enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            network_enabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            if (!gps_enabled && !network_enabled) {
                return  false; //Both are false, therefore cannot get position
            }
            else { return true; } //All is good, return true
        }
        catch (Exception ex) {
            Log.e("MenuActivity", ex.getMessage());
            return false;
        }
    }

    /**
     * Method used for checking whether the device is currently connected to the internet, in order
     * to get the updates from the web-server. Checks all variations of connectivity!
     * @return True if there is a valid connection to the internet, otherwise false
     */
    public Boolean isInternetAvailable() {
        //Get connectivity manager
        ConnectivityManager connectivityManager = (ConnectivityManager)getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) { //No way of connecting to the internet
            NetworkInfo[] info = connectivityManager.getAllNetworkInfo(); //Get all network devices
            if (info != null) { //Found some devices
                //Check each to see if any are connected
                for (int i = 0; i < info.length; i++) {
                    if (info[i].getState() == NetworkInfo.State.CONNECTED) {
                        return true; //Found one that is connected, we are connected!
                    }
                }
                return  false; //Not connected to the internet, but has a network device
            }
        }
        return  false; //Return false, cannot connect to the internet
    }

    /**
     * Gets the current DB versions on the web-server to compare with the current applications
     * version
     * @return The object which holds the DB versions held on the server-end, otherwise null if
     * and error occurs
     */
    public Updates getAvailableUpdates() {
        final SharedPreferences appPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        final httpManager httpManager = new httpManager(this);

        if (isInternetAvailable()) {
            try {
                Updates gotUpdates = null;
                //Perform Async Task to get the Stream Data
                AsyncUpdateParser parser = new AsyncUpdateParser();
                String rssData = parser.execute().get();
                if (rssData != null) {
                    //Parse the stream, looking for the my update tags
                    xmlParser xmlParse = new xmlParser();
                    gotUpdates = xmlParse.parseXMLUpdateString(rssData); //Create object
                } else {
                    Log.e("Parser", "Unable to Download Stream");
                }
                return gotUpdates;
            } catch (Exception ex) {
                if (appPreferences.getBoolean("debugSend", true)) {
                    httpManager.sendErrorMessageToServer("MA-CheckUpdates", ex.getMessage());
                }
                Log.e("MA-CheckUpdates", ex.getMessage());
                return null;
            }
        } else { return null; }
    }

    /**
     * Downloads the RSS Feed from the web-server. Gets the XML file as a String variable
     * that contains the information on the current DB versions at the server-end
     */
    private class AsyncUpdateParser extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... voids) {
            String result = "";
            try {
                xmlDownloader downloader = new xmlDownloader(0);
                result = downloader.getStringFromStream();
                Log.i("RSS", "GOT: " + result);
            } catch (Exception e) {
                Log.i("RSS", "ERROR: " + e.getMessage());
            }
            return result;
        }

        @Override
        protected void onPostExecute(String rssFeed) {
            if (rssFeed == null || rssFeed.equals("")) {
                Log.i("RSS", "EMPTY FEED");
            }
            else {
                Log.i("RSS", rssFeed);
            }
        }
    }
}
