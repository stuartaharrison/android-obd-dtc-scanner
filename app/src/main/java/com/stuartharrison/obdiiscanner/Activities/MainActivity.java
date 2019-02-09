package com.stuartharrison.obdiiscanner.Activities;

import android.annotation.TargetApi;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Build;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.ImageButton;
import android.util.Log;
import android.bluetooth.*;

import com.stuartharrison.obdiiscanner.Dialogues.AgreementDialog;
import com.stuartharrison.obdiiscanner.Dialogues.InfoDialogue;
import com.stuartharrison.obdiiscanner.Managers.dbManager;
import com.stuartharrison.obdiiscanner.Managers.httpManager;
import com.stuartharrison.obdiiscanner.Managers.prefManager;
import com.stuartharrison.obdiiscanner.Objects.Updates;
import com.stuartharrison.obdiiscanner.R;

/**
 * @author Stuart Harrison
 * @version 1.0
 */
public class MainActivity extends MenuActivity implements View.OnClickListener {

    //Variables
    ImageButton btnDiagnostics;
    ImageButton btnLiveData;
    ImageButton btnSavedSearch;
    ImageButton btnMaps;
    ImageButton btnSettings;
    ImageButton btnUpdates;

    /**
     * When the activity is created, do this code
     * @param savedInstanceState The state of the activity
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        PreferenceManager.setDefaultValues(this, R.xml.preference_workshopdata, false);

        preferenceManager = new prefManager(this);
        httpManager = new httpManager(this);

        //Display a Dialog to ask the User to Agree to my terms
        if (!preferenceManager.getApplicationUsageAgreement()) {
            AgreementDialog agreementDialog = new AgreementDialog();
            agreementDialog.setPrefManager(preferenceManager);
            agreementDialog.show(this.getFragmentManager(), "Agree");
        }
        onCreateSetup(); //Call my method for setting everything up on the screen.
    }

    /**
     * The method which is called when an object with an onClickListener event is 'clicked'
     * @param view The object/view that initiated the onClick event
     */
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnDiagnostics:
                //There is no point accessing this next Intent if the User does not have Bluetooth
                //There, it will be easier just to not allow them to continue, to save loads of
                //additional and unnecessary error handling
                if (isBluetoothAvailable()) {
                    Intent diagnosticsInt = new Intent(this, DiagnosticsMainActivity.class);
                    this.startActivity(diagnosticsInt);
                }
                else {
                    InfoDialogue noBT = new InfoDialogue();
                    noBT.setMessage(getResources().getString(R.string.NoBluetoothMessage));
                    noBT.show(this.getFragmentManager(), "No Bluetooth");
                }
                break;
            case R.id.btnLiveData:
                //There is no point accessing this next Intent if the User does not have Bluetooth
                //There, it will be easier just to not allow them to continue, to save loads of
                //additional and unnecessary error handling
                if (isBluetoothAvailable()) {
                    Intent livedataInt = new Intent(this, LiveDataActivity.class);
                    this.startActivity(livedataInt);
                }
                else {
                    InfoDialogue noBT = new InfoDialogue();
                    noBT.setMessage(getResources().getString(R.string.NoBluetoothMessage));
                    noBT.show(this.getFragmentManager(), "No Bluetooth");
                }
                break;
            case R.id.btnSavedDTC:
                break;
            case R.id.btnGaragesMap:
                if (isGPSAvailable()) {
                    Intent mapsIntent = new Intent(this, MapMainActivity.class);
                    this.startActivity(mapsIntent);
                }
                else {
                    InfoDialogue noGPS = new InfoDialogue();
                    noGPS.setMessage(getResources().getString(R.string.NoGPSMessage));
                    noGPS.show(this.getFragmentManager(), "No GPS");
                }
                break;
            case R.id.btnSettings:
                Intent settingsInt = new Intent(this, UserSettingsActivity.class);
                this.startActivity(settingsInt);
                break;
            case R.id.btnUpdates:
                //Only access this screen if the internet is available
                //Pointless screen otherwise, so notify the user they cannot update
                if (isInternetAvailable()) {
                    Intent updatesInt = new Intent(this, UpdatesActivity.class);
                    this.startActivity(updatesInt);
                }
                else {
                    InfoDialogue noInternet = new InfoDialogue();
                    noInternet.setMessage(getResources().getString(R.string.NoInternetMessage));
                    noInternet.show(this.getFragmentManager(), "No Internet");
                }
                break;
        }
    }

    /**
     * Method used to keep the onCreate method small. Sets up the views, database management
     * and checks for any available database updates
     */
    private void onCreateSetup() {
        //DB Management Checks
        try {
            String dbName = preferenceManager.getDatabaseName();
            databaseManager = new dbManager(this, dbName, null, 1);
            databaseManager.dbCreate();
        }
        catch (Exception ex) {
            httpManager.sendErrorMessageToServer("MA-DatabaseManagement", ex.getMessage());
            Log.e("MA-DatabaseManagement", ex.getMessage());
        }

        //BT Configuration
        if (isBluetoothAdapterAvailable() && preferenceManager != null) {
            preferenceManager.setBluetoothEnabled(BluetoothAdapter.getDefaultAdapter().isEnabled());
        }

        //Image Button Setup
        btnDiagnostics = (ImageButton)findViewById(R.id.btnDiagnostics);
        btnDiagnostics.setOnClickListener(this);
        //
        btnLiveData = (ImageButton)findViewById(R.id.btnLiveData);
        btnLiveData.setOnClickListener(this);
        //
        btnSavedSearch = (ImageButton)findViewById(R.id.btnSavedDTC);
        btnSavedSearch.setOnClickListener(this);
        //
        btnMaps = (ImageButton)findViewById(R.id.btnGaragesMap);
        btnMaps.setOnClickListener(this);
        //
        btnSettings = (ImageButton)findViewById(R.id.btnSettings);
        btnSettings.setOnClickListener(this);
        //
        btnUpdates = (ImageButton)findViewById(R.id.btnUpdates);
        btnUpdates.setOnClickListener(this);

        //Should only parse updates if there is an internet connection!
        Updates gotUpdates = getAvailableUpdates(); //Get and Parse the Stream
        if (gotUpdates != null) {
            //Get the current DB version from my preferences
            Updates currentDBVersions = preferenceManager.getDBVersions();
            //Compare the versions
            if (gotUpdates.getDtcDbVersion() > currentDBVersions.getDtcDbVersion() ||
                    gotUpdates.getGarageDbVersion() > currentDBVersions.getGarageDbVersion()) {
                btnUpdates.setImageResource(R.drawable.updatesalert); //This has a '!' symbol
            }
        }
    }
}
