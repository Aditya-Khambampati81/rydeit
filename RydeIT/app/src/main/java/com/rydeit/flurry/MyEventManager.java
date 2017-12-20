package com.rydeit.flurry;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;

import com.flurry.android.FlurryAgent;
import com.flurry.android.FlurryEventRecordStatus;
import com.parse.ParseUser;
import com.rydeit.BuildConfig;
import com.rydeit.RydeItApplication;
import com.rydeit.provider.LocationProvider;
import com.rydeit.util.AndroidUtils;
import com.rydeit.util.ConfigurationObject;
import com.rydeit.util.PreferenceUtils;
import com.rydeit.view.MapsActivity;

import java.util.HashMap;

/**
 * Created by Aditya.Khambampati on 11/29/2015.
 * Class encapusaltes basic params that are logged with every crash
 *
 */
public class MyEventManager {

    private static MyEventManager instance = new MyEventManager();
    private static String VersionInfo = null;

    private MyEventManager()
    {

    }


    public static MyEventManager getInstance()
    {
        return instance;

    }


    public HashMap<String, String> getCommonEventMap()  {
        HashMap <String,String> map = new HashMap<String,String>();
        //device model
        map.put("a", Integer.toString(Build.VERSION.SDK_INT));
        map.put("d", Build.MODEL);
        map.put("m", Build.MANUFACTURER);
        if(VersionInfo == null){
            try {
                PackageInfo pInfo = AndroidUtils.getAppContext().getPackageManager().getPackageInfo("com.rydeit", 0);
                VersionInfo = "Version " + RydeItApplication.VERSION_NAME + " Version code :" + pInfo.versionName;

            }
            catch (Exception ex)
            {
                ex.printStackTrace();
            }
        }
        map.put("v",VersionInfo );
        if(ConfigurationObject.captureanalytics )
        {
            //captur information only if user agreed to do so
            if(ParseUser.getCurrentUser()!= null)
                map.put("u", ParseUser.getCurrentUser().getUsername());
            else
                map.put("u","NA");
        }
        else
            map.put("u","NA");

        Location loc = LocationProvider.getInstance().getLastKnownLocation(AndroidUtils.getAppContext());
        map.put("loc", loc ==null? "NA":(loc.getLatitude() + " : " + loc.getLongitude()) );
        return map;
    }


    public FlurryEventRecordStatus logEventData(String eventName , HashMap<String,String> values)
    {
        values.putAll(getCommonEventMap());
        return FlurryAgent.logEvent(eventName, values);
    }

    /**
     * API to log timed event to flurry
     * @param eventName
     * @param values
     * @return
     */
    public FlurryEventRecordStatus logEventDataTimed(String eventName , HashMap<String,String> values)
    {
        values.putAll(getCommonEventMap());
        return FlurryAgent.logEvent(eventName, values,true);
    }

    /**
     *  API to end timed event
      */
     public void endTimedEvent(String eventName)
     {
         FlurryAgent.endTimedEvent(eventName);
     }


    public void logPromoLinkEvent(String event , String link)
    {

        HashMap<String,String> myMap = new HashMap<String,String>();
        myMap.putAll(getCommonEventMap());
        myMap.put("val", link);
        logEvent(event, myMap);
    }



    public void logUncaughtException(String event , String data, String trace)
    {

        HashMap<String,String> myMap = new HashMap<String,String>();
        myMap.putAll(getCommonEventMap());
        myMap.put("val", data);
        myMap.put("trc", trace);
        logEvent(event, myMap);
    }




    public void logCabData(String crn, String name, String mobile, String cabNumber,String cabtype, String providerType   )
    {
        HashMap<String,String> myMap = new HashMap<String,String>();
        myMap.putAll(getCommonEventMap());
        myMap.put("id",crn);
        myMap.put("d", name);
        myMap.put("n", mobile);
        myMap.put("c", cabNumber);
        myMap.put("t", cabtype);
        myMap.put("p", providerType);
        logEvent("cd", myMap);
    }


    public void loadBookingStatus(String event , String provider )
    {
        HashMap<String,String> myMap = new HashMap<String,String>();
        myMap.putAll(getCommonEventMap());
        myMap.put("p", provider);
        logEvent(event, myMap);
    }

    private void logEvent(String event, HashMap<String, String> myMap)
    {
        // Log analytics only if it is non debug build
        if(!BuildConfig.DEBUG)
            FlurryAgent.logEvent(event, myMap);
    }
}
