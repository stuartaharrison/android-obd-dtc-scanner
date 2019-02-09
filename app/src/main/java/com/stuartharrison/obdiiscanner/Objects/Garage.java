package com.stuartharrison.obdiiscanner.Objects;

import java.io.Serializable;
import java.sql.Time;

/**
 * @author Stuart Harrison
 * @version 1.0
 */
public class Garage implements Serializable {

    //Variables
    private String name;
    private String speciality;
    private String contactNumber;
    private Time opening;
    private Time closing;
    private int daysOpen;
    private double latitude;
    private double longitude;

    //Public getter properties
    public String getName() { return name; }
    public String getSpeciality() { return speciality; }
    public String getContactNumber() { return  contactNumber; }
    public Time getOpening() { return  opening; }
    public Time getClosing() { return  closing; }
    public int getDaysOpen() { return  daysOpen; }
    public double getLatitude() { return  latitude; }
    public double getLongitude() { return  longitude; }

    //Public setter properties
    public void setName(String value) { this.name = value; }
    public void setSpeciality(String value) { this.speciality = value; }
    public void setContactNumber(String value) { this.contactNumber = value; }
    public void setOpening(Time value) { this.opening = value; }
    public void setClosing(Time value) { this.closing = value; }
    public void setDaysOpen(int value) { this.daysOpen = value; }
    public void setLatitude(double value) { this.latitude = value; }
    public void setLongitude(double value) { this.longitude = value; }

    /**
     * Default constructor
     */
    public Garage() { }

    /**
     * Constructor for the object which gives all variables of the object a value
     * @param name The name of the Garage
     * @param speciality What is the garages speciality
     * @param opening When does the Garage open
     * @param closing When does the Garage close
     * @param daysOpen Number of days a week the Garage is open
     * @param contactNumber The contact telephone number of the Garage
     * @param latitude The latitude location of the Garage
     * @param longitude The longitude location of the Garage
     */
    public Garage(String name, String speciality, Time opening, Time closing, int daysOpen,
                  String contactNumber, double latitude, double longitude) {
        this.name = name;
        this.speciality = speciality;
        this.opening = opening;
        this.closing = closing;
        this.daysOpen = daysOpen;
        this.contactNumber = contactNumber;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    /**
     * Getter method for converting a Time object into String format for display
     * @return The String value of the opening Time object
     */
    public String getOpeningString() {
        return opening.toString();
    }

    /**
     * Getter method for converting a Time object into String format for display
     * @return The String value of the closing Time object
     */
    public String getClosingString() {
        return closing.toString();
    }
}
