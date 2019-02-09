package com.stuartharrison.obdiiscanner.Managers;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.stuartharrison.obdiiscanner.Objects.DTC;
import com.stuartharrison.obdiiscanner.Objects.Garage;
import com.stuartharrison.obdiiscanner.Objects.Manufacturer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * @author Stuart Harrison
 * @author Bobby Law
 * @version 1.1
 *
 * Class for handling the reading, and writing to the applications local database
 */
public class dbManager extends SQLiteOpenHelper {

    private static final int DB_VER = 1;
    private static final String DB_NAME = "obd.s3db";
    private final Context appContext;

    /**
     * Default constructor for the class
     * @param context The context of the activity setting up the object/class
     * @param name Name of the Database
     * @param factory SQLLiteDatabase Cursor Factory, normally set to null
     * @param version The current version of the DB
     */
    public dbManager(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
        this.appContext = context;
    }

    /**
     * Method, which executes first, will create any tables that do not currently exist
     * @param db The current Database connection object
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        String createManufacTable = "CREATE TABLE IF NOT EXISTS manufacturers (" +
                "manufacturer_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," +
                "manufacturer_name VARCHAR(64) NOT NULL," +
                "manufacturer_img VARCHAR(64) NOT NULL" +
                ")";

        String createDTCTable = "CREATE TABLE IF NOT EXISTS troublecodes (" +
                "dtc_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," +
                "dtc_code VARCHAR(10) NOT NULL," +
                "dtc_desc VARCHAR(2096) NOT NULL" +
                ")";

        String createMapTable = "CREATE TABLE IF NOT EXISTS garagelocations (" +
                "location_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," +
                "garage_name VARCHAR(64) NOT NULL," +
                "garage_speciality VARCHAR(64) NOT NULL," +
                "garage_opening VARCHAR(10) NOT NULL," +
                "garage_closing VARCHAR(10) NOT NULL," +
                "days_open INTEGER NOT NULL," +
                "contact_number VARCHAR(13) NOT NULL," +
                "latitude FLOAT NOT NULL," +
                "longitude FLOAT NOT NULL)";

        //Execute the queries that have been created above
        db.execSQL(createManufacTable);
        db.execSQL(createDTCTable);
        db.execSQL(createMapTable);
    }

    /**
     * Method which is called when the the version passed in by the application is different
     * from the current version. Will drop all tables and then setup new tables with up-to-date
     * information
     * @param db
     * @param oldVersion
     * @param newVersion
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if(newVersion > oldVersion) {
            db.execSQL("DROP TABLE IF EXISTS manufacturers");
            db.execSQL("DROP TABLE IF EXISTS troublecodes");
            db.execSQL("DROP TABLE IF EXISTS garagelocations");
            onCreate(db);
        }
    }

    /**
     * Creates a empty database on the system and rewrites it with your own database, which is
     * taken from your assets.
     * @throws IOException Thrown when an error occurs during copying of the tables
     */
    public void dbCreate() throws IOException {
        boolean dbExist = dbCheck();

        if(!dbExist){
            //By calling this method an empty database will be created into the default system path
            //of your application so we can overwrite that database with our database.
            this.getReadableDatabase();

            try {
                copyDBFromAssets();
            }
            catch (IOException e) {
                throw new Error("Error copying database");
            }
        }
    }

    /**
     * Check if the database already exist to avoid re-copying the file each time you open the application.
     * @return true if it exists, false if it doesn't
     */
    private boolean dbCheck(){
        SQLiteDatabase db = null;

        try{
            File dbFile = appContext.getDatabasePath(DB_NAME);
            String dbPath = dbFile.getPath();
            db = SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READWRITE);
            db.setLocale(Locale.getDefault());
            db.setVersion(1);
        }
        catch(SQLiteException e){
            Log.e("SQLHelper","Database not Found!");
        }

        if(db != null){
            db.close();
        }

