package com.rydeit.api.ola;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by Prakhyath on 10/13/15.
 */

/**
 * Helper class that extends the Retrofit Callback and implements the default failure method.
 */
public class OlaCallback<T> implements Callback<T> {


    @Override
    public void success(T t, Response response) {

    }

    @Override
    public void failure(RetrofitError error) {
        error.printStackTrace();
    }

}
