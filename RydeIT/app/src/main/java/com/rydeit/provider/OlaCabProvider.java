package com.rydeit.provider;

import com.rydeit.api.ola.OlaAPIClient;
import com.rydeit.cab.service.ola.OlaAPIConstants;
import com.rydeit.cab.service.ola.OlaSandBox;
import com.rydeit.util.AndroidUtils;
import com.rydeit.util.Constants;
import com.rydeit.util.SharedPrefUtil;

import retrofit.Callback;

/**
 * Created by Aditya.Khambampati on 1/1/2016.
 */
public class OlaCabProvider implements  GenericCabInterface {



    private static OlaCabProvider sInstance = new OlaCabProvider() ;


    public static OlaCabProvider getOlaCabProvider()
    {
        return sInstance;
    }



    private OlaCabProvider()
    {

    }



    @Override
    public void bookCab( String estimateId,String Token, String AccesToken , Double latitude, Double longitude, Callback callback) {

        if(Constants.SIMULATE_BOOKING){
            callback.success(OlaSandBox.getSimulateRideBookingObject(), null);
            return;
        }

        OlaAPIClient.getOlaV1APIClient().bookRide(OlaAPIConstants.getOlaXAppToken(AndroidUtils.getAppContext()),
                Token + " " + AccesToken,
                latitude,
                longitude,
                "NOW",
                estimateId,
                callback);
    }

    @Override
    public void getRideStatus( String requestId , Callback callback) {
        if(Constants.SIMULATE_BOOKING){
            callback.success(OlaSandBox.getSimulatedRideTrackObject(), null);
            return;
        }


        OlaAPIClient.getOlaV1APIClient().trackRide(OlaAPIConstants.getOlaXAppToken(AndroidUtils.getAppContext()),
                getAccessToken(),
               callback
        );
    }

    @Override
    public void cancelRide(String rid, String Token , String AccessToken, Callback callback) {
        OlaAPIClient.getOlaV1APIClient().cancelRide(OlaAPIConstants.getOlaXAppToken(AndroidUtils.getAppContext()),
                Token + " " + AccessToken,
                rid,
               callback);
    }

    public String getAccessToken() {

        String AccessToken=null,TokenType=null;
        Constants.CABCOMPANY CabCompany=Constants.CAB_INDIA.OLA;
        if(SharedPrefUtil.getStringPreference(AndroidUtils.getAppContext(), Constants.getAccessTokenKey(CabCompany))!=null) {
            AccessToken = SharedPrefUtil.getStringPreference(AndroidUtils.getAppContext(), Constants.getAccessTokenKey(CabCompany));
            TokenType = SharedPrefUtil.getStringPreference(AndroidUtils.getAppContext(), Constants.getTokenTypeKey(CabCompany));
            if(TokenType.equals("bearer")){
                TokenType="Bearer";//FIXME- TEMP FIX TO OLA API ISSUE
            }
        }
        return TokenType + " " + AccessToken;
    }


}
