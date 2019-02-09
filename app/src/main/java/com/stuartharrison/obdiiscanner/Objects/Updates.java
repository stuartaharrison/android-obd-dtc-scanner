package com.stuartharrison.obdiiscanner.Objects;

import java.io.Serializable;

/**
 * @author Stuart Harrison
 * @version 1.0
 */
public class Updates implements Serializable {

    //Variables
    private int dtcDbVersion;
    private int garageDbVersion;

    //Public getter properties
    public int getDtcDbVersion() { return dtcDbVersion; }
    public int getGarageDbVersion() { return garageDbVersion; }

    //Public setter properties
    public void setDtcDbVersion(int dbVersion) { this.dtcDbVersion = dbVersion; }
    public void setGarageDbVersion(int garVersion) { this.garageDbVersion = garVersion; }

    /**
     * Default constructor for the object
     */
    public Updates() {
        dtcDbVersion = 0;
        garageDbVersion = 0;
    }
}
