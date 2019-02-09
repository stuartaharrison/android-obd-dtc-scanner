package com.stuartharrison.obdiiscanner.Parser;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * @author Stuart Harrison
 * @version 1.0
 *
 * Class for getting the RSS feed as a string from the web-server
 */
public class xmlDownloader {

    /** The URL of the XML file to download */
    private static String myURL = "";

    /**
     * Base constructor for the class, which decides which URL to use based on the parameter
     * passed
     * @param typeOf The URL to use, 0; 1; or 2
     */
    public xmlDownloader(int typeOf) {
        switch (typeOf) {
            case 0:
                // Update check is for checking the variations in versions between what is
                //hosted on the web-server and the current version on the application
                myURL = "http://www.stuart-harrison.com/obdScanner/app/updatecheck.xml";
                break;
            case 1:
                //Location of the most recent list of diagnostic trouble codes
                myURL = "http://www.stuart-harrison.com/obdScanner/app/dtcs.xml";
                break;
            case 2:
                //Location of the most recent list of mechanics locations for the map to display
                myURL = "http://www.stuart-harrison.com/obdScanner/app/mapdata.xml";
                break;
            default:
                break; //Will leave the URL empty. However checks are in place for handling
            //any potential errors
        }
    }

    /**
     * Method for actually getting the XML as a string from the web-server
     * @return Returns a string, which contains the data in XML format
     * @throws Exception Exception is thrown when no URL has been defined or an error occurs during
     * the actual downloading of the stream
     */
    public String getStringFromStream() throws Exception {
        if (myURL.equals("")) {
            //Check whether the URL has been defined. If not, throw an exception
            throw new Exception("No URL has been defined");
        }
        else {
            //Setup my variables
            InputStream in = null;
            String rssFeed = null;
            try {
                URL url = new URL(myURL); //Create the URL
                //Setup my connection using the URL assigned and open the connection
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                in = conn.getInputStream();
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                //Write out the contents of the file on the web server to a byte array
                for (int count; (count = in.read(buffer)) != -1; ) {
                    out.write(buffer, 0, count);
                }
                byte[] response = out.toByteArray();
                //Assign the byte array as a new formatted string ready to be returned
                rssFeed = new String(response, "UTF-8");
            } finally {
                if (in != null) {
                    in.close();
                }
            }
            //Finally return the string
            return rssFeed;
        }
    }
}
