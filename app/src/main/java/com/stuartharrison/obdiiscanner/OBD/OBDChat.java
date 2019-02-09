package com.stuartharrison.obdiiscanner.OBD;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.stuartharrison.obdiiscanner.Activities.MenuActivity;
import com.stuartharrison.obdiiscanner.CanvasDraw.LiveDataSurfaceView;

@SuppressLint({"HandlerLeak"})
public class OBDChat extends MenuActivity {

    //Variables
    public BluetoothAdapter btDeviceAdapter;
    public OBDChatService obdChatService;
    public LiveDataSurfaceView surfaceView;
    public int messageNumber = 1;
    public String btConnectedDeviceName;
    public StringBuffer btOutStringBuffer;

    /**
     * The main communication tool for the two connected devices. Handles what the activity should
     * do when a message is obtained from the ECU. Also handles when the ChatService state changes
     * due to dropped connection, ect.
     */
    public final Handler btHandler = new Handler() {
        public void handleMessage(Message message) {
            int value = 0;
            int value2 = 0;
            int PID = 0;
            switch (message.what) {
                case 1: //When the ChatService State changes
                    Log.d("LiveDataActivity", "Message State Changed: " + message.arg1);
                case 2: //Reading data that is gotten from the ELM327 device
                    //Get the byte data
                    byte[] readBuffer = (byte[])message.obj;
                    String[] volt_number1;
                    if (readBuffer == null) { return; }
                    //Format byte array into a string we can use
                    String readMessage = new String(readBuffer, 0, message.arg1);
                    //Variables we need for calculating data, ect
                    String receivedData;
                    String volt_value2;
                    //Different vehicles will have different ECU outputs, need to handle all possible
                    //combinations, or at least as many as we can
                    if (readMessage != null && readMessage.matches("\\s*[0-9A-Fa-f]{2} [0-9A-Fa-f]{2}\\s*\r?\n?")) {
                        receivedData = readMessage.trim(); //Trim the data of white space/noise
                        volt_number1 = receivedData.split(" "); //Split up the message
                        //PID will identify which piece of information we got from the ECU
                        //volt number will contain the value
                        if (volt_number1[0] != null && volt_number1[1] != null) {
                            PID = Integer.parseInt(volt_number1[0].trim(), 16);
                            value = Integer.parseInt(volt_number1[1].trim(), 16);
                        }
                        Log.d("LiveDataActivity", "PID: " + PID);
                        Log.d("LiveDataActivity", "Value: " + value);
                        //Switch on which pieces of information we received
                        //TODO: Handle more messages
                        switch (PID) {
                            case 4: //Engine Load
                                value = value * 100 / 255;
                                Log.i("LiveDataActivity", "Engine Load: " + value);
                                return;
                            case 5: //Coolant Temperature
                                value = (value - 40) * 9 / 5 + 32;
                                Log.i("LiveDataActivity", "Coolant Temperature: " + value);
                                return;
                            case 12: //RPM
                                value = value * 256 / 4;
                                surfaceView.setLiveRPM(value);
                                Log.i("LiveDataActivity", "RPM: " + value);
                                return;
                            case 13: //MPH
                                value = value * 5 / 8;
                                Log.i("LiveDataActivity", "MPH: " + value);
                                return;
                            case 15: //Intake Temperature
                                value = (value - 40) * 9 / 5 + 32;
                                Log.i("LiveDataActivity", "Intake Temperature: " + value);
                                return;
                            case 6:
                            case 7:
                            case 8:
                            case 9:
                            case 10:
                            case 11:
                            case 14:
                            default:
                                return;
                        }
                    } //Next possible ECU response format
                    else if (readMessage != null && readMessage.matches("\\s*[0-9A-Fa-f]{1,2} [0-9A-Fa-f]{2} [0-9A-Fa-f]{2}\\s*\r?\n?")) {
                        //Trim and split my data again
                        receivedData = readMessage.trim();
                        volt_number1 = receivedData.split(" ");
                        //Get the PID and Values
                        if (volt_number1[0] != null && volt_number1[1] != null && volt_number1[2] != null) {
                            PID = Integer.parseInt(volt_number1[0].trim(), 16);
                            value = Integer.parseInt(volt_number1[1].trim(), 16);
                            value2 = Integer.parseInt(volt_number1[2].trim(), 16);
                        }
                        Log.d("LiveDataActivity", "PID: " + PID);
                        Log.d("LiveDataActivity", "Value: " + value);
                        Log.d("LiveDataActivity", "Value2: " + value2);
                        //PID 12 is for the RPM, while 1 and 65 hold the other information
                        if (PID == 12) { //RPM
                            value = (value * 256 + value2) / 4;
                            surfaceView.setLiveRPM(value);
                            Log.i("LiveDataActivity", "RPM: " + value);
                        } //Value 1 now acts as the PID like the previous switch
                        else if (PID == 1 || PID == 65) {
                            switch (value) {
                                case 4://Engine Load
                                    value2 = value2 * 100 / 255;
                                    Log.i("LiveDataActivity", "Engine Load: " + value2);
                                    return;
                                case 5: //Coolant Temperature
                                    value2 = (value2 - 40) * 9 / 5 + 32;
                                    Log.i("LiveDataActivity", "Coolant Temperature: " + value2);
                                    return;
                                case 13: //MPH
                                    value2 = value2 * 5 / 8;
                                    Log.i("LiveDataActivity", "MPH: " + value2);
                                    return;
                                case 15: //Intake Temperature
                                    value2 = (value2 - 40) * 9 / 5 + 32;
                                    Log.i("LiveDataActivity", "Intake Temperature: " + value2);
                                    return;
                            }
                        }
                    }
                    else {
                        if (readMessage != null && readMessage.matches("\\s*[0-9]+(\\.[0-9]?)?V\\s*\r*\n*")) {
                            //TODO: Handle more responses from this format
                            receivedData = readMessage.trim();
                            Log.i("LiveDataActivity", "Voltage: " + receivedData);
                        }
                        else if(readMessage != null && readMessage.matches("\\s*[0-9]+(\\.[0-9]?)?V\\s*V\\s*>\\s*\r*\n*")) {
                            //TODO: Handle more responses from this format
                            receivedData = readMessage.trim();
                            Log.i("LiveDataActivity", "Voltage: " + receivedData);
                        }
                        else if(readMessage != null && readMessage.matches("\\s*[ .A-Za-z0-9\\?*>\r\n]*\\s*>\\s*\r*\n*")) {
                            //Here we want to get the next bit of information from the ECU,
                            if (messageNumber == 7) {
                                messageNumber = 1;
                            }
                            getData(messageNumber);
                        }
                        else {
                            //Just log out that I could not find anything
                            Log.e("LiveDataActivity", "Could not find anything with: " + readMessage.trim());
                        }
                    }
                    break;
                case 3: //Sending a message to the ECU
                    byte[] writeBuffer = (byte[])message.obj;
                    new String(writeBuffer);
                    break;
                case 4: //Sending the Bluetooth device name, and alerting the Log that a device
                    //has connected
                    btConnectedDeviceName = message.getData().getString("device_name");
                    Log.i("LiveDataActivity", "Device Name: " + btConnectedDeviceName);
                    break;
                case 5:
                    //There is an error, display it in the Logcat
                    Log.e("LiveDataActivity", "Bluetooth Handler Case5: " + message.getData().getString("toast"));
                    break;
            }
        }
    };

