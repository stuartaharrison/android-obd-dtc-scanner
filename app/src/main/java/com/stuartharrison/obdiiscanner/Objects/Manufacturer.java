package com.stuartharrison.obdiiscanner.Objects;

import java.io.Serializable;

/**
 * @author Stuart Harrison
 * @version 1.0
 */
public class Manufacturer implements Serializable {

    //Variables
    private int manufacID;
    private String manufacName;
    private String manufacImg;

    //Public getter properties
    public int getManufacID() { return manufacID; }
    public String getManufacName() { return manufacName; }
    public String getManufacImg() { return manufacImg; }

    /**
     * Default constructor
     * @param id The unique ID for the Manufacturer from the DB
     * @param name The Manufacturers name
     * @param img The name of the image file for the Manufacturers logo in the applications
     *            resources
     */
    public Manufacturer(int id, String name, String img) {
        this.manufacID = id;
        this.manufacName = name;
        this.manufacImg = img;
    }
}
