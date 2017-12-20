package com.rydeit.api.ola;

import com.rydeit.BuildConfig;
import com.rydeit.model.ola.CancelResponse;
import com.rydeit.model.ola.RideAvailability;
import com.rydeit.model.ola.RideBooking;
import com.rydeit.model.ola.RideEstimateList;
import com.rydeit.model.ola.TrackRide;
import com.rydeit.cab.service.ola.OlaAPIConstants;

import retrofit.Callback;
import retrofit.Endpoint;
import retrofit.RestAdapter;
import retrofit.http.GET;
import retrofit.http.Header;
import retrofit.http.Query;

/**
 * Created by Prakhyath on 10/13/15.
 */

public class OlaAPIClient {

    private static IcabApiInterface sOlaAPIService;
    private static OLAEndPoint sEndPoint = new OLAEndPoint(OlaAPIConstants.BASE_OLA_URL_V1);
    private enum API_URL { APIURLV1};

    private static IcabApiInterface getOlaAPIClient() {
        if (sOlaAPIService == null) {
            RestAdapter restAdapter = new RestAdapter.Builder()
                    .setEndpoint(sEndPoint)
                    .setLogLevel(BuildConfig.DEBUG ? RestAdapter.LogLevel.FULL : RestAdapter.LogLevel.NONE)
                    .build();

            sOlaAPIService = restAdapter.create(IcabApiInterface.class);
        }

        return sOlaAPIService;
    }

    public static IcabApiInterface getOlaV1APIClient() {
        sEndPoint.setVersion(API_URL.APIURLV1);
        return getOlaAPIClient();
    }

    public interface IcabApiInterface {

        /**
         * The Products endpoint returns information about the Ola products offered at a given
         * location. The response includes the display name and other details about each product,
         * and lists the products in the proper display order.
         *
         * @param xAppToken Key which identifies the partner.
         * @param latitude  Latitude component of location.
         * @param longitude Longitude component of location.
         * @param cabtype cab type
         * @param callback
         */
        @GET("/products")
        void getRydeAvailability(@Header("X-APP-TOKEN") String xAppToken,
                         @Query("pickup_lat") double latitude,
                         @Query("pickup_lng") double longitude,
                         @Query("category") String cabtype,
                         Callback<RideAvailability> callback);

        /**
         *
         * @param xAppToken  Key which identifies the partner.
         * @param pickupLatitude  pickup Latitude component.
         * @param pickupLongitude pickup Longitude component.
         * @param dropLatitude  pickup Latitude component.
         * @param dropLongitude pickup Longitude component.
         * @param cabtype cab type
         * @param callback
         */
        @GET("/products")
        void getRydeEstimates(@Header("X-APP-TOKEN") String xAppToken,
                              @Query("pickup_lat") double pickupLatitude,
                              @Query("pickup_lng") double pickupLongitude,
                              @Query("drop_lat") double dropLatitude,
                              @Query("drop_lng") double dropLongitude,
                              @Query("category") String cabtype,
                              Callback<RideEstimateList> callback);

        /**
         *
         * @param xAppToken Key which identifies the partner.
         * @param AuthToken  OAuth 2.0 bearer token
         * @param pickupLatitude  pickup Latitude component.
         * @param pickupLongitude pickup Longitude component.
         * @param pickupmode  pickup model.
         * @param cabtype cab type
         * @param callback
         */
        @GET("/bookings/create")
        void bookRide(@Header("X-APP-TOKEN") String xAppToken,
                      @Header("Authorization") String AuthToken,
                              @Query("pickup_lat") double pickupLatitude,
                              @Query("pickup_lng") double pickupLongitude,
                              @Query("pickup_mode") String pickupmode,
                              @Query("category") String cabtype,
                              Callback<RideBooking> callback);

        /**
         *
         * @param xAppToken Key which identifies the partner.
         * @param AuthToken  OAuth 2.0 bearer token
         * @param callback
         */
        @GET("/bookings/track_ride")
        void trackRide(@Header("X-APP-TOKEN") String xAppToken,
                      @Header("Authorization") String AuthToken,
                      Callback<TrackRide> callback);

        /**
         *
         * @param xAppToken Key which identifies the partner.
         * @param AuthToken  OAuth 2.0 bearer token
         * @param crn  Booking ID for the cab ride.
         * @param callback
         */
        @GET("/bookings/cancel")
        void cancelRide(@Header("X-APP-TOKEN") String xAppToken,
                      @Header("Authorization") String AuthToken,
                      @Query("crn") String  crn,
                      Callback<CancelResponse> callback);

    }

    private static class OLAEndPoint implements Endpoint {

        private final String apiUrlV1;
        private API_URL Api_Url;

        private OLAEndPoint(String apiUrlV1) {
            this.apiUrlV1 = apiUrlV1;
        }

        public void setVersion(API_URL Api_Url) {
            this.Api_Url = Api_Url;
        }

        @Override
        public String getUrl() {

            if(Api_Url== API_URL.APIURLV1)
                return apiUrlV1;
            else
                return apiUrlV1;
        }

        @Override
        public String getName() {
            return "default";
        }
    }
}
