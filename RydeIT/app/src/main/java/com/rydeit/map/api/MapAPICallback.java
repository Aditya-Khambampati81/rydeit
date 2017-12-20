package com.rydeit.map.api;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Helper class that extends the Retrofit Callback and implements the default failure method.
 */
public class MapAPICallback<T> implements Callback<T> {

    @Override
    public void success(T t, Response response) {

    }

    @Override
    public void failure(RetrofitError error) {
        error.printStackTrace();
    }

}
