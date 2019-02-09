package com.stuartharrison.obdiiscanner.Activities;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.stuartharrison.obdiiscanner.Managers.dbManager;
import com.stuartharrison.obdiiscanner.Managers.httpManager;
import com.stuartharrison.obdiiscanner.Managers.prefManager;
import com.stuartharrison.obdiiscanner.Objects.Garage;
import com.stuartharrison.obdiiscanner.Objects.Updates;
import com.stuartharrison.obdiiscanner.Parser.xmlDownloader;
import com.stuartharrison.obdiiscanner.Parser.xmlParser;
import com.stuartharrison.obdiiscanner.R;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Stuart Harrison
 * @version 1.0
 */
public class UpdatesActivity extends MenuActivity {

    LinearLayout displayLayout;

    /**
     * When the activity is created, do this code
     * @param savedInstanceState The state of the activity
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.updates_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        preferenceManager = new prefManager(this);
        httpManager = new httpManager(this);
        onCreateSetup(); //Call my method for setting everything up on the screen.
    }

    /**
     * Method used to keep the onCreate method small. Sets up the views, database management
     * and checks for any available database updates
     */
    private void onCreateSetup() {
        //View Setup
        displayLayout = (LinearLayout)findViewById(R.id.updateList);
        //Check which updates are available!
        Updates newUpdates = getAvailableUpdates();
        //Get DB versions from preferences
        Updates currentDBVersions = preferenceManager.getDBVersions();

        //Compare DB versions, display a message to alert the user there is no updates
        //available if they have latest DB versions.
        if (newUpdates.getDtcDbVersion() > currentDBVersions.getDtcDbVersion()) {
            displayDTCUpdate(newUpdates.getDtcDbVersion());
        }
        if (newUpdates.getGarageDbVersion() > currentDBVersions.getGarageDbVersion()) {
            displayMapUpdate(newUpdates.getGarageDbVersion());
        }
        if (newUpdates.getDtcDbVersion() <= currentDBVersions.getDtcDbVersion() &&
                newUpdates.getGarageDbVersion() <= currentDBVersions.getGarageDbVersion()) {
            displayNoUpdatesAvailable();
        }

    }

