package com.rydeit.cab.service.ola;

import com.rydeit.model.ola.RideBooking;
import com.rydeit.model.ola.TrackRide;

/**
 * Created by Prakhyath on 10/18/15.
 */
public class OlaSandBox {

    public static RideBooking getSimulateRideBookingObject()
    {
        RideBooking rideBooking=new RideBooking();

        //{"crn":"137110242","driver_name":"Venkatesh Venkatesh","driver_number":"9743841656","cab_type":"mini",
        // "cab_number":"KA 01 AE 7347","car_model":"Tata Indica","eta":4,"driver_lat":12.9494516,"driver_lng":77.6454383}
        rideBooking=new RideBooking();
        rideBooking.crn="137110242";
        rideBooking.driver_name="Venkatesh Kumar";
        rideBooking.driver_number=9880712668L;
        rideBooking.cab_type="mini";
        rideBooking.cab_number="KA 01 BH 7347";
        rideBooking.car_model="Toyota Etios";
        rideBooking.eta=4;
        rideBooking.driver_lat=12.9494516;
        rideBooking.driver_lng=77.6454383;

        return rideBooking;

    }

    public static TrackRide getSimulatedClientLocated()
    {
    /**
        {
            "status": "SUCCESS",
                "request_type": "TRACK_RIDE",
                "crn": "82053435",
                "driver_lat": 12.949909,
                "driver_lng": 77.6428323,
                "booking_status": "CLIENT_LOCATED"
        }
      **/

        TrackRide trackRide=new TrackRide();
        trackRide.status="SUCCESS";
        trackRide.crn="82053435";
        trackRide.request_type="TRACK_RIDE";
        trackRide.booking_status="CLIENT_LOCATED";
        trackRide.driver_lat=12.95696810;
        trackRide.driver_lng=77.70097732;


        return trackRide;

    }


    public static TrackRide getSimulatedCallDriver()
    {
        /**
          {
              "status":"SUCCESS",
              "request_type":"TRACK_RIDE",
              "booking_status":"CALL_DRIVER",
              "crn":"169077209",
              "duration":
                        {
                         "value":3,
                         "unit":"MINUTE"
                        },
              "distance":
                        {
                          "value":600.6,
                          "unit":"METER"
                        },
              "driver_lat":12.9531149,
              "driver_lng":77.6526333
         }
         */
        TrackRide trackRide=new TrackRide();
        trackRide.status="SUCCESS";
        trackRide.crn="169077209";
        trackRide.request_type="TRACK_RIDE";
        trackRide.booking_status="CALL_DRIVER";
        trackRide.duration.value=3;
        trackRide.duration.unit="MINUTE";
        trackRide.distance.value=600.6;
        trackRide.distance.unit="METER";
        trackRide.driver_lat=12.95696810;
        trackRide.driver_lng=77.70097732;

        return trackRide;

    }

    public static TrackRide getSimulatedRideTrackObject()
    {
        //trip started
        /**
         * {
         "status": "SUCCESS",
         "request_type": "TRACK_RIDE",
         "crn": "82053435",
         "driver_lat": 12.9499099,

         "driver_lng": 77.6428464,
         "booking_status": "IN_PROGRESS"
         }

         */

        TrackRide trackRide=new TrackRide();
        trackRide.status="SUCCESS";
        trackRide.crn="82053435";
        trackRide.request_type="TRACK_RIDE";
        trackRide.booking_status="IN_PROGRESS";
        trackRide.driver_lat=12.95696810;
        trackRide.driver_lng=77.70097732;


        return trackRide;
    }

    public static TrackRide getSimulatedTripEnded()
    {

        TrackRide trackRide=new TrackRide();
        trackRide.crn="82053435";
        trackRide.request_type="TRACK_RIDE";
        trackRide.booking_status="COMPLETED";
        trackRide.driver_lat=12.95696810;
        trackRide.driver_lng=77.70097732;
        trackRide.duration.value=2;
        trackRide.duration.unit="MINUTE";
        trackRide.distance.value=119.60000000000001;
        trackRide.distance.unit="KMS";
        trackRide.trip_info.amount=1013;
        trackRide.trip_info.payable_amount= 121;
        trackRide.trip_info.wait_time.value= 10;
        trackRide.trip_info.wait_time.unit ="MINUTE";
        trackRide.trip_info.discount = 90;
        trackRide.trip_info.advance =10;

        //FIXME fare breakup has to be an array
        trackRide.fareBreakup.display_text= "First 5 km:";
        trackRide.fareBreakup.value=10;
        /**
         * Smple response here...
         *
         * {
         "status": "SUCCESS",
         "request_type": "TRACK_RIDE",
         "booking_status": "COMPLETED",
         "crn": "82053435",
         "ola_money_balance": 28,
         "trip_info": {
         "amount": 101,
         "payable_amount": 0,
         "distance": {
         "value": 0,
         "unit": "KMS"
         },
         "wait_time": {
         "value": 0,
         "unit": "MINUTE"
         },
         "discount": 90,
         "advance": 11
         },
         "fare_breakup": [
         {
         "display_text": "First 5 km: ",
         "value": 10
         },
         {
         "display_text": "Charge above base fare: ",
         "value": 0
         },
         {
         "display_text": "Wait time charge: ",
         "value": 0
         }
         ]
         }

         */

       return trackRide;
    }

}
