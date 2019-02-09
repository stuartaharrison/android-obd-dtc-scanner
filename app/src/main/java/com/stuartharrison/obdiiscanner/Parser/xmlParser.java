package com.stuartharrison.obdiiscanner.Parser;

import android.util.Log;

import com.stuartharrison.obdiiscanner.Objects.Garage;
import com.stuartharrison.obdiiscanner.Objects.Updates;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.StringReader;
import java.sql.Time;
import java.util.HashMap;

/**
 * @author Stuart Harrison
 * @version 1.0
 *
 * Class for passing an XML file. This class should be used with the xmlDownloader class to parse
 * the data collected from there
 */
public class xmlParser {

    /**
     * Default constructor
     */
    public xmlParser() { }

    /**
     * Parsing the string which contains the information about which version of both the maps
     * and data trouble codes are available on the web-server. Normally used to compare versions,
     * to notify the app user of updates
     * @param rssFeed The string which contains the XML to be parsed
     * @return Returns an object which holds the information on each DB version
     */
    public Updates parseXMLUpdateString(String rssFeed) {
        Updates myUpdates = new Updates(); //Declare new instance of return object

        try {
            //Setup my pullparser
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser pullParser = factory.newPullParser();
            pullParser.setInput(new StringReader(rssFeed)); //Pass in the string Parameter
            int eventType = pullParser.getEventType();

            //Run through the String
            while (eventType != XmlPullParser.END_DOCUMENT) {

                if (eventType == XmlPullParser.START_TAG) {
                    if (pullParser.getName().equalsIgnoreCase("dtc")) { //DTC version is held in this tag
                        String dtcText = pullParser.nextText();
                        Log.i("xmlParser", "Got: " + dtcText);
                        myUpdates.setDtcDbVersion(Integer.parseInt(dtcText)); //Set it to the object
                    }
                    if (pullParser.getName().equalsIgnoreCase("garages")) { //Garage DB version is held in this tag
                        String garText = pullParser.nextText();
                        Log.i("xmlParser", "Got: " + garText);
                        myUpdates.setGarageDbVersion(Integer.parseInt(garText)); //Set it to the new object
                    }
                }
                eventType = pullParser.next(); //Move to next line in the string/document
            }
        }
        catch (Exception ex) {
            Log.e("xmlParseErr", ex.getMessage()); //Log out any caught exceptions
        }
        //Return the new object
        return myUpdates;
    }

