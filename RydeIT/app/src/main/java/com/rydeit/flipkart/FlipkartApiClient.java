package com.rydeit.flipkart;

import com.rydeit.BuildConfig;
import com.rydeit.model.uber.ProductList;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.http.GET;
import retrofit.http.Header;
import retrofit.http.Query;

/**
 * Created by Aditya.Khambampati on 12/5/2015.
 * API client to poll flipkart api to get latest offers
 * The idea is to use poll for flipkart deal of the dat offer
 */
public class FlipkartApiClient {

    public static final String affiliateID = "reachryde";
    public static final String affiliateToken = "f78f403402e44b0b8835fc2a02f2bac5";
    public static FlipkartApiInterface fInterface = null;
    public static String flipkartendpoint = "https://affiliate-api.flipkart.net";

    public static FlipkartApiInterface getfInterface() {
        if(fInterface == null)
        {
            RestAdapter restAdapter = new RestAdapter.Builder()
                    .setEndpoint(flipkartendpoint)
                    .setLogLevel(BuildConfig.DEBUG ? RestAdapter.LogLevel.FULL : RestAdapter.LogLevel.NONE)
                    .build();
            fInterface = restAdapter.create(FlipkartApiInterface.class);
        }

        return fInterface;
    }

    public interface FlipkartApiInterface {
        // here flipkart apis needs to be defined as per retrofit guidelines

        /**
         *
         * @param affid  - flipkart affiliate ID
         * @param affToken - Flipkart affiliate Token
         * @param callback - Callback to be invoked
         *
         */
        @GET("/affiliate/offers/v1/dotd/json")
        void getDealOfTheDay(@Header("Fk-Affiliate-Id") String affid,
                       @Header("Fk-Affiliate-Token") String affToken,
                       Callback<FlipkartOffers> callback);
    }
}
