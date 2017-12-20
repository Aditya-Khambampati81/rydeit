package com.rydeit.model.uber;

/**
 * Used by the Uber Request Booking request
 */
public class RideRequest extends UberModel {

    /**
     * The unique ID of the Request.
     */
    String request_id;

    /**
     * The status of the Request indicating state.
     */
    String  status;

    /**
     * The object that contains vehicle details.
     */
    Vehicle vehicle;

    /**
     * The object that contains driver details.
     */
    Driver driver;

    /**
     * The object that contains the location information of the vehicle and driver.
     */
    Location location;

    /**
     * The estimated time of vehicle arrival in minutes.
     */
    int eta;

    /**
     * The surge pricing multiplier used to calculate the increased price of a Request.
     * A multiplier of 1.0 means surge pricing is not in effect.
     */
    float surge_multiplier;

    public String getRequest_id() {
        return request_id;
    }

    public String getStatus() {
        return status;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public Driver getDriver() {
        return driver;
    }

    public Location getLocation() {
        return location;
    }

    public int getEta() {
        return eta;
    }

    public float getSurge_multiplier() {
        return surge_multiplier;
    }

    public class Driver{
        public String phone_number;
        public double rating;
        public String picture_url;
        public String name;
    }

    public class Location{
        public float latitude;
        public float longitude;
        public int bearing;
    }

    public class Vehicle{
        public String make;
        public String model;
        public String license_plate;
        public String picture_url;
    }

    @Override
    public String toString() {
        return "RideRequest{" +
                "request_id='" + request_id + '\'' +
                ", status='" + status + '\'' +
                ", vehicle=" + vehicle +
                ", driver=" + driver +
                ", location=" + location +
                ", eta=" + eta +
                ", surge_multiplier=" + surge_multiplier +
                '}';
    }
}