    /**
     * Parsing the string that contains the XML data on all the Diagnostic Trouble Codes available
     * to the applications database
     * @param rssFeed The string which holds the XML
     * @param currentCodes A dictionary containing the current codes in the database. This is used
     *                     to check that no duplicate codes are added to the database
     * @return A list of DTC's that are not currently in the applications database
     */
    public HashMap<String, String> parseXMLDTCString(String rssFeed,
                                                      HashMap<String, String> currentCodes) {
        HashMap<String, String> myDTCs = new HashMap<>();

        try {
            //Setup the pull parser
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser pullParser = factory.newPullParser();
            pullParser.setInput(new StringReader(rssFeed)); //Pass in the parameter to the reader
            int eventType = pullParser.getEventType();
            //Setup the variables I need to perform the search
            String gotCode = null;
            String gotDesc = null;
            Boolean flag = false;
            //for every line in the document/xml string
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {
                    //Looking for specific start tags
                    if (pullParser.getName().equalsIgnoreCase("id")) {
                        //Assign variable with the text from between the found id tags
                        gotCode = pullParser.nextText();
                        //Check that the current DB doesn't have that code
                        if (!currentCodes.containsKey(gotCode)) {
                            flag = true; //Setting this flag to notify the code we will be storing
                            //this code
                        }
                    }

                    //Looking for the description tag, but only if we are storing this DTC code
                    if (pullParser.getName().equalsIgnoreCase("description") && flag) {
                        flag = false; //Reset my flag
                        gotDesc = pullParser.nextText(); //Assign variable to text between tags
                    }
                }

                //Looking for the end tag
                if (eventType == XmlPullParser.END_TAG) {
                    //Will only add the DTC to the new dictionary object if both the gotCode
                    //and gotDescription has values
                    //Also check we are at the end of a code tag within the document, and double
                    //check that the code does not currently exist as this would create an
                    //exception
                    if (pullParser.getName().equalsIgnoreCase("code") && gotCode != null &&
                            gotDesc != null && !myDTCs.containsKey(gotCode)) {
                        myDTCs.put(gotCode, gotDesc);
                        gotCode = null; //Reset gotCode
                        gotDesc = null; //Reset gotDesc
                        //Resetting these values so that the end tag doesn't 'fire' because there
                        //is previously kept data still there!
                    }
                }
                //Finally, move to the next line
                eventType = pullParser.next();
            }
        }
        catch (Exception ex) {
            //Log out any caught error
            Log.e("xmlParseErr", ex.getMessage());
        }
        //Return my new dictionary list of new trouble codes
        return  myDTCs;
    }

    /**
     * Parsing the string which contains the data on all the currently available potential map
     * markers from the web-server. Will create a list of markers that are not currently held in the
     * current version of the applications database
     * @param rssFeed The XML String which holds the map markers data
     * @param currentMapLocs The current list of marker data held by the database
     * @return A new list of map marker information that is not currently held by the application
     * database
     */
    public HashMap<String, Garage> parseXMLMapString(String rssFeed,
                                                     HashMap<String, Garage> currentMapLocs) {
        HashMap<String, Garage> newLocales = new HashMap<>(); //Create new dictionary object

        try {
            //Setup my pull parser
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser pullParser = factory.newPullParser();
            pullParser.setInput(new StringReader(rssFeed)); //Pass in the XML String
            int eventType = pullParser.getEventType();
            //Setup my variables
            String garage_name = null;
            Boolean flag = false;
            Garage newGarage = null;
            //Foreach line in the XML document/String
            while (eventType != XmlPullParser.END_DOCUMENT) {
                //Check for start tag
                if (eventType == XmlPullParser.START_TAG) {
                    //Finding the name is important
                    //Once we have found that, we can create a new Garage object ready for
                    //collecting the other data in the string
                    if (pullParser.getName().equalsIgnoreCase("name")) {
                        garage_name = pullParser.nextText(); //Get the text
                        if (!currentMapLocs.containsKey(garage_name)) {
                            //Only if the Map data does not exists currently in the DB
                            newGarage = new Garage(); //Create new object
                            newGarage.setName(garage_name); //Set the name to the text
                        }
                    }
                    //Still checking for start tag but now we check if the newGarage object
                    //is not null before checking for the other tags; trying to speed the parsing
                    //up a bit
                    if (newGarage != null) {
                        if (pullParser.getName().equalsIgnoreCase("speciality")) {
                            newGarage.setSpeciality(pullParser.nextText()); //Set the garages speciality
                        }
                        if (pullParser.getName().equalsIgnoreCase("opening")) {
                            String time = pullParser.nextText();
                            newGarage.setOpening(Time.valueOf(time)); //Parse the found string into a Time object
                        }
                        if (pullParser.getName().equalsIgnoreCase("closing")) {
                            String time = pullParser.nextText();
                            newGarage.setClosing(Time.valueOf(time)); //Parse the found string into a Time object
                        }
                        if (pullParser.getName().equalsIgnoreCase("daysopen")) {
                            newGarage.setDaysOpen(Integer.parseInt(pullParser.nextText())); //Parse to an Int
                        }
                        if (pullParser.getName().equalsIgnoreCase("contact")) {
                            newGarage.setContactNumber(pullParser.nextText()); //Keeping the contact number a string
                            //as most numbers start with a 0!
                        }
                        if (pullParser.getName().equalsIgnoreCase("latitude")) {
                            newGarage.setLatitude(Float.parseFloat(pullParser.nextText())); //Float
                        }
                        if (pullParser.getName().equalsIgnoreCase("longitude")) {
                            newGarage.setLongitude(Float.parseFloat(pullParser.nextText())); //Float
                        }
                    }
                } //End start tag checking
                //Check for the end tag and that the newGarage object contains something
                if (eventType == XmlPullParser.END_TAG && newGarage != null) {
                    if (pullParser.getName().equalsIgnoreCase("map")) {
                        //Put the newly found map data object into the dictionary
                        newLocales.put(newGarage.getName(), newGarage);
                        newGarage = null; //Reset object for next iteration
                    }
                }
                //Move to the next line
                eventType = pullParser.next();
            }
        }
        catch (Exception ex) {
            //Log out the error
            Log.e("xmlParseErr", ex.getMessage());
        }
        //Return my dictionary object
        return newLocales;
    }
}
