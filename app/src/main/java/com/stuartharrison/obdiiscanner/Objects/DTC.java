package com.stuartharrison.obdiiscanner.Objects;

import java.io.Serializable;

/**
 * @author Stuart Harrison
 * @version 1.0
 */
public class DTC implements Serializable {

    // Variables
    private String dtcCode;
    private String dtcDescription;

    // Getter Properties
    public String getDtcCode() { return dtcCode; }
    public String getDtcDescription() { return dtcDescription; }

    // Setter Properties
    public void setDtcCode(String code) { this.dtcCode = code; }
    public void setDtcDescription(String description) { this.dtcDescription = description; }

    /**
     * Default constructor, assigns the variables to empty strings
     */
    public DTC() {
        this.dtcCode = "";
        this.dtcDescription = "";
    }

    /**
     * Constructor for the class, which will assign the class variables
     * @param code The trouble code which the ECU will return
     * @param description The description on what the code means, which is more readable to the user
     */
    public DTC(String code, String description) {
        this.dtcCode = code;
        this.dtcDescription = description;
    }

    /**
     * Overriden toString method used for generating a JSON Array
     * @return The string which holds the layout of the JSON array, which holds the variable
     * data from this object
     */
    @Override
    public String toString() {
        String dtcData;
        dtcData = "DTC [dtcCode=" + this.dtcCode;
        dtcData += ", dtcDescription=" + this.dtcDescription + "]";
        return dtcData;
    }
}