    /**
     * Overridden method for stopping the activity, also closes off the ChatService communication
     * between the devices
     */
    @Override
    protected void onStop() {
        super.onStop();
        if (obdChatService != null) {
            obdChatService.stop();
        }
    }

    /**
     * Overridden method for pausing the activity, normally when the application is paused. Will
     * also stop the communication between devices
     */
    @Override
    protected void onPause() {
        super.onPause();
        if (obdChatService != null) {
            obdChatService.stop();
        }
    }

    /**
     * Overridden method for resuming the paused activity, will restart the stopped communication
     * between devices
     */
    @Override
    protected synchronized void onResume() {
        super.onResume();
        if (obdChatService != null && obdChatService.getState() == 0) {
            obdChatService.start();
        }
    }

    /**
     * Overridden method for handling when the activity is destroyed. Will stop the communication
     * between devices
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (obdChatService != null) {
            obdChatService.stop();
        }
    }

    /**
     * Called when the Activity is first started/initialised. Will instantiate a new ChatService
     * object for handling communication between the application device and bluetooth device and
     * begin the communication
     */
    public void setupOBDChat() {
        obdChatService = new OBDChatService(this, btHandler);
        btOutStringBuffer = new StringBuffer("");
        connectDevice();
        startTransmission();
    }

    /**
     * Method for getting the selected paired device from the applications preferences, checking
     * bluetooth is ready and then making a connection request via the ChatService object
     */
    public void connectDevice() {
        if (this.preferenceManager != null && btDeviceAdapter != null) {
            String btDeviceAddress = preferenceManager.getBluetoothPairedDevice();
            BluetoothDevice btDevice = btDeviceAdapter.getRemoteDevice(btDeviceAddress);
            obdChatService.connect(btDevice);
        }
    }

