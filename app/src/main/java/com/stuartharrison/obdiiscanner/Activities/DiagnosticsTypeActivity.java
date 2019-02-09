package com.stuartharrison.obdiiscanner.Activities;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import com.stuartharrison.obdiiscanner.Managers.prefManager;
import com.stuartharrison.obdiiscanner.OBD.OBDChat;
import com.stuartharrison.obdiiscanner.Objects.Manufacturer;
import com.stuartharrison.obdiiscanner.R;

/**
 * @author Stuart Harrison
 * @version 1.0
 */
public class DiagnosticsTypeActivity extends OBDChat implements View.OnClickListener {

    //Variables
    Manufacturer selectManufacturer;
    ImageButton btnAllCodes;
    ImageButton btnEngine;
    ImageButton btnBody;
    ImageButton btnChassis;
    ImageButton btnNetwork;
    ImageButton btnQuick;

    /**
     * When the activity is created, do this code
     * @param savedInstanceState The state of the activity
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.diagnostics_type);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        onCreateSetup(); //Call my method for setting everything up on the screen.
    }

    /**
     * Method for setting up all the layouts and application managers, called separately from
     * the onCreate to keep everything tidy.
     */
    private void onCreateSetup() {
        //Image Button Setup
        btnAllCodes = (ImageButton)findViewById(R.id.btnAllCodes);
        btnAllCodes.setOnClickListener(this);
        btnEngine = (ImageButton)findViewById(R.id.btnEngine);
        btnEngine.setOnClickListener(this);
        btnBody = (ImageButton)findViewById(R.id.btnBody);
        btnBody.setOnClickListener(this);
        btnChassis = (ImageButton)findViewById(R.id.btnChassis);
        btnChassis.setOnClickListener(this);
        btnNetwork = (ImageButton)findViewById(R.id.btnNetwork);
        btnNetwork.setOnClickListener(this);
        btnQuick = (ImageButton)findViewById(R.id.btnQuickReset);
        btnQuick.setOnClickListener(this);

        //Get the Manufacturer
        Intent iMainAct = getIntent();
        selectManufacturer = (Manufacturer)iMainAct.getSerializableExtra("manufacturer");

        this.preferenceManager = new prefManager(this);
        btDeviceAdapter = BluetoothAdapter.getDefaultAdapter();

        if (obdChatService == null) {
            setupOBDChat(); //Setup a chat to the ECU
        }
    }

    /**
     * The method which is called when an object with an onClickListener event is 'clicked'
     * @param view The object/view that initiated the onClick event
     */
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnAllCodes:
                break;
            case R.id.btnEngine:
                break;
            case R.id.btnBody:
                break;
            case R.id.btnChassis:
                break;
            case R.id.btnNetwork:
                break;
            case R.id.btnQuickReset:
                clearCodes();
                break;
        }
    }
}
