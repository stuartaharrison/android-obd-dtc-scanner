package com.stuartharrison.obdiiscanner.Activities;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ActionMenuView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.stuartharrison.obdiiscanner.Managers.dbManager;
import com.stuartharrison.obdiiscanner.Managers.httpManager;
import com.stuartharrison.obdiiscanner.Managers.prefManager;
import com.stuartharrison.obdiiscanner.Objects.Manufacturer;
import com.stuartharrison.obdiiscanner.Parser.xmlDownloader;
import com.stuartharrison.obdiiscanner.R;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Stuart Harrison
 * @version 1.0
 */
public class DiagnosticsMainActivity extends MenuActivity {

    //Layout variables
    LinearLayout display;
    ViewSwitcher switcher;

    /**
     * When the activity is created, do this code
     * @param savedInstanceState The state of the activity
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.diagnostics_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        onCreateSetup(); //Call my method for setting everything up on the screen.
    }

    /**
     * Method for setting up all the layouts and application managers, called separately from
     * the onCreate to keep everything tidy.
     */
    private void onCreateSetup() {
        //Get LinearLayout for Display
        display = (LinearLayout)findViewById(R.id.manufacturerList);
        //Get ViewSwitcher
        switcher = (ViewSwitcher)findViewById(R.id.switcher);

        //Setup managers
        httpManager = new httpManager(this);
        preferenceManager = new prefManager(this);

        try {
            //Setup DB
            String dbName = preferenceManager.getDatabaseName();
            databaseManager = new dbManager(this, dbName, null, 1);
            databaseManager.dbCreate();
        }
        catch (Exception ex) {
            Log.e("DiagnosticsMainActivity", ex.getMessage());
            httpManager.sendErrorMessageToServer("DiagnosticsMainActivity", ex.getMessage());
        }

        AsyncDiagnosticsSetup setup = new AsyncDiagnosticsSetup();
        setup.execute();
    }

    /**
     * Moves the application to the new Activity
     * @param intentToMove The activity to be moved too
     */
    private void nextActivity(Intent intentToMove) {
        this.startActivity(intentToMove);
    }

    /**
     * Call to method will display all available manufacturers in rows of 4 columns
     * @param manufacturers The list of Manufacturers to be displayed on the screen
     */
    public void displayManufacturerButtons(final List<Manufacturer> manufacturers) {
        //Setup layout
        // 0, 4, 8, ect will be on new rows..
        // 0 1 2 3
        // 4 5 6 7
        // 8 9 10 11
        // This pattern is followed for all manufacturers found in the DB
        Context appContext = getApplicationContext();
        LinearLayout.LayoutParams outerParams = new LinearLayout.LayoutParams(
                                                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
        LinearLayout.LayoutParams innerParams = new LinearLayout.LayoutParams(
                                                    LinearLayout.LayoutParams.WRAP_CONTENT, 0, 1.0f);
        //Setup some bits
        LinearLayout rowLayout = null;
        int maxRow = 4;
        int currentRow = 1;
        for (int counter = 0; counter < manufacturers.size(); counter++) {
            String imagePath = null;
            int imgResID;

            try {
                //Get the Manufacturer data, which is next in the array
                Manufacturer nextManufacturer = manufacturers.get(counter);
                Log.i("Got Manufacturer:", nextManufacturer.getManufacName());
                //Check the next row value;
                int toNextRow = maxRow * currentRow;
                //Check if we are going to the next row, append the current working layout
                if (counter >= toNextRow) {
                    currentRow++;
                    display.addView(rowLayout);
                }
                //Check if I am going onto the next row, then create new linearlayout
                if (counter == 0 || counter == toNextRow) {
                    rowLayout = new LinearLayout(this);
                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
                    rowLayout.setLayoutParams(layoutParams);
                    rowLayout.setWeightSum(4);
                    rowLayout.setOrientation(LinearLayout.HORIZONTAL);
                }

                //Create the LinearLayout to hold ImageButton and Text
                LinearLayout imageLayout = new LinearLayout(this);
                imageLayout.setLayoutParams(outerParams);
                imageLayout.setWeightSum(1);
                imageLayout.setOrientation(LinearLayout.VERTICAL);


                //Assign the Image Button
                //Get button image
                imagePath = "drawable/" + nextManufacturer.getManufacImg();
                imgResID = appContext.getResources().getIdentifier(imagePath, "drawable", "com.stuartharrison.obdiiscanner");


                //Setup the Image Button
                ImageButton imageButton = new ImageButton(this);
                imageButton.setLayoutParams(innerParams);
                imageButton.setImageResource(imgResID);
                //imageButton.setBackgroundColor(Color.TRANSPARENT);
                imageButton.setScaleType(ImageView.ScaleType.FIT_CENTER);
                //Set Tag
                imageButton.setTag(nextManufacturer);
                //Setup the onClickListener for each ImageButton
                imageButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent typeScreen = new Intent(getApplicationContext(), DiagnosticsTypeActivity.class);
                        typeScreen.putExtra("manufacturer", (Manufacturer) v.getTag());
                        nextActivity(typeScreen);
                    }
                });

                //Setup my TextView
                TextView txtView = new TextView(this);
                txtView.setLayoutParams(innerParams);
                txtView.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
                txtView.setText(nextManufacturer.getManufacName());
                txtView.setTextColor(Color.WHITE);

                //Add my views
                imageLayout.addView(imageButton);
                imageLayout.addView(txtView);
                rowLayout.addView(imageLayout);
            }
            catch (Exception ex) {
                Log.e("DiagnosticsMainActivity", ex.getMessage());
                httpManager.sendErrorMessageToServer("DiagnosticsMainActivity", ex.getMessage());
            }
        }
        // As the loop will break earlier than required, finally append last row
        display.addView(rowLayout);
    }

    /**
     * Async Task for getting a complete list of all Manufacturers before returning the result
     * back to the Main Thread to be displayed
     */
    public class AsyncDiagnosticsSetup extends AsyncTask<Void, ArrayList<Manufacturer>, Void> {

        public AsyncDiagnosticsSetup() { }

        @SuppressWarnings("unchecked")
        @Override
        protected Void doInBackground(Void... voids) {
            ArrayList<Manufacturer> manufacturers;
            try {
                //Create list of car manufacturers and then display them
                manufacturers = databaseManager.getManufacturerList();
                //displayManufacturerButtons(manufacturers);
                publishProgress(manufacturers);
            }
            catch (Exception ex) {
                Log.i("DiagnosticsMainActivity", "Async Background Error");
                httpManager.sendErrorMessageToServer("DiagnosticsMainActivity", "Async Background Error");
                publishProgress(new ArrayList<Manufacturer>());
            }
            return null;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void onProgressUpdate(ArrayList<Manufacturer>... values) {
            displayManufacturerButtons(values[0]);
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            switcher.showNext();
        }
    }
}
