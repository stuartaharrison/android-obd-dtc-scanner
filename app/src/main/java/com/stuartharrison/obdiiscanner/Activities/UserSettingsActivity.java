package com.stuartharrison.obdiiscanner.Activities;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.util.Log;

import com.stuartharrison.obdiiscanner.Managers.httpManager;
import com.stuartharrison.obdiiscanner.Managers.prefManager;
import com.stuartharrison.obdiiscanner.R;

import java.util.ArrayList;
import java.util.Set;

/**
 * @author Stuart Harrison
 * @version 1.0
 */
public class UserSettingsActivity extends MenuActivity {

    /**
     * When the activity is created, do this code
     * @param savedInstanceState The state of the activity
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        //Setup managers from Superclass
        preferenceManager = new prefManager(this);
        httpManager = new httpManager(this);
        //Try setting up my actionbar
        try {
            android.support.v7.app.ActionBar actionBar = getSupportActionBar();
            assert actionBar != null;
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        catch (Exception ex) {
            Log.e("UserSettingsActivity", ex.getMessage());
            httpManager.sendErrorMessageToServer("UserSettingsActivity", ex.getMessage());
        }
        //Replace the current screen with the fragment class in this class
        getFragmentManager().beginTransaction().replace(android.R.id.content, new SettingsFragment()).commit();
    }

    /**
     * The Fragment Display for display of the Settings
     * TODO: Change the passing of the AppContext to a Bundle
     */
    public static class SettingsFragment extends PreferenceFragment implements
             Preference.OnPreferenceChangeListener {

        //Variables
        httpManager httpManager;
        //Context appContext;

        /**
         * Default constructor
         * @param appContext Context of the current application
         */
        //public SettingsFragment(Context appContext) {
            //this.appContext = appContext;
        //}

        /**
         * When the activity/fragment is created, do this code
         * @param savedInstanceState The state of the activity
         */
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preference_workshopdata); //Add my preferences
            //httpManager = new httpManager(appContext);
            //Foreach preference I have, get the preference, and display it with a separate
            //method call
            for (int i = 0; i < getPreferenceScreen().getPreferenceCount(); i++) {
                Preference preference = getPreferenceScreen().getPreference(i);
                pickPreferenceObject(preference);
            }

            bluetoothDeviceDiscovery(); //Get a list of paired bluetooth devices for the device
        }

        /**
         * Method which is 'fired' when the user changes a preference value on the screen. Will
         * change the preference summary equal to the newly changed value so that the user can
         * see the change has happened, and what the value is
         * @param preference The preference which has been changed
         * @param newValue The new value for that preference
         * @return returns true when the method has completed, false if any exceptions have been
         * caught
         */
        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            // Workshop and User Preferences
            if (preference.getKey().equals("name")) {
                preference.setSummary(newValue.toString());
            }
            if (preference.getKey().equals("workshop")) {
                preference.setSummary(newValue.toString());
            }
            //Bluetooth enabling and disabling the adapter by when the checkbox is checked
            //or un-checked
            if (preference.getKey().equals("btEnabled")) {
                try {
                    //Covert my the 'new value' into a boolean
                    Boolean value = Boolean.parseBoolean(newValue.toString());
                    BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter(); //Get Adapter
                    if (btAdapter != null) { //If we have an adapter
                        //I could use the isBluetoothAvailable in the MenuActivity
                        if (value) { //If the new Value is true, enable bluetooth
                            btAdapter.enable();
                        }
                        else { //Otherwise disable bluetooth
                            btAdapter.disable();
                        }
                    }
                }
                catch (Exception ex) {
                    return  false;
                }
            }
            //Bluetooth Paired Device
            if (preference.getKey().equals("btDevice")) {
                preference.setSummary(newValue.toString());
            }
            return true;
        }

        /**
         * Method for checking preference is not a category before display. Will re-call itself
         * when the the preference is identified as category instead of an actual preference
         * @param p The preference being checked
         */
        private void pickPreferenceObject(Preference p) {
            if (p instanceof PreferenceCategory) { //Check if the preference is a category
                PreferenceCategory cat = (PreferenceCategory) p;
                //Foreach preference inside the category
                for (int i = 0; i < cat.getPreferenceCount(); i++) {
                    pickPreferenceObject(cat.getPreference(i)); //Recheck the preference
                }
            }
            else {
                initSummary(p); //Otherwise it is not a category, we can set it up for display
            }
        }

        /**
         * Method for displaying the preference. Checks whether the preference is an edit-text,
         * checkbox or listPreference (bluetooth devices appears in a list) and sets the summary
         * and on preference changed listener for each preference
         * @param p The preference to be displayed
         */
        private void initSummary(Preference p) {
            try {
                if (p instanceof EditTextPreference) {
                    //Get the preference as an edit text
                    EditTextPreference editTextPreference = (EditTextPreference) p;
                    p.setOnPreferenceChangeListener(this);
                    p.setSummary(editTextPreference.getText());
                }
                if (p instanceof CheckBoxPreference) {
                    p.setOnPreferenceChangeListener(this);
                }
                if (p instanceof ListPreference) {
                    ListPreference listPreference = (ListPreference) p;
                    p.setOnPreferenceChangeListener(this);
                    p.setSummary(listPreference.getValue());
                }
            }
            catch (Exception ex) {
                Log.i("SettingsSummaryError", ex.getMessage());
                //httpManager.sendErrorMessageToServer("SettingsSummaryError", ex.getMessage());
            }
        }

        /**
         * Method for getting a list of Bluetooth paired devices with the device the application
         * is being run on. So when the user selects the list preference changed, a list of selectable
         * paired bluetooth devices show
         */
        private void bluetoothDeviceDiscovery() {
            //Setup my arrays
            ArrayList<CharSequence> btPairedDevices = new ArrayList<>();
            ArrayList<CharSequence> btVals = new ArrayList<>();
            //Get the current list
            ListPreference btListDevices = (ListPreference)getPreferenceScreen().findPreference("btDevice");
            //Check the device actually has a Bluetooth Adapter
            //I cannot use the MenuActivity check because this doesn't extend that class,
            //therefore this check needs to be done long!
            final BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
            if (btAdapter == null) {
                btListDevices.setEntries(btPairedDevices.toArray(new CharSequence[0]));
                btListDevices.setEntryValues(btVals.toArray(new CharSequence[0]));
                return; //No Adapter, no point in continuing
            }
            //Bluetooth preferences click response
            btListDevices.setEntries(new CharSequence[1]);
            btListDevices.setEntryValues(new CharSequence[1]);
            btListDevices.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {
                    // see what I mean in the previous comment?
                    if (btAdapter == null || !btAdapter.isEnabled()) {
                        return false;
                    }
                    return true;
                }
            });
            //Get Bluetooth Paired Devices List and populate my Preference List
            Set<BluetoothDevice> pairedDevices = btAdapter.getBondedDevices();
            if (pairedDevices.size() > 0) {
                for (BluetoothDevice device : pairedDevices) {
                    btPairedDevices.add(device.getName() + "\n" + device.getAddress());
                    btVals.add(device.getAddress());
                }
            }
            btListDevices.setEntries(btPairedDevices.toArray(new CharSequence[0]));
            btListDevices.setEntryValues(btVals.toArray(new CharSequence[0]));
        }
    }
}
