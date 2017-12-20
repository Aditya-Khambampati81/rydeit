package com.rydeit.util;

import android.app.Activity;
import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.rydeit.view.MapCabFinder;

import java.util.HashMap;

public class Constants {


    public static boolean SIMULATE_BOOKING=false;
    public static final boolean ENABLE_DEBUG_TOAST=false;

    public interface CABCOMPANY{};
    public enum CAB_GLOBAL implements CABCOMPANY{
        UBER
    };

    public enum CAB_INDIA implements CABCOMPANY{
        OLA,TAXIFORSURE, MERU,EASYCAB
    };

    public enum CAB_USA implements CABCOMPANY{
        LYFT
    };

    private static final String SHARED_PREF_ACCESSTOKEN="ACCESS_TOKEN";
    private static final String SHARED_PREF_TOKENTYPE="TOKEN_TYPE";
    private static final String SHARED_PREF_TOKENVALIDITY="TOKEN_VALIDITY";
    private static final String SHARED_PREF_REFRESHTOKEN="REFRESH_TOKEN";


    public static HashMap<String, String> authParameters = new HashMap<String, String>();

    public static final String AUTHORIZE_URL = "https://login.uber.com/oauth/authorize";
    public static final String BASE_URL = "https://login.uber.com/";
    public static final String SCOPES = "profile history_lite history request request_receipt";

    public static final String BASE_UBER_URL_V1 = "https://api.uber.com/v1/";
    public static final String BASE_UBER_URL_V1_1 = "https://api.uber.com/v1.1/";
    public static final String BASE_UBER_URL_V1_2 = "https://api.uber.com/v1.2/";

    public static final String BASE_UBER_SANDBOX_URL_V1 = "https://sandbox-api.uber.com/v1/";

    public static double START_LATITUDE = MapCabFinder.PIN_LATITUDE;// 12.9569115;//Manipal Hospital
    public static double START_LONGITUDE = MapCabFinder.PIN_LONGITUDE;//77.648981;
    public static final double END_LATITUDE = 12.9575456;//Marathalli
    public static final double END_LONGITUDE = 77.6517453;

    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static String getUberClientId(Activity activity) {
        return AndroidUtils.getManifestData(activity, "com.rydeit.UBER_CLIENT_ID");
    }

    public static String getUberClientSecret(Activity activity) {
        return AndroidUtils.getManifestData(activity, "com.rydeit.UBER_CLIENT_SECRET");
    }

    public static String getUberRedirectUrl(Activity activity) {
        return AndroidUtils.getManifestData(activity, "com.rydeit.UBER_REDIRECT_URL");
    }

    public static String getUberServerToken(Context context) {
        return AndroidUtils.getManifestData(context, "com.rydeit.UBER_SERVER_TOKEN");
    }

    public static String getAccessTokenKey(CABCOMPANY cabcompany){
        return cabcompany+SHARED_PREF_ACCESSTOKEN;
    }

    public static String getTokenTypeKey(CABCOMPANY cabcompany){
        return cabcompany+SHARED_PREF_TOKENTYPE;
    }

    public static String getTokenValidityKey(CABCOMPANY cabcompany){
        return cabcompany+SHARED_PREF_TOKENVALIDITY;
    }

    public static String getRefreshTokenKey(CABCOMPANY cabcompany){
        return cabcompany+SHARED_PREF_REFRESHTOKEN;
    }

    public class URL{
        public static final String LICENSES="file:///android_asset/mylicenses.html";
        public static final String DEVELOPER_SITE="http://bsoftlabs.in";
    }

}
