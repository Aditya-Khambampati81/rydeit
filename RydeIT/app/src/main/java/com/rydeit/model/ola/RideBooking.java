package com.rydeit.model.ola;

import java.io.Serializable;

/**
 * Created by Prakhyath on 10/17/15.
 */

public class RideBooking implements Serializable{

    public String crn;
    public String driver_name;
    public long driver_number;
    public String cab_type;
    public String cab_number;
    public String car_model;
    public int eta;
    public double driver_lat;
    public double driver_lng;

    @Override
    public String toString() {
        return "RideBooking{" +
                "crn='" + crn + '\'' +
                ", driver_name='" + driver_name + '\'' +
                ", driver_number=" + driver_number +
                ", cab_type='" + cab_type + '\'' +
                ", cab_number='" + cab_number + '\'' +
                ", car_model='" + car_model + '\'' +
                ", eta=" + eta +
                ", driver_lat=" + driver_lat +
                ", driver_lng=" + driver_lng +
                '}';
    }
}