    /**
     * Start communication between the application device and ELM327 adapter by sending the
     * correct connect message
     */
    public void startTransmission() {
        sendMessage("01 00\r");
    }

    /**
     * Method for sending a request to the ECU to erase all Diagnostic Trouble Codes stored within
     * the ECU
     */
    public void clearCodes() {
        if(this.btConnectedDeviceName != null) {
            sendMessage("04\r"); //Send the message
            Toast.makeText(getApplicationContext(), "DTCs Reset", Toast.LENGTH_LONG).show();
        }
        else {
            //Codes cannot be erased, notify user!
            Toast.makeText(getApplicationContext(), "Unable to Reset", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Method which sends a string message to the bluetooth device via the ChatService class
     * setup. For this application it will be the request for each piece of live data or code reset
     * request, or list of current DTCs
     * @param message the ECU request code
     */
    public void sendMessage(String message) {
        if (obdChatService.getState() != 3) {
            Log.d("LiveDataActivity", "SendMessage != 3");
        }
        else {
            if (message.length() > 0) {
                byte[] send = message.getBytes();
                obdChatService.write(send);
                btOutStringBuffer.setLength(0);
            }
        }
    }

    /**
     * Method for getting each piece of information required from the car ECU. Once the message
     * has been sent, the message number will increment and for the next piece of information
     * to be requested
     * @param messageNumber The current position to determine which message to send
     */
    public void getData(int messageNumber) {
        switch (messageNumber) {
            case 1:
                sendMessage("01 0C\r");
                Log.d("LiveDataActivity", "Send Message: 01 0C");
                ++messageNumber;
                break;
            case 2:
                sendMessage("01 0D\r");
                Log.d("LiveDataActivity", "Send Message: 01 0D");
                //++messageNumber;
                break;
            case 3:
                sendMessage("01 04\r");
                Log.d("LiveDataActivity", "Send Message: 01 04");
                //++messageNumber;
                break;
            case 4:
                sendMessage("01 05\r");
                Log.d("LiveDataActivity", "Send Message: 01 05");
                //++messageNumber;
                break;
            case 5:
                sendMessage("01 0F\r");
                Log.d("LiveDataActivity", "Send Message: 01 0F");
                //++messageNumber;
                break;
            case 6:
                sendMessage("AT RV\r");
                Log.d("LiveDataActivity", "Send Message: AT RV");
                //++messageNumber;
                break;
        }
    }
}
