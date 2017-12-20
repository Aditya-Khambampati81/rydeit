package com.rydeit.model.uber;

/**
 * Used by the user activity endpoint and contains information including the pickup location,
 * dropoff location, request start time, request end time, and distance of requests (in miles), as
 * well as the product type that was requested.
 */
public class History extends UberModel {
    /**
     * Status of the trip. Only returns completed for now.
     */
    String status;

    public String getStatus() {
        return status;
    }

    /**
     * Length of trip in miles.
     */
    float distance;



    public float getDistance() {
        return distance;
    }

     /**
     * Unix timestamp of trip request time.
     */
    long request_time;

    public long getRequestTime() {
        return request_time;
    }

    /**
     * Unix timestamp of trip start time.
     */
    long start_time;

    public long getStart_time() {
        return start_time;
    }

    /**
     * information of the trip city based on V2 response.
     */
    UberCity start_city;

    public UberCity getStart_City()
    {
       return start_city;
    }

    long  end_time;
    /**
     * get trip end time
     */
    public long getEnd_time()
    {
        return end_time;
    }

    String request_id;

    /**
     * get Req id
     */
    public String getRequest_id()
    {
        return request_id;
    }



    String product_id;

    /**
     * Return product id of cab
     */
    public String getProduct_id()
    {
        return product_id;
    }


}
