package com.stuartharrison.obdiiscanner.Managers;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Stuart Harrison
 * @version 1.0
 *
 * Class for sending handled errors to the web-servers database so as I developer I can debug any
 * errors that may occur during testing
 */
public class httpManager {

    //The hash is to stop any anyone other than this app submitting to to the PHP file
    private static final String HTTP_HASH = "MOBILE1234";
    //The location of the PHP file which will handle inserting the data into the database
    private static final String HTTP_URL_ERROR = "http://www.stuart-harrison.com/obdScanner/app/logerror.php";
    private final Context appContext;
    private final prefManager prefManager;

    /**
     * Default constructor, doesn't really do anything
     * @param appContext the current application context from the activity declaring this object
     */
    public httpManager(Context appContext) {
        this.appContext = appContext;
        prefManager = new prefManager(appContext);
    }

    /**
     * Method for taking the TAG and Message, as you would in a normal Logcat output, and then
     * push it as a HTTP Post Request to the web-server, which will then put that data into a
     * database in the cloud.
     * @param tag The Tag associated with the error message. Normally will be the name of the activity
     *            the error came from
     * @param message The message associated with the error code/tag
     */
    public void sendErrorMessageToServer(String tag, String message) {
        //Check that I can actually send the error messages, that we have an internet connection
        //and the user has said yes to sending error messages to the web-server in the preferences
        if(!prefManager.getSendErrorMessages() && !isInternetAvailable()) {
            return;
        }
        //Setup HTTP objects
        HttpClient httpClient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost(HTTP_URL_ERROR);
        //Setup values to send
        List<NameValuePair> dataToSend = new ArrayList<>();
        dataToSend.add(new BasicNameValuePair("hash", HTTP_HASH));
        dataToSend.add(new BasicNameValuePair("tag", tag));
        dataToSend.add(new BasicNameValuePair("message", message));

        //Encode POST data and send
        try {
            httpPost.setEntity(new UrlEncodedFormEntity(dataToSend));
            HttpResponse response = httpClient.execute(httpPost);
            Log.i("HTTP", response.toString());
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Method used for checking whether the device is currently connected to the internet, in order
     * to get the updates from the web-server. Checks all variations of connectivity!
     * @return True if there is a valid connection to the internet, otherwise false
     */
    public Boolean isInternetAvailable() {
        //Get connectivity manager
        ConnectivityManager connectivityManager = (ConnectivityManager)appContext.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
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
}
