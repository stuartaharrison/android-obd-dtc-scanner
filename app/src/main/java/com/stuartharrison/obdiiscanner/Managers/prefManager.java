package com.stuartharrison.obdiiscanner.Managers;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.stuartharrison.obdiiscanner.Objects.Updates;

/**
 * @author Stuart Harrison
 * @version 1.0
 */
public class prefManager {

    SharedPreferences appPreferences;

    /**
     * Default constructor, doesn't really do anything
     * @param appContext the current application context from the activity declaring this object
     */
    public prefManager(Context appContext) {
        appPreferences = PreferenceManager.getDefaultSharedPreferences(appContext);
    }

    /**
     * Get the device MAC address from the applications shared preferences
     * @return the paired device which the user has selected from the settings
     */
    public String getBluetoothPairedDevice() {
        return appPreferences.getString("btDevice", null);
    }

    /**
     * Sets whether the bluetooth adapter on the application device is enabled or not
     * @param enabled The current state of the bluetooth adapter, true is enabled, otherwise false
     */
    public void setBluetoothEnabled(Boolean enabled) {
        SharedPreferences.Editor editor = appPreferences.edit();
        editor.putBoolean("btEnabled", enabled);
        editor.apply();
    }

    /**
     * Get the name of the database stored in the applications directory on the running android
     * device
     * @return The current name of the database file stored
     */
    public String getDatabaseName() {
        return appPreferences.getString("dbName", null);
    }

    /**
     * Method for getting the current versions of both updatable tables in the running devices
     * application directory database
     * @return The update object containing the version numbers from the preferences
     */
    public Updates getDBVersions() {
        try {
            Updates dbVersion = new Updates();
            int dtcVersion = Integer.parseInt(appPreferences.getString("dtcVersion", "0"));
            int garVersion = Integer.parseInt(appPreferences.getString("garVersion", "0"));
            dbVersion.setDtcDbVersion(dtcVersion);
            dbVersion.setGarageDbVersion(garVersion);

            return dbVersion;
        }
        catch (Exception ex) {
            Log.e("prefManager", ex.getMessage());
            return null;
        }
    }

    /**
     * Method for setting the version of the DTC table in the current applications working directory
     * database and store it in the user preference for ease of access
     * @param version The new version of the table
     */
    public void setDTCDatabaseVersion(int version) {
        SharedPreferences.Editor editor = appPreferences.edit();
        editor.putString("dtcVersion", String.valueOf(version));
        editor.apply();
    }

    /**
     * Method for setting the version of the Maps table in the current applications working directory
     * database and store it in the user preference for ease of access
     * @param version The new version of the table
     */
    public void setMAPDatabaseVersion(int version) {
        SharedPreferences.Editor editor = appPreferences.edit();
        editor.putString("garVersion", String.valueOf(version));
        editor.apply();
    }

    /**
     * Method for getting the value from the shared preferences on whether sending updates to the
     * developer feature has been enabled
     * @return Returns true if the feature is enabled, otherwise false Defaults to always true
     */
    public Boolean getSendErrorMessages() {
        //TODO: Should this be revised? Opt in rather than opt out?
        return appPreferences.getBoolean("debugSend", true);
    }

    /**
     * Method for getting whether the user has accepted the terms from the applications
     * shared preferences
     * @return Returns true if the terms have been accepted, false otherwise. Defaults to false
     */
    public Boolean getApplicationUsageAgreement() {
        return appPreferences.getBoolean("agreement", false);
    }

    /**
     * Method for setting the user preference whether they have accepted the terms of the application
     * @param agreement True if the user has agreed to the terms, otherwise false
     */
    public void setApplicationUsageAgreement(Boolean agreement) {
        SharedPreferences.Editor editor = appPreferences.edit();
        editor.putBoolean("agreement", agreement);
        editor.apply();
    }
}
