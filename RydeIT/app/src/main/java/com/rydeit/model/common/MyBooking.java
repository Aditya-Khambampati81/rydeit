package com.rydeit.model.common;

import com.rydeit.util.JavaUtil;

import java.io.Serializable;

/**
 * Created by Prakhyath on 10/17/15.
 */

public class MyBooking implements Serializable{

    public String crn;
    public String driver_name;
    public String driver_number;
    public String cab_type;
    public String cab_number;
    public String car_model;
    public int eta;

    public String cabCompany;
    public MapPoint pickupLoction;
    public MapPoint dropLocation;

    public String booking_status;
    public String pickUpAddress;

    public MapPoint driverLocation;

    public Long pickupTime;

    public String chauffeurImageUrl;

    @Override
    public String toString() {
        return "MyBooking{" +
                "crn='" + crn + '\'' +
                ", driver_name='" + driver_name + '\'' +
                ", driver_number=" + driver_number +
                ", cab_type='" + cab_type + '\'' +
                ", cab_number='" + cab_number + '\'' +
                ", car_model='" + car_model + '\'' +
                ", eta=" + eta +
                ", cabCompany='" + cabCompany + '\'' +
                ", pickupLoction=" + pickupLoction +
                ", dropLocation=" + dropLocation +
                ", booking_status='" + booking_status + '\'' +
                ", pickUpAddress='" + pickUpAddress + '\'' +
                ", pickupTime=" + pickupTime +
                '}';
    }


    public String printCustom()
    {

        final String DATE_FORMAT = "dd/MMM 'at' hh:mm a";
        String starttime = JavaUtil.getDate(pickupTime, DATE_FORMAT);
        return
                " crn='" + crn + '\'' +
                ", driver_name='" + driver_name + '\'' +
                ", driver_number=" + driver_number +
                ", cab_type='" + cab_type + '\'' +
                ", cab_number='" + cab_number + '\'' +
                ", car_model='" + car_model + '\'' +
                ", cabCompany='" + cabCompany + '\'' +
                ", pickupLoction=" + "Lat -" + pickupLoction.getLattitude() + ", Long-"+ pickupLoction.getLongitude()+
                ", dropLocation=" + dropLocation +
                ", booking_status='" + booking_status + '\'' +
                ", pickUpAddress='" + pickUpAddress + '\'' +
                ", pickupTime=" + starttime.toUpperCase();
    }
}