    /**
     * Method for outputting the update button option for allowing the user to update their
     * applications database
     * @param dbVersion The new DB version available on the web-server
     */
    private void displayDTCUpdate(final int dbVersion) {
        //Setup the views that will be the same across multiple methods
        RelativeLayout newRelativeLayout = getLayoutWrapper();
        TextView displayText = getLayoutTextView(this.getString(R.string.UADTCUpdate) + " " + dbVersion);
        final Context appContext = this.getApplicationContext();
        //Because I am programmatically declaring my button, I need to setup the onclicklistener
        //with it.
        Button displayButton = getLayoutButtonView();
        displayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    AsyncDownloader downloader = new AsyncDownloader(1, dbVersion, appContext);
                    downloader.execute();
                }
                catch (Exception ex) {
                    Log.e("UpdateDTC", ex.getMessage());
                }
            }
        });

        //Append my new Views to the Relative Layout
        newRelativeLayout.addView(displayText);
        newRelativeLayout.addView(displayButton);
        displayLayout.addView(newRelativeLayout); //Attach to main View on Layout
    }

    /**
     * Method for outputting the update button option for allowing the user to update their
     * applications database
     * @param mapVersion The new DB version available on the web-server
     */
    private void displayMapUpdate(final int mapVersion) {
        //Setup the views that will be the same across multiple methods
        RelativeLayout newRelativeLayout = getLayoutWrapper();
        TextView displayText = getLayoutTextView(this.getString(R.string.UAMAPUpdate) + " " + mapVersion);
        final Context appContext = this.getApplicationContext();
        //Because I am programmatically declaring my button, I need to setup the onclicklistener
        //with it. Therefore these needs to be unique to each view.
        Button displayButton = getLayoutButtonView();
        displayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AsyncDownloader downloader = new AsyncDownloader(2, mapVersion, appContext);
                downloader.execute();
            }
        });

        newRelativeLayout.addView(displayText);
        newRelativeLayout.addView(displayButton);
        displayLayout.addView(newRelativeLayout);
    }

    /**
     * Method for outputting text to the activity screen to notify the user that there is currently
     * no updates available
     */
    private void displayNoUpdatesAvailable() {
        //Setup the views that will be the same across multiple methods
        RelativeLayout newRelativeLayout = getLayoutWrapper();
        TextView displayText = getLayoutTextView(this.getString(R.string.UANoUpdates));

        newRelativeLayout.addView(displayText);
        displayLayout.addView(newRelativeLayout);
    }

    /**
     * Called in one of the 'display' methods, gets a 'wrapper' layout which contains all the
     * contents of that method. For example, displayDTCUpdate has a button and text which will
     * be placed inside this functions returned layout.
     * @return The relative layout wrapper
     */
    private RelativeLayout getLayoutWrapper() {
        //Setup layout
        RelativeLayout newRelativeLayout = new RelativeLayout(this);

        //Setup parameters
        RelativeLayout.LayoutParams relLayoutParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        relLayoutParams.setMargins(10, 10, 10, 10);

        //Assign the layout the layout parameters and give it additional values for display
        newRelativeLayout.setLayoutParams(relLayoutParams);
        newRelativeLayout.setPadding(20, 20, 20, 20);
        newRelativeLayout.setBackground(getResources().getDrawable(R.drawable.borderblack));
        //Return new layout
        return newRelativeLayout;
    }

    /**
     * Creates a TextView object with the appropriate text to be displayed on the screen.
     * @param text The text to be assigned to the TextView
     * @return Returns the TextView view object
     */
    private TextView getLayoutTextView(String text) {
        TextView displayText = new TextView(this);
        displayText.setLayoutParams(new ActionBar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ActionBar.LayoutParams.WRAP_CONTENT));
        displayText.setGravity(Gravity.CENTER_VERTICAL);
        displayText.setTextColor(Color.BLACK);
        displayText.setText(text);

        return displayText;
    }

    /**
     * Creates a Button object to be displayed on the screen.
     * @return The button view object created
     */
    private Button getLayoutButtonView() {
        Button displayButton = new Button(this);
        RelativeLayout.LayoutParams buttonParams = new RelativeLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        //buttonParams.addRule(RelativeLayout.ALIGN_PARENT_END);
        buttonParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        displayButton.setLayoutParams(buttonParams);
        displayButton.setMinWidth(250);
        displayButton.setMaxHeight(250);
        displayButton.setText(R.string.UAUpdateButton);

        return displayButton;
    }

    /**
     * Recreates the Activity, called once the AsyncTask in this class has finished. To 'refresh'
     * the available downloads
     */
    private void refreshUpdateList() {
        this.recreate();
    }

    /**
     * Used to 'download' the new database version from the web-server
     */
    private class AsyncDownloader extends AsyncTask<Void, Integer, Void> {

        int typeOf;
        int dbVersion;
        Context appContext;
        ProgressDialog progressDialog;

        /**
         * Default constructor for the Task
         * @param typeOf Which XML file to download. 1 = DTC and 2 = Maps
         * @param newDBVersion The version of the Database which this application will update too
         */
        public AsyncDownloader(int typeOf, int newDBVersion, Context appContext) {
            this.dbVersion = newDBVersion;
            this.typeOf = typeOf;
            this.appContext = appContext;
        }

        @Override
        protected void onPreExecute() {
            //Create a new progress dialog
            progressDialog = new ProgressDialog(UpdatesActivity.this);
            //Set the progress dialog to display a horizontal progress bar
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            //Set the dialog title to 'Loading...'
            progressDialog.setTitle(appContext.getString(R.string.UAUpdateProgrss));
            //Set the dialog message to 'Searching for faults, please wait...'
            progressDialog.setMessage("");
            //This dialog can't be canceled by pressing the back key
            progressDialog.setCancelable(false);
            //This dialog isn't indeterminate
            progressDialog.setIndeterminate(false);
            //The maximum number of items is 100
            progressDialog.setMax(5);
            //Set the current progress to zero
            progressDialog.setProgress(0);
            //Display the progress dialog
            progressDialog.show();

            databaseManager = new dbManager(appContext, preferenceManager.getDatabaseName(), null, 1);
        }

        @Override
        protected Void doInBackground(Void... params) {
            switch (typeOf) {
                case 1:
                    backgroundDTC();
                    break;
                case 2:
                    backgroundMap();
                    break;
            }
            return null;
        }

        /**
         * Downloads the DTC information and inserts each into a new row in the database
         */
        private void backgroundDTC() {
            try {
                databaseManager.dbCreate();
                //Get current list of DTCs
                HashMap<String, String> allMyDTCs = databaseManager.getDTCList("ALL", "ALL");
                Log.i("AsyncDTCUpdate", "Got all DTC from the Database");
                onProgressUpdate(1); //Send progress update to the Progress Dialog

                xmlDownloader downloader = new xmlDownloader(1); //Set URL to the DTC link
                String dtcRSS = downloader.getStringFromStream(); //Download the XML stream
                Log.i("AsyncDTCUpdate", "Got RSS feed from the Internet");
                onProgressUpdate(2); //Update progress

                //Parse the Data from the XML stream
                xmlParser parser = new xmlParser();
                HashMap<String, String> newDTCs = parser.parseXMLDTCString(dtcRSS, allMyDTCs);
                Log.i("AsyncDTCUpdate", "Parsed the RSS feed for new Codes");
                onProgressUpdate(3);

                //Foreach DTC parsed from the parser, insert as new row into the database
                for (Map.Entry<String, String> entry : newDTCs.entrySet()) {
                    databaseManager.addDTC(entry.getKey(), entry.getValue());
                }
                Log.i("AsyncDTCUpdate", "Finished putting the new codes in the database");
                onProgressUpdate(4);

                //Update DB version number in the preferences
                preferenceManager.setDTCDatabaseVersion(dbVersion);
                onProgressUpdate(5);
            }
            catch (Exception ex) {
                Log.e("AsycDTCUpdate", ex.getMessage());
                httpManager.sendErrorMessageToServer("AsycDTCUpdate", ex.getMessage());
            }
        }

        /**
         * Downloads the Garage location data and inserts each into a new row in the database
         */
        private void backgroundMap() {
            try {
                databaseManager.dbCreate();
                //Get current list of available map/garage locations
                HashMap<String, Garage> allMyMaps = databaseManager.getMapsList();
                Log.i("AsyncMAPUpdate", "Got all Map Locales from the Database");
                onProgressUpdate(1);

                //Download the XML stream
                xmlDownloader downloader = new xmlDownloader(2); //2 means the MAPs URL
                String mapRSS = downloader.getStringFromStream(); //Download
                Log.i("AsyncMAPUpdate", "Got RSS feed from the Internet");
                onProgressUpdate(2); //Update the UI

                xmlParser parser = new xmlParser(); //Setup my parser
                HashMap<String, Garage> newMapLocales = parser.parseXMLMapString(mapRSS, allMyMaps);
                Log.i("AsyncMAPUpdate", "Parsed the RSS feed for the new map locales");
                onProgressUpdate(3); //Update UI thread

                //Foreach Garage object pulled from the parser, insert as a new row into the DB
                for (Map.Entry<String, Garage> entry : newMapLocales.entrySet()) {
                    databaseManager.addMap(entry.getValue());
                }
                Log.i("AsyncMAPUpdate", "Finished putting the new Map locales in the database");
                onProgressUpdate(4);

                //Update DB version number in the preferences
                preferenceManager.setMAPDatabaseVersion(dbVersion);
                onProgressUpdate(5);
            }
            catch (Exception ex) {
                Log.e("AsycMAPUpdate", ex.getMessage());
                httpManager.sendErrorMessageToServer("AsycMAPUpdate", ex.getMessage());
            }
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            progressDialog.setProgress(values[0]);
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            progressDialog.dismiss();
            refreshUpdateList();
        }
    }
}
