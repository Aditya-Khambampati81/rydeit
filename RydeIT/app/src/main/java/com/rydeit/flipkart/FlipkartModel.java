package com.rydeit.flipkart;

import com.rydeit.util.Constants;

/**
 * Created by Aditya.Khambampati on 12/5/2015.
 */
public class FlipkartModel {
    @Override
    public String toString() {
        return Constants.GSON.toJson(this);
    }

}
