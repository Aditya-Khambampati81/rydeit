package com.rydeit.cab.service.ola;

import android.app.Activity;
import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.rydeit.util.AndroidUtils;

import java.util.HashMap;

/**
 * Created by Prakhyath on 10/13/15.
 */

public class OlaAPIConstants {

    private static HashMap<String, String> authParameters = new HashMap<String, String>();

    public static final String AUTHORIZE_URL = "https://devapi.olacabs.com/oauth2/authorize";
    public static final String BASE_OLA_URL_V1 = "https://devapi.olacabs.com/v1/";

    public static class OAUTH_AUTH_PARAM{
        public static final String RESPONSE_TYPE="response_type";
        public static final String RESPONSE_TYPE_VALUE="token";
        public static final String CLIENT_ID="client_id";
        public static final String SCOPE="scope";
        public static final String SCOPE_VALUE = "profile booking";
        public static final String REDIRECT_URI="redirect_uri";
        public static final String STATE="state";
        public static final String STATE_VALUE="state123";
    }

    public static class OAUTH_REDIRECT_PARAM{
        public static final String ACCESS_TOKEN="access_token";
        public static final String TOKEN_TYPE="token_type";
        public static final String EXPIRES_IN="expires_in";
    }

    public static class BOOKING_STATUS {
        public static final String CALL_DRIVER = "CALL_DRIVER";
        public static final String CLIENT_LOCATED = "CLIENT_LOCATED";
        public static final String IN_PROGRESS = "IN_PROGRESS";
        public static final String COMPLETED = "COMPLETED";
        public static final String NO_BOOKING = "NO_BOOKING";
    }

    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static String getOlaXAppToken(Context activity) {
        return AndroidUtils.getManifestData(activity, "com.rydeit.OLA_X_APP_TOKEN");
    }

    public static String getOlaClientId(Context activity) {
        return AndroidUtils.getManifestData(activity, "com.rydeit.OLA_CLIENT_ID");
    }

    public static String getOlaRedirectUrl(Context activity) {
        return AndroidUtils.getManifestData(activity, "com.rydeit.OLA_REDIRECT_URL");
    }

}
