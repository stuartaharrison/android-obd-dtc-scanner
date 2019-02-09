package com.stuartharrison.obdiiscanner.Activities;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.stuartharrison.obdiiscanner.CanvasDraw.LiveDataSurfaceView;
import com.stuartharrison.obdiiscanner.Managers.prefManager;
import com.stuartharrison.obdiiscanner.OBD.OBDChat;
import com.stuartharrison.obdiiscanner.OBD.OBDChatService;
import com.stuartharrison.obdiiscanner.R;

import java.security.MessageDigest;
import java.util.logging.LogRecord;

/**
 * @author Stuart Harrison
 * @version 1.0
 */
@SuppressLint({"HandlerLeak"})
public class LiveDataActivity extends OBDChat {

    /**
     * When the activity is created, do this code
     * @param savedInstanceState The state of the activity
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupViews();
    }

    /**
     * Called when the Activity starts, either from onCreate or resumed
     */
    @Override
    protected void onStart() {
        super.onStart();
        setupViews();
    }

    /**
     * Method called when the activity is created or started, will setup the view, layout objects,
     * management variables, and the OBD chat connection
     */
    private void setupViews() {
        setContentView(R.layout.livefeed_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        surfaceView = new LiveDataSurfaceView(this);
        setContentView(surfaceView);

        this.preferenceManager = new prefManager(this);
        btDeviceAdapter = BluetoothAdapter.getDefaultAdapter();

        if (obdChatService == null) {
            setupOBDChat(); //Setup chat with the cars ECU
        }
    }
}


































