package com.rydeit.util;

import org.json.JSONObject;

/**
 * Created by Aditya.Khambampati on 11/4/2015.
 */
public class ConfigurationObject {

    public static int pollfrequency = 12;
    public static boolean captureanalytics  = true;
    public static boolean uberwithin = false;
    public static boolean flippull = true;
    public static String playStoreLatestAppVersion="1.0";

    public static JSONObject playStoreLatestAppDetails;

    public static final String ANALYTICS="analytics";
    public static final String POLL_FREQ="pollfreq";
    public static final String UBER_WITHIN="uberwithin";
    public static final String FLIPCART_PULL="flipkartpull";
    public static final String PLAYSTORE_APP_VERSION="playstoreappversion";
    public static final String PLAYSTORE_APP_VERSION1="playstoreappversion1";
}
