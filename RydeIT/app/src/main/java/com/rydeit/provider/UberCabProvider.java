package com.rydeit.provider;

import com.rydeit.api.uber.UberAPIClient;
import com.rydeit.model.uber.Requests.UberRequestBody;
import com.rydeit.util.AndroidUtils;
import com.rydeit.util.Constants;
import com.rydeit.util.SharedPrefUtil;

import retrofit.Callback;

/**
 * Created by Aditya.Khambampati on 1/1/2016.
 */
public class UberCabProvider implements GenericCabInterface {


    private static UberCabProvider sInstance  = new UberCabProvider();

    private UberCabProvider(){

    }

    public static UberCabProvider getUberCabProvider()
    {
        return sInstance;
    }
    /**
     *
     * @param estimateId
     * @param surgeConfirmationId -- for uber use token for surge confirmation if
     * @param AccesToken
     * @param latitude
     * @param longitude
     * @param callback
     */
    @Override
    public void bookCab(String  estimateId,String surgeConfirmationId, String AccesToken, Double latitude, Double longitude, Callback callback) {

        UberRequestBody uberRequestBody = new UberRequestBody(estimateId, latitude,longitude, surgeConfirmationId);

        if (Constants.SIMULATE_BOOKING) {
            UberAPIClient.getUberV1SandBoxAPIClient().rideRequest(getAccessToken(), uberRequestBody, callback);
        }
        else{
            UberAPIClient.getUberV1APIClient().rideRequest(getAccessToken(), uberRequestBody, callback);
        }
    }

    /**
     * @param requestId - pass request id here for OLA there is no need to pass
     * @param callback
     */
    @Override
    public void getRideStatus(final String requestId, Callback callback) {

        if (Constants.SIMULATE_BOOKING) {
            UberAPIClient.getUberV1SandBoxAPIClient().rideRequestDetails(getAccessToken(), requestId, callback);
        }
        else{
            UberAPIClient.getUberV1APIClient().rideRequestDetails(getAccessToken(), requestId, callback);
        }

    }

    /**
     *
     * @param rid  - Id of trip to be cancelled.
     * @param Token 0 Access token no need to pass for uber
     * @param AccessToken - Access tokne no need to pass for uber
     * @param callback - Callback for ride response..
     */
    @Override
    public void cancelRide(String rid, String Token, String AccessToken, Callback callback) {

        UberAPIClient.getUberV1APIClient().deleteRequest(getAccessToken(),
                rid,
                callback);

    }

    /*public String getAccessToken(Constants.CABCOMPANY CabCompany) {

        String AccessToken=null,TokenType=null;
        if(SharedPrefUtil.getStringPreference(AndroidUtils.getAppContext(), Constants.getAccessTokenKey(CabCompany))!=null) {
            AccessToken = SharedPrefUtil.getStringPreference(AndroidUtils.getAppContext(), Constants.getAccessTokenKey(CabCompany));
            TokenType = SharedPrefUtil.getStringPreference(AndroidUtils.getAppContext(), Constants.getTokenTypeKey(CabCompany));
        }
        return TokenType + " " + AccessToken;
    }*/

    public String getAccessToken() {

        String AccessToken=null,TokenType=null;
        Constants.CABCOMPANY CabCompany=Constants.CAB_GLOBAL.UBER;
        if(SharedPrefUtil.getStringPreference(AndroidUtils.getAppContext(), Constants.getAccessTokenKey(CabCompany))!=null) {
            AccessToken = SharedPrefUtil.getStringPreference(AndroidUtils.getAppContext(), Constants.getAccessTokenKey(CabCompany));
            TokenType = SharedPrefUtil.getStringPreference(AndroidUtils.getAppContext(), Constants.getTokenTypeKey(CabCompany));
        }
        return TokenType + " " + AccessToken;
    }
}
