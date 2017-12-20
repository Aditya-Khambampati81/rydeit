package com.rydeit.model.common;

import java.io.Serializable;

/**
 * Created by Prakhyath on 11/22/15.
 */
public class MapPoint implements Serializable{
    private double lattitude;
    private double longitude;

    public MapPoint(double lattitude, double longitude){
        this.lattitude=lattitude;
        this.longitude=longitude;
    }

    public double getLattitude() {
        return lattitude;
    }

    public void setLattitude(double lattitude) {
        this.lattitude = lattitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}
