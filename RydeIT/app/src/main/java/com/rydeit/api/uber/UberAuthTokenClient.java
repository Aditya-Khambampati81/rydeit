package com.rydeit.api.uber;

import com.rydeit.BuildConfig;
import com.rydeit.util.Constants;
import com.rydeit.model.uber.User;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.http.POST;
import retrofit.http.Query;

public class UberAuthTokenClient {

    private static UberAuthTokenInterface sUberAuthService;

    public static UberAuthTokenInterface getUberAuthTokenClient() {
        if (sUberAuthService == null) {
            RestAdapter restAdapter = new RestAdapter.Builder()
                    .setEndpoint(Constants.BASE_URL)
                    .setLogLevel(BuildConfig.DEBUG ? RestAdapter.LogLevel.FULL : RestAdapter.LogLevel.NONE)
                    .build();

            sUberAuthService = restAdapter.create(UberAuthTokenInterface.class);
        }

        return sUberAuthService;
    }

    public interface UberAuthTokenInterface {

        /**
         * Exchange this authorization code for an access_token, which will allow you to make
         * requests on behalf of a user. The access_token expires in 30 days.
         *
         * @param clientSecret A 40 character string. DO NOT SHARE. This should not be available on
         *                     any public facing server or web site.
         * @param clientId     A 32 character string (public)
         * @param grantType    May be authorization_code or refresh_token
         * @param code
         * @param redirectUrl
         * @param callback
         */
        @POST("/oauth/token")
        void getAuthToken(@Query("client_secret") String clientSecret,
                          @Query("client_id") String clientId,
                          @Query("grant_type") String grantType,
                          @Query("code") String code,
                          @Query("redirect_uri") String redirectUrl,
                          Callback<User> callback);


        /***
         * Uber API to refresh auth token ...
         * @param clientSecret
         * @param clientId
         * @param grantType
         * @param redirectUrl
         * @param refreshToken
         * @param callback
         */
        @POST("/oauth/v2/token")
        void refreshAuthToken(@Query("client_secret") String clientSecret,
                          @Query("client_id") String clientId,
                          @Query("grant_type") String grantType,
                          @Query("redirect_uri") String redirectUrl,
                          @Query("refresh_token") String refreshToken,
                          Callback<User> callback);






    }

}
