package com.rydeit.model.uber;

/**
 * Created by Aditya.Khambampati on 11/13/2015.
 */
public class UberCity extends UberModel {

    /**
     * Latitude component of location.
     */
    double latitude;

    public double getLatitude() {
        return latitude;
    }

    String display_name ;

    /**
     * Start point of the location
     * @return
     */
    public String getCityName()
    {
        return  display_name;
    }
    /**
     * Longitude
     */
    /**
     * Latitude component of location.
     */
    double longitude;

    public double getLongitude() {
        return longitude;
    }


}