        return db != null ? true : false;
    }

    /**
     * Copies your database from your local assets-folder to the just created empty database in the
     * system folder, from where it can be accessed and handled.
     * This is done by transfering bytestream.
     * @throws IOException true if it exists, false if it doesn't
     */
    private void copyDBFromAssets() throws IOException{
        InputStream dbInput = null;
        OutputStream dbOutput = null;
        File dbFile = appContext.getDatabasePath(DB_NAME);
        String dbFileName = dbFile.getPath();

        try {
            dbInput = appContext.getAssets().open(DB_NAME);
            dbOutput = new FileOutputStream(dbFileName);
            //transfer bytes from the dbInput to the dbOutput
            byte[] buffer = new byte[1024];
            int length;
            while ((length = dbInput.read(buffer)) > 0) {
                dbOutput.write(buffer, 0, length);
            }

            //Close the streams
            dbOutput.flush();
            dbOutput.close();
            dbInput.close();
        }
        catch (IOException e) {
            throw new Error("Problems copying DB!");
        }
    }

    /**
     * Method for getting a list of all the currently available manufacturers that the application
     * can read from. The manufacturers and logo data are stored within the DB
     * @return Returns an array-list of Manufacturer objects containing all the relevant data
     */
    public ArrayList<Manufacturer> getManufacturerList() {
        ArrayList<Manufacturer> myManufacturers = new ArrayList<>(); //Create empty list

        String query = "SELECT * FROM manufacturers"; //Do my query
        SQLiteDatabase db = this.getReadableDatabase(); //Get a readable version of the application
        //current database within the working directory

        Cursor cursor = db.rawQuery(query, null); //Execute the query

        if (cursor != null) { //Check we have rows returned!
            //Foreach row found in the query
            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                //Setup object and add to list
                Manufacturer newManufacturer = new Manufacturer(
                                                    Integer.parseInt(cursor.getString(0)),
                                                    cursor.getString(1),
                                                    cursor.getString(2));
                myManufacturers.add(newManufacturer);
            }
        }

        //Close off and free-up some memory by disposal of the results
        if (cursor != null) {
            cursor.close();
        }

        //return the list
        return myManufacturers;
    }

    /**
     * Method for getting a list of diagnostic trouble codes. As there is a lot of codes, the
     * manufacturer and searchType should be used to narrow the amount of results returned to
     * speed up searching for the user.
     * @param manufacturer The manufacturer the user selected prior to this method call.
     * @param searchType The type of search on the ECU (body, chassis, ALL, power-train, ect)
     * @return Returns a dictionary array of available DTC's based on search parameters
     */
    public HashMap<String, String> getDTCList(String manufacturer, String searchType) {
        HashMap<String, String> dtcList = new HashMap<>(); //Create my array
        Cursor cursor = null; //Set to null
        //TODO: Few things here to do, such as split the query based on the search parameters
        try {
            String query = "SELECT * FROM troublecodes"; //Setup my query
            SQLiteDatabase db = this.getReadableDatabase(); //Get a readable version of my DB
            cursor = db.rawQuery(query, null); //Execute the query

            if (cursor != null) { //Check we have results
                //Foreach result, add the code, and description to the dictionary. The code is the
                //key and should be unique within the DB
                for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                    dtcList.put(cursor.getString(1), cursor.getString(2));
                }
            }
        }
        catch (Exception ex) {
            Log.e("dbManager", ex.getMessage());
        }
        finally {
            //Close off/dispose of results if there was any
            if (cursor != null) {
                cursor.close();
            }
        }

        return dtcList; //Return my array of values
    }

    /**
     * Method for getting a collection of Garage objects used for displaying on the google maps
     * as map markers, which are stored in the DB
     * @return Collection of Garage objects
     */
    public HashMap<String, Garage> getMapsList() {
        HashMap<String, Garage> mapsList = new HashMap<>(); //Create my array
        Cursor cursor = null;
        try {
            String query = "SELECT * FROM garagelocations"; //Setup my query
            SQLiteDatabase db = this.getReadableDatabase(); //Get a readable version of my DB
            cursor = db.rawQuery(query, null); //Execute my query
            //Instantiate my variables
            String name, speciality, contact;
            int openDays;
            Time opening, closing;
            float latitude, longitude;
            if (cursor != null) { //If my query got some results
                //Then for each of them
                for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                    //Get all the data from each column
                    name = cursor.getString(1);
                    speciality = cursor.getString(2);
                    opening = Time.valueOf(cursor.getString(3));
                    closing = Time.valueOf(cursor.getString(4));
                    openDays = Integer.parseInt(cursor.getString(5));
                    contact = cursor.getString(6);
                    latitude = Float.parseFloat(cursor.getString(7));
                    longitude = Float.parseFloat(cursor.getString(8));
                    //Create new Garage object and put it in the Dictionary array
                    mapsList.put(name, new Garage(name, speciality, opening, closing, openDays,
                            contact, latitude, longitude));
                }
            }
        }
        catch (Exception ex) {
            Log.e("dbManager", ex.getMessage());
        }
        finally {
            //Dispose/close
            if (cursor != null) {
                cursor.close();
            }
        }
        //Return my array
        return mapsList;
    }

    /**
     * Method for inserting a record into the DTC table, which is used for adding new OBD Codes
     * to the applications database
     * @param code The unique identifier code for the DTC as read from the ECU
     * @param desc A description of the code
     */
    public void addDTC(String code, String desc) {
        try {
            //Setup my query
            String query = "INSERT INTO troublecodes (dtc_code, dtc_desc) VALUES ('" + code + "', '" + desc + "');";
            SQLiteDatabase db = this.getWritableDatabase(); //Get a writable version of the DB
            db.execSQL(query); //Execute the query
        }
        catch (Exception ex) {
            Log.e("dbManager", ex.getMessage());
        }
    }

    /**
     * Method for inserting a record into the GarageLocations table in the applications DB, which
     * is used for storing all the information of local garages to be displayed on the applications
     * map feature
     * @param newGarage Garage object which contains all the necessary information for each column
     *                  for a single row
     */
    public void addMap(Garage newGarage) {
        try {
            //Setup the query
            String query = "INSERT INTO garagelocations (garage_name, garage_speciality, " +
                            "garage_opening, garage_closing, days_open, contact_number, " +
                            "latitude, longitude) VALUES ('" + newGarage.getName() +
                            "', '" + newGarage.getSpeciality() + "', '" +
                            newGarage.getOpeningString() + "', '" + newGarage.getClosingString() +
                            "', " + newGarage.getDaysOpen() +  ", '" +
                            newGarage.getContactNumber() + "', " +  newGarage.getLatitude() +
                            ", " + newGarage.getLongitude() + ")";
            SQLiteDatabase db = this.getWritableDatabase(); //Get a writable version of the DB
            db.execSQL(query); //Execute the query
        }
        catch (Exception ex) {
            Log.e("dbManager", ex.getMessage());
        }
    }
}
