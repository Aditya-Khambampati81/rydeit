package com.rydeit.api.uber;

import com.rydeit.BuildConfig;
import com.rydeit.model.uber.PriceEstimateList;
import com.rydeit.model.uber.ProductList;
import com.rydeit.model.uber.Profile;
import com.rydeit.model.uber.Requests.UberRequestBody;
import com.rydeit.model.uber.RideRequest;
import com.rydeit.model.uber.TimeEstimateList;
import com.rydeit.model.uber.UserActivity;
import com.rydeit.model.uber.sandbox.SandboxProductBody;
import com.rydeit.model.uber.sandbox.SandboxRequestBody;
import com.rydeit.util.Constants;

import retrofit.Callback;
import retrofit.Endpoint;
import retrofit.RestAdapter;
import retrofit.client.Response;
import retrofit.http.Body;
import retrofit.http.DELETE;
import retrofit.http.GET;
import retrofit.http.Header;
import retrofit.http.POST;
import retrofit.http.PUT;
import retrofit.http.Path;
import retrofit.http.Query;

public class UberAPIClient {

    private static IcabApiInterface sUberAPIService;
    private static UberEndPoint sEndPoint = new UberEndPoint(Constants.BASE_UBER_URL_V1, Constants.BASE_UBER_URL_V1_1,
            Constants.BASE_UBER_URL_V1_2, Constants.BASE_UBER_SANDBOX_URL_V1);
    private enum API_URL { APIURLV1, APIURLV11, APIURLV22, APISANBOXURLV1 };

    private static IcabApiInterface getUberAPIClient() {
        if (sUberAPIService == null) {
            RestAdapter restAdapter = new RestAdapter.Builder()
                    .setEndpoint(sEndPoint)
                    .setLogLevel(BuildConfig.DEBUG ? RestAdapter.LogLevel.FULL : RestAdapter.LogLevel.NONE)
                    .build();

            sUberAPIService = restAdapter.create(IcabApiInterface.class);
        }

        return sUberAPIService;
    }

    public static IcabApiInterface getUberV1APIClient() {
        sEndPoint.setVersion(API_URL.APIURLV1);
        return getUberAPIClient();
    }

    public static IcabApiInterface getUberV1_1APIClient() {
        sEndPoint.setVersion(API_URL.APIURLV11);
        return getUberAPIClient();
    }

    public static IcabApiInterface getUberV1_2APIClient() {
        sEndPoint.setVersion(API_URL.APIURLV22);
        return getUberAPIClient();
    }

    public static IcabApiInterface getUberV1SandBoxAPIClient() {
        sEndPoint.setVersion(API_URL.APISANBOXURLV1);
        return getUberAPIClient();
    }

    public interface IcabApiInterface {

        /**
         * The Products endpoint returns information about the Uber products offered at a given
         * location. The response includes the display name and other details about each product,
         * and lists the products in the proper display order.
         *
         * @param authToken OAuth 2.0 bearer token with the profile scope.
         * @param latitude  Latitude component of location.
         * @param longitude Longitude component of location.
         * @param callback
         */
        @GET("/products")
        void getProducts(@Header("Authorization") String authToken,
                         @Query("latitude") double latitude,
                         @Query("longitude") double longitude,
                         Callback<ProductList> callback);

        /**
         * The Time Estimates endpoint returns ETAs for all products offered at a given location,
         * with the responses expressed as integers in seconds. We recommend that this endpoint be
         * called every minute to provide the most accurate, up-to-date ETAs.
         *
         * @param authToken      OAuth 2.0 bearer token or server_token
         * @param startLatitude  Latitude component.
         * @param startLongitude Longitude component.
         * @param callback
         */
        @GET("/estimates/time")
        void getTimeEstimates(@Header("Authorization") String authToken,
                              @Query("start_latitude") double startLatitude,
                              @Query("start_longitude") double startLongitude,
                              Callback<TimeEstimateList> callback);

        /**
         * The Price Estimates endpoint returns an estimated price range for each product offered
         * at a given location. The price estimate is provided as a formatted string with the full
         * price range and the localized currency symbol.
         * <p/>
         * The response also includes low and high estimates, and the ISO 4217 currency code for
         * situations requiring currency conversion. When surge is active for a particular product,
         * its surge_multiplier will be greater than 1, but the price estimate already factors in
         * this multiplier.
         *
         * @param authToken      OAuth 2.0 bearer token or server_token
         * @param startLatitude  Latitude component of start location.
         * @param startLongitude Longitude component of start location.
         * @param endLatitude    Longitude component of start location.
         * @param endLongitude   Longitude component of end location.
         * @param callback
         */
        @GET("/estimates/price")
        void getPriceEstimates(@Header("Authorization") String authToken,
                               @Query("start_latitude") double startLatitude,
                               @Query("start_longitude") double startLongitude,
                               @Query("end_latitude") double endLatitude,
                               @Query("end_longitude") double endLongitude,
                               Callback<PriceEstimateList> callback);

