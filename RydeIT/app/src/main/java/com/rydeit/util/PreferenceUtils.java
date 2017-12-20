package com.rydeit.util;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Aditya.Khambampati on 11/4/2015.
 */
public class PreferenceUtils {

    private static PreferenceUtils sInstance = null;
    private static final Object lock = new Object();
    public static final String LAST_CONFIG_PULL_TIME = "config_pull_time";
    public static final String FLIP_PULL_TIME = "flipkart_pull_time";
    private static final String PREFS_FILE = "my_prefs";

    public static PreferenceUtils getInstance() {
        synchronized (lock) {
            if (sInstance == null) {
                sInstance = new PreferenceUtils();
            }

        }
        return sInstance;

    }

    private PreferenceUtils() {

    }

    public  void updateConfigFetchTime(Context cxt, long UpdatedTimeStamp) {

        if (cxt == null) {
            throw new IllegalArgumentException("Please pass valid context ....");

        } else {
            SharedPreferences storeHeartBeatInfoPref = cxt.getApplicationContext().getSharedPreferences(
                    PreferenceUtils.PREFS_FILE,
                    Context.MODE_MULTI_PROCESS);
            SharedPreferences.Editor editor = storeHeartBeatInfoPref.edit();
            editor.putLong(LAST_CONFIG_PULL_TIME, UpdatedTimeStamp);
            editor.commit();

        }
    }

    public  long getLastConfigTime(Context cxt) {
        SharedPreferences getHeartBeatPref = cxt.getApplicationContext().getSharedPreferences(
                PreferenceUtils.PREFS_FILE,
                Context.MODE_MULTI_PROCESS);
        long timeStamp = getHeartBeatPref.getLong(LAST_CONFIG_PULL_TIME, 0);

        return timeStamp;
    }


    public  long getLastflipkartOffertime(Context cxt) {
        SharedPreferences getHeartBeatPref = cxt.getApplicationContext().getSharedPreferences(
                PreferenceUtils.PREFS_FILE,
                Context.MODE_MULTI_PROCESS);
        long timeStamp = getHeartBeatPref.getLong(FLIP_PULL_TIME, 0);

        return timeStamp;
    }

    public  void updateFlipkartPullTime(Context cxt, long UpdatedTimeStamp) {

        if (cxt == null) {
            throw new IllegalArgumentException("Please pass valid context ....");

        } else {
            SharedPreferences storeHeartBeatInfoPref = cxt.getApplicationContext().getSharedPreferences(
                    PreferenceUtils.PREFS_FILE,
                    Context.MODE_MULTI_PROCESS);
            SharedPreferences.Editor editor = storeHeartBeatInfoPref.edit();
            editor.putLong(FLIP_PULL_TIME, UpdatedTimeStamp);
            editor.commit();

        }
    }
}
