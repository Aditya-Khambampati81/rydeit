package com.rydeit.flipkart;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by Aditya.Khambampati on 12/5/2015.
 */
public class FlipkartApiCallback<T> implements Callback<T> {

        @Override
        public void success(T t, Response response) {

        }

        @Override
        public void failure(RetrofitError error) {
            error.printStackTrace();
        }

}
