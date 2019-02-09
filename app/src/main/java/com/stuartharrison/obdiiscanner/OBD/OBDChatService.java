package com.stuartharrison.obdiiscanner.OBD;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Bundle;
import android.os.Message;
import android.os.Handler;
import android.util.Log;

import com.stuartharrison.obdiiscanner.Managers.httpManager;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

/**
 * @author Stuart Harrison
 * @version 2.0
 */
public class OBDChatService {

    //Variables
    public Context appContext;
    public Handler btHandler;
    public BluetoothAdapter btAdapter;

    //Declare my Threads
    private btAcceptThread acceptThread;
    private btConnectedThread connectedThread;
    private btConnectThread connectThread;

    private int btState = 0; //Current State
    private static final UUID myUUID = UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66"); //UUID for BT Connection

    /**
     * Default constructor for the class
     * @param context The applications current context
     * @param handler The Handler object from the main Chat activity for handling responses from the
     *                vehicle
     */
    public OBDChatService(Context context, Handler handler) {
        appContext = context;
        btHandler = handler;
        btAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    /**
     * Method for setting the current state of the service.
     * 0 means nothing can be sent
     * 1 means the device is ready to accept incoming connections
     * 2 means the device has made a connection to the ELM327 device
     * 3 means the device is ready to transmit messages to the car ECU
     * @param state
     */
    public synchronized void setState(int state) {
        btState = state;
        btHandler.obtainMessage(1, state, -1).sendToTarget(); //Send message to the Handler
    }

    /**
     * Method which will get the current state of the service
     * 0 means nothing can be sent
     * 1 means the device is ready to accept incoming connections
     * 2 means the device has made a connection to the ELM327 device
     * 3 means the device is ready to transmit messages to the car ECU
     * @return returns the current state of the service
     */
    public synchronized int getState() {
        return btState;
    }

    /**
     * Method for starting the three threads required to connect, run, and accept devices
     */
    public synchronized void start() {
        if (connectThread != null) {
            connectThread.cancel();
            connectThread = null;
        }

        if (connectedThread != null) {
            connectedThread.cancel();
            connectedThread = null;
        }

        setState(1);
        if (acceptThread == null) {
            acceptThread = new btAcceptThread(appContext);
            acceptThread.start();
        }
    }

    /**
     * Method for connecting a bluetooth device with the application device and starting a new
     * thread for the connection
     * @param device The bluetooth device to connect too
     */
    public synchronized void connect(BluetoothDevice device) {
        if (btState == 2 && connectThread != null) {
            connectThread.cancel();
            connectThread = null;
        }

        if (connectedThread != null) {
            connectedThread.cancel();
            connectedThread = null;
        }

        connectThread = new btConnectThread(device, appContext);
        connectThread.start();
        setState(2); //Set state to device connected/connecting
    }

    /**
     * Method called once the device has made a successfully Bluetooth connection. Setups a new
     * connected thread with the socket and device to allow messages to be sent between them
     * @param socket The bluetooth socket the connection is made on
     * @param device The bluetooth device the device is connected to
     */
    public synchronized void connected(BluetoothSocket socket, BluetoothDevice device) {
        //Cancel all threads and then setup a new connected thread with the socket and device
        cancelThreads();
        connectedThread = new btConnectedThread(socket, appContext);
        connectedThread.start();

        //Get message from the handler
        Message message = btHandler.obtainMessage(4);
        Bundle bundle = new Bundle(); //Create a bundle
        bundle.putString("device_name", device.getName()); //Put the name of the device in the bundle
        message.setData(bundle);
        btHandler.sendMessage(message); //Send a message to the handler with the bundle
        setState(3); //Set state to 3 to let the Chat activity know we can send messages back
        //and forth
    }

    /**
     * Stop all communication between the devices and application device and then change
     * the state to 0
     */
    public synchronized void stop() {
        cancelThreads();
        setState(0);
    }

    /**
     * Method for sending a stream of data to the bluetooth device. In this application,
     * will send the ECU data requests
     * @param out the byte information for the message to send to the connected device
     */
    public void write(byte[] out) {
        OBDChatService.btConnectedThread connectedThread;
        synchronized (this) {
            if(btState != 3) { //We want the state to be 3 because that is when everything is connected
                //and can accept in-coming and out-going messages
                return;
            }
            connectedThread = this.connectedThread; //Get the current connected device thread
        }
        connectedThread.write(out);
    }

    /**
     * Method for alerting the user that the application device failed to make a connection
     * to the selected bluetooth device.
     */
    private void connectionFailed() {
        Message message = btHandler.obtainMessage(5);
        Bundle bundle = new Bundle();
        bundle.putString("toast", "Unable to connect devices");
        message.setData(bundle);
        btHandler.sendMessage(message);
        start();
    }

    /**
     * Method for alerting the user that the application device has lost/dropped communication
     * with the currently connected bluetooth device
     */
    private void connectionLost() {
        Message message = this.btHandler.obtainMessage(5);
        Bundle bundle = new Bundle();
        bundle.putString("toast", "Device connection was lost");
        message.setData(bundle);
        btHandler.sendMessage(message);
        start();
    }

    /**
     * Method for checking the current each thread is active and the stopping them gracefully
     */
    public synchronized void cancelThreads() {
        if (connectThread != null) {
            connectThread.cancel();
            connectThread = null;
        }

        if (connectedThread != null) {
            connectedThread.cancel();
            connectedThread = null;
        }

        if (acceptThread != null) {
            acceptThread.cancel();
            acceptThread = null;
        }
    }

    /**
     * Threaded class which loops around waiting to accept a bluetooth device connection to the
     * application
     */
    private class btAcceptThread extends Thread {

        //Variables
        private final BluetoothServerSocket btServerSocket;
        private final httpManager httpManager;

        /**
         * Default constructor for the thread
         */
        public btAcceptThread(Context appContext) {
            BluetoothServerSocket tmpServerSocket = null;
            httpManager = new httpManager(appContext);

            try {
                tmpServerSocket = OBDChatService.this.btAdapter.listenUsingInsecureRfcommWithServiceRecord("OBDChat", OBDChatService.myUUID);
            }
            catch (IOException ex) {
                Log.e("OBDChatService", ex.getMessage());
                httpManager.sendErrorMessageToServer("OBDChatService", ex.getMessage());
            }

            btServerSocket = tmpServerSocket;
        }

        /**
         * Main loop of the class thread
         */
        public void run() {
            this.setName("btAcceptThread");
            BluetoothSocket socket = null;
            //Loop around listening for connections until a connection is made and the current
            //state becomes 3
            while (OBDChatService.this.btState != 3) {
                try {
                    socket = btServerSocket.accept();
                }
                catch (IOException ex) {
                    Log.e("OBDChatService", ex.getMessage());
                    httpManager.sendErrorMessageToServer("OBDChatService", ex.getMessage());
                    break;
                }

                if (socket != null) {
                    synchronized (OBDChatService.this) {
                        switch (OBDChatService.this.btState) {
                            case 0:
                            case 3:
                                try {
                                    socket.close();
                                }
                                catch (IOException ex) {
                                    Log.e("OBDChatService", ex.getMessage());
                                    httpManager.sendErrorMessageToServer("OBDChatService", ex.getMessage());
                                }
                            case 1:
                            case 2:
                                OBDChatService.this.connected(socket, socket.getRemoteDevice());
                        }
                    }
                }
            }
            Log.i("OBDChatService", "AcceptThread Ended");
        }

        /**
         * Closes off the socket listening for device connections
         */
        public void cancel() {
            Log.i("OBDChatService", "Socket Closing: " + this);
            try {
                btServerSocket.close();
            }
            catch (IOException ex) {
                Log.e("OBDChatService", ex.getMessage());
            }
        }
    }

    /**
     * Threaded class which is called when the application device attempts to make a connection
     * to the device
     */
    private class btConnectThread extends Thread {

        //Variables
        private final BluetoothSocket btSocket;
        private final BluetoothDevice btDevice;
        private final httpManager httpManager;

        /**
         * Default constructor
         * @param device The device that is being connected to
         */
        public btConnectThread(BluetoothDevice device, Context appContext) {
            //Setup my manager and assign my device
            httpManager = new httpManager(appContext);
            btDevice = device;
            BluetoothSocket tmpSocket = null;
            UUID SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
            //Attempt to make an insecure socket connection with the device
            try {
                tmpSocket = device.createInsecureRfcommSocketToServiceRecord(SPP_UUID);
            }
            catch (IOException ex) {
                Log.e("OBDChatService", ex.getMessage());
                httpManager.sendErrorMessageToServer("OBDChatService", ex.getMessage());
            }
            btSocket = tmpSocket; //Assign my socket which the connection is made
        }

        /**
         * Main loop for the thread
         */
        public void run() {
            this.setName("btConnectThread");
            //cancel the bluetooth adapters discovery, we have a device to connect to!
            try {
                OBDChatService.this.btAdapter.cancelDiscovery();
                btSocket.connect(); //Try connect to the device
            }
            catch (IOException oex) {
                try {
                    btSocket.close(); //Failed, close off the socket
                }
                catch (IOException iex) {
                    Log.e("OBDChatService", iex.getMessage());
                    httpManager.sendErrorMessageToServer("OBDChatService", iex.getMessage());
                }
                httpManager.sendErrorMessageToServer("OBDChatService", oex.getMessage());
                OBDChatService.this.connectionFailed(); //Alert user, connection failed
                return;
            }

            synchronized (OBDChatService.this) {
                OBDChatService.this.connectThread = null;
            }
            //Everything went well, finalise the connection and start the main thread which handles
            //message sending and receiving
            OBDChatService.this.connected(btSocket, btDevice);
        }

        /**
         * Fired when the thread is ending, closes the current bluetooth socket
         */
        public void cancel() {
            try {
                btSocket.close();
            }
            catch (IOException ex) {
                Log.e("OBDChatService", ex.getMessage());
                httpManager.sendErrorMessageToServer("OBDChatService", ex.getMessage());
            }
        }
    }

    /**
     * Threaded class which handles the sending and receiving of messages from the application
     * device and the connected bluetooth device
     */
    private class btConnectedThread extends Thread {

        //Variables
        private final BluetoothSocket btSocket;
        private final InputStream btInputStream;
        private final OutputStream btOutputStream;
        private final httpManager httpManager;

        /**
         * Default constructor of the class
         * @param socket the socket on which the application device and bluetooth device are
         *               connected on
         */
        public btConnectedThread(BluetoothSocket socket, Context appContext) {
            //Setup variables
            btSocket = socket;
            InputStream tmpInputStream = null;
            OutputStream tmpOutputStream = null;
            httpManager = new httpManager(appContext);

            //Attempt to get input and output streams of the socket
            try {
                tmpInputStream = socket.getInputStream();
                tmpOutputStream = socket.getOutputStream();
            }
            catch (Exception ex) {
                Log.e("OBDChatService", ex.getMessage());
                httpManager.sendErrorMessageToServer("OBDChatService", ex.getMessage());
            }
            //Assign the attempted to get streams to the variable streams
            this.btInputStream = tmpInputStream;
            this.btOutputStream = tmpOutputStream;
        }

        /**
         * Main loop for the thread class will continually check for data coming from the
         * connected bluetooth device
         */
        public void run() {
            byte[] buffer = new byte[1024];
            while (true) {
                try {
                    //Try getting data back from ELM327 device connected to the vehicle
                    int bytes = this.btInputStream.read(buffer);
                    //Send the message to the chat activity handler
                    OBDChatService.this.btHandler.obtainMessage(2, bytes, -1, buffer).sendToTarget();
                }
                catch (IOException ex) {
                    Log.e("OBDChatService", ex.getMessage());
                    httpManager.sendErrorMessageToServer("OBDChatService", ex.getMessage());
                    OBDChatService.this.connectionLost(); //Tell the service we have lost connection
                    OBDChatService.this.start(); //Restart chat service
                    return;
                }
            }
        }

        /**
         *
         * @param buffer The buffer to be send to the ELM327 connected bluetooth device, which
         *               is plugged into the vehicle
         */
        public void write(byte[] buffer) {
            try {
                this.btOutputStream.write(buffer);
                OBDChatService.this.btHandler.obtainMessage(3, -1, -1, buffer).sendToTarget();
            }
            catch (IOException ex) {
                Log.e("OBDChatService", ex.getMessage());
                httpManager.sendErrorMessageToServer("OBDChatService", ex.getMessage());
            }
        }

        /**
         * Closes down the currently connected socket between application device and connected
         * bluetooth device
         */
        public void cancel() {
            try {
                btSocket.close();
            }
            catch (IOException ex) {
                Log.e("OBDChatService", ex.getMessage());
                httpManager.sendErrorMessageToServer("OBDChatService", ex.getMessage());
            }
        }
    }
}
