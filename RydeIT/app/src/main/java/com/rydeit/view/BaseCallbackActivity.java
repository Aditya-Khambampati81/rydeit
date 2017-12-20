package com.rydeit.view;

import com.rydeit.api.ola.OlaAPIClient;
import com.rydeit.api.ola.OlaCallback;
import com.rydeit.cab.service.ola.OlaAPIConstants;
import com.rydeit.cab.service.ola.OlaSandBox;
import com.rydeit.model.ola.RideBooking;
import com.rydeit.model.ola.TrackRide;
import com.rydeit.model.uber.RideRequest;
import com.rydeit.uilibrary.BaseActivityActionBar;
import com.rydeit.util.Constants;
import com.rydeit.util.Log;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by Aditya.Khambampati on 12/31/2015.
 * FIXME : this is work in progresss .. later all confirm booking activity and others will be moved here...
 *
 */
public abstract  class BaseCallbackActivity<T> extends BaseActivityActionBar implements Callback<T> {

    /**
     * Uber ride request object
     * @param rideRequest
     * @param response
     * @param error
     */
    public abstract void uberRideStatus(RideRequest rideRequest,Response response, RetrofitError error);

    /**
     * OLA ride request objecct
     * @param rideRequest
     * @param resp
     * @param error
     */
    public abstract void  olaRideBooking(RideBooking rideRequest , Response resp , RetrofitError error);

    /**
     * Abstract classes that needs to be implemented by extended classes
     * @param trackRide
     * @param resp
     * @param error
     */
    public abstract void olaTrackRide(TrackRide trackRide ,Response resp ,RetrofitError error);

    @Override
    public void success(T t, Response response) {

        if (t instanceof  RideRequest)
        {
            uberRideStatus((RideRequest)t,response,null);
        }
        else if(t instanceof  RideBooking)
        {
            olaRideBooking((RideBooking) t, response, null);

        }
        else if (t instanceof TrackRide)
        {
            olaTrackRide((TrackRide)t,response,null);
        }
        else
        {
            Log.i("BaseCallbackActivity", "Something really weird here");
            //Some other company here...

        }
    }

    @Override
    public void failure(RetrofitError error) {
        error.printStackTrace();

    }



}