        /**
         * The User Activity endpoint returns data about a user's lifetime activity with Uber. The
         * response will include pickup locations and times, dropoff locations and times, the
         * distance of past requests, and information about which products were requested.
         * <p/>
         * The history array in the response will have a maximum length based on the limit parameter.
         * The response value count may exceed limit, therefore subsequent API requests may be
         * necessary.
         *
         * @param authToken OAuth 2.0 bearer token with the history scope.
         * @param offset    Offset the list of returned results by this amount. Default is zero.
         * @param limit     Number of items to retrieve. Default is 5, maximum is 100.
         * @param callback
         */
        @GET("/history")
        void getUserActivity(@Header("Authorization") String authToken,
                             @Query("offset") int offset,
                             @Query("limit") int limit,
                             Callback<UserActivity> callback);

        /**
         * The User Profile endpoint returns information about the Uber user that has authorized
         * with the application.
         *
         * @param authToken OAuth 2.0 bearer token with the profile scope.
         * @param callback
         */
        @GET("/me")
        void getProfile(@Header("Authorization") String authToken,
                        Callback<Profile> callback);

        /** Request APIs **/
        /**
         *
         * @param authToken OAuth 2.0 bearer token with the history scope.
         * productId  unique product id
         * pickupLatitude  pickup Latitude component.
         * pickupLongitude pickup Longitude component.
         * dropLatitude  drop Latitude component.
         * dropLongitude drop Longitude component.
         * @param callback
         */
        @POST("/requests")
        void rideRequest(@Header("Authorization") String authToken,
                         @Body UberRequestBody body,
                         Callback<RideRequest> callback);

        /**
         *
         * @param authToken OAuth 2.0 bearer token with the history scope.
         * @param callback
         */
        @GET("/requests/{request_id}")
        void rideRequestDetails(@Header("Authorization") String authToken,
                                @Path("request_id") String request_id, Callback<RideRequest> callback);


        /**
         * Cancel an ongoing Request on behalf of a rider
         *
         * @param requestId Unique identifier representing a Request.
         * @return
         */
        @DELETE("/requests/{request_id}")
        void deleteRequest(@Header("Authorization") String authToken,
                           @Path("request_id") String requestId, Callback<Response> callback);


        /**
         * Used to simulate the possible responses the Request endpoint will return when requesting a particular product,
         * such as surge pricing, against the Sandbox.
         *
         * Accepts a JSON body indicating what you would like the surge_multiplier to be when making a Request to a particular Product.
         * @param productId Unique identifier representing a specific product for a given latitude & longitude. For example,
         *                  uberX in San Francisco will have a different product_id than uberX in Los Angeles.
         * @param sandboxProductBody Accepts a JSON body indicating what you would like the surge_multiplier to be when
         *                           making a Request to a particular Product.
         * @return Status-code: 204 Success
         */
        @PUT("/sandbox/products/{product_id}")
        Response putProducts(@Header("Authorization") String authToken,
                             @Path("product_id") String productId, @Body SandboxProductBody sandboxProductBody);

        /**
         *
         * @param requestId The unique ID of the Request.
         * @param requestBody The value to change a Request's status to. Ex, {"status": "accepted"}
         * @return Status-code: 204 Success
         */
        @PUT("/sandbox/requests/{request_id}")
        Response putRequest(@Header("Authorization") String authToken,
                            @Path("request_id") String requestId, @Body SandboxRequestBody requestBody);

        /**
         * API to getCabType
         * OLA,UBER,TFS, MERY,OTHER
         */

        CabType getCabType();


        public enum CabType
        {
            TYPE_OLA,
            TYPE_UBER,
            TYPE_TFS,
            TYPE_MERU,
            TYPE_OTHER
        }
    }

    private static class UberEndPoint implements Endpoint {

        private final String apiUrlV1, apiUrlV11, apiUrlV12, apiSandboxUrlV1;
        private API_URL Api_Url;

        private UberEndPoint(String apiUrlV1, String apiUrlV11, String apiUrlV12, String apiSanboxUrlV1) {
            this.apiUrlV1 = apiUrlV1;
            this.apiUrlV11 = apiUrlV11;
            this.apiUrlV12 = apiUrlV12;
            this.apiSandboxUrlV1 = apiSanboxUrlV1;
        }

        public void setVersion(API_URL Api_Url) {
            this.Api_Url = Api_Url;
        }

        @Override
        public String getUrl() {

            if(Api_Url==API_URL.APIURLV11)
                return apiUrlV11;
            else if(Api_Url==API_URL.APIURLV22)
                return apiUrlV12;
            else if(Api_Url==API_URL.APISANBOXURLV1)
                return apiSandboxUrlV1;
            else
                return apiUrlV1;
        }

        @Override
        public String getName() {
            return "default";
        }
    }
}
