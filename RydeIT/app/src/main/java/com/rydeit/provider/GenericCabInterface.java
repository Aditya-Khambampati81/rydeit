package com.rydeit.provider;

import retrofit.Callback;

/**
 * Created by Aditya.Khambampati on 1/1/2016.
 */
public interface GenericCabInterface<T> {

    public void bookCab( String estimateId,String Token, String AccesToken , Double latitude, Double longitude, Callback<T> callback);
    public void getRideStatus( String requestId, Callback<T> callback);
    public void cancelRide(String rid, String Token , String AccessToken,Callback<T> callback);
    public String getAccessToken();
}
