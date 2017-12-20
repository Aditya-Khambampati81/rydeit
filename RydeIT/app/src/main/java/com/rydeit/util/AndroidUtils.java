package com.rydeit.util;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import com.rydeit.flipkart.FlipkartOffers;
import com.rydeit.push.model.PushMessage;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Prakhyath on 11/14/15.
 */
public class AndroidUtils {

    // one copy of static application context to be used across app
    private static Context mAppContext = null;


    public static void  setAppContext(Context cxt)
    {
        mAppContext = cxt;
    }


    public static Context getAppContext()
    {
        return mAppContext;
    }
    /**
     * To Change the color of Notification Bar for OS higher than Andorid L
     * @param activity
     * @param color
     */
    public static void setNotificationBarColor(Activity activity, int color){
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = activity.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(activity.getResources().getColor(color));
        }
    }

    public static String getManifestData(Context context, String name) {
        String data = Constants.authParameters.get(name);
        if (data != null) {
            return data;
        }
        try {
            ApplicationInfo ai = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            Bundle bundle = ai.metaData;
            data = bundle.getString(name);
            Constants.authParameters.put(name, data);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return data;
    }

    public static  boolean appInstalledOrNot(String uri,Context cxt) {
        PackageManager pm = cxt.getPackageManager();
        boolean app_installed = false;
        try {
            pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
            app_installed = true;
        }
        catch (PackageManager.NameNotFoundException e) {
            app_installed = false;
        }
        return app_installed ;
    }


    public static List<PushMessage>  convertFlipOffers(FlipkartOffers offers)
    {
        List<PushMessage > listMessages = new ArrayList<PushMessage>();

        for ( FlipkartOffers.AllOffers temp : offers.dotdList)
        {

            //By default add 24 hrs for expiry of deal as these are daily offers
            long hrs = 24;
            //Flipkart returns Zero for end time.
             if(temp.endTime != 0)
                hrs = temp.endTime / (60*60*1000);
            String url = null;
            /**
             * Last one is the one with highest resolution.
             */
            if(temp.availability != null && temp.availability.compareTo("LIVE")==0)
            {
                if (temp.imageUrls != null && temp.imageUrls.size() > 0)
                    url = temp.imageUrls.get(temp.imageUrls.size() - 1).url;
                PushMessage pm = new PushMessage(temp.title, temp.description, hrs, url, "PROMO_FLIP", temp.url);
                listMessages.add(pm);
            }
             else
            {
                Log.i("AndroidUtils", "Deal is not currently live so ignore it");
            }
        }
        return listMessages;
    }


}
