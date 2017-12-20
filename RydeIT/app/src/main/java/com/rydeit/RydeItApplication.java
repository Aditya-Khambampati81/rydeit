package com.rydeit;

import android.app.Application;
import android.content.pm.PackageManager;
import android.widget.Toast;

import com.flurry.android.FlurryAgent;
import com.parse.ConfigCallback;
import com.parse.Parse;
import com.parse.ParseConfig;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.rydeit.flipkart.FlipkartApiCallback;
import com.rydeit.flipkart.FlipkartApiClient;
import com.rydeit.flipkart.FlipkartOffers;
import com.rydeit.provider.LocationProvider;
import com.rydeit.push.PushParseIntentService;
import com.rydeit.push.database.PushPojoManager;
import com.rydeit.push.model.PushMessage;
import com.rydeit.util.AndroidUtils;
import com.rydeit.util.ConfigurationObject;
import com.rydeit.util.Constants;
import com.rydeit.util.Log;
import com.rydeit.util.PreferenceUtils;

import java.util.List;

import retrofit.client.Response;

/**
 * Created by Aditya.Khambampati on 7/29/2015.
 */
public class RydeItApplication extends Application {
    public static final String VERSION_NAME = "1.1";
    private static final String MY_FLURRY_APIKEY = "RZPSWT7RDZWKMX7YZKSS";
    /**
     * This json is test code , each time application is opened one new message is added in database.
     * Server push is also working fine
     */


    @Override
    public void onCreate() {
        super.onCreate();
        /** storing one copy of context and initializing location provider**/
        AndroidUtils.setAppContext(this);
        LocationProvider.getInstance().init(this);
       /** initializing the parse sdk **/
        Parse.initialize(this, "s6byF3ZoMw6T2P5lfMGfKfGwB1vjUHgByURi1XJ8", "4tzpMN8t06gBXGfvTALZ4vtUTC81JS60ZUuJSriQ");
        ParseInstallation.getCurrentInstallation().saveInBackground();
        /** Intent service to clean obsolete messages **/
        PushParseIntentService.startCleanDb(this);

        //Pull only flipkart content once a day
        final long lastFetchedTime = PreferenceUtils.getInstance().getLastflipkartOffertime(AndroidUtils.getAppContext());
        if ((System.currentTimeMillis() - lastFetchedTime) > (ConfigurationObject.pollfrequency *60*60*1000)) {
            FlipkartApiClient.getfInterface().getDealOfTheDay(FlipkartApiClient.affiliateID, FlipkartApiClient.affiliateToken, callback);
        }
        /** Get parse configuration object from server **/
        ParseConfig currentConfig = ParseConfig.getCurrentConfig();
        if(currentConfig != null)
        {
            ConfigurationObject.captureanalytics = currentConfig.getBoolean(ConfigurationObject.ANALYTICS);
            ConfigurationObject.pollfrequency = (currentConfig.getNumber(ConfigurationObject.POLL_FREQ)== null ?
                    12:currentConfig.getNumber(ConfigurationObject.POLL_FREQ).intValue());
            ConfigurationObject.uberwithin = currentConfig.getBoolean(ConfigurationObject.UBER_WITHIN);
            ConfigurationObject.flippull = currentConfig.getBoolean(ConfigurationObject.FLIPCART_PULL);
            ConfigurationObject.playStoreLatestAppVersion=currentConfig.getString(ConfigurationObject.PLAYSTORE_APP_VERSION);
        }
        refreshConfig();

        /** Setting configuration parameters based on build flavour **/
        if (!BuildConfig.DEBUG) {
            Log.setLogging(false);
            Constants.SIMULATE_BOOKING=false;
            // configure Flurry
            FlurryAgent.setLogEnabled(false);
        }
        else {
         // configure Flurry
            FlurryAgent.setLogEnabled(true);
            Constants.SIMULATE_BOOKING= true;
            Log.setLogging(true);
        }
        // init Flurry
        FlurryAgent.init(this, MY_FLURRY_APIKEY);

    }


    private FlipkartApiCallback<FlipkartOffers> callback = new FlipkartApiCallback<FlipkartOffers>() {
        @Override
        public void success(final FlipkartOffers flipOffers, Response response) {

            //Add data to DB in a thread

            new Thread() {
                @Override
                public void run() {

                    PreferenceUtils.getInstance().updateFlipkartPullTime(AndroidUtils.getAppContext(), System.currentTimeMillis());

                    if (flipOffers != null && flipOffers.dotdList != null) {
                        //Add flip offers to DBc

                        List<PushMessage> list = AndroidUtils.convertFlipOffers(flipOffers);

                        try {
                            PushPojoManager.getInstance().insertBatch(AndroidUtils.getAppContext(), list);
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }

                    }
                }
            }.start();


        }
    };


//
//
//    private void calculateHashKey(String yourPackageName) {
//        try {
//            PackageInfo info = getPackageManager().getPackageInfo(
//                    yourPackageName,
//                    PackageManager.GET_SIGNATURES);
//            for (Signature signature : info.signatures) {
//                MessageDigest md = MessageDigest.getInstance("SHA");
//                md.update(signature.toByteArray());
//                Log.d("KeyHash:",
//                        "KeyHash: " +Base64.encodeToString(md.digest(), Base64.DEFAULT));
//            }
//        } catch (PackageManager.NameNotFoundException e) {
//            e.printStackTrace();
//        } catch (NoSuchAlgorithmException e) {
//            e.printStackTrace();
//        }
//    }

    // Fetches the config at most once every 12 hours per app runtime
    public  void refreshConfig() {
        long currentTime = System.currentTimeMillis();
        final long lastFetchedTime = PreferenceUtils.getInstance().getLastConfigTime(this);

        if ((currentTime - lastFetchedTime) > (ConfigurationObject.pollfrequency *60*60*1000)) {
            ParseConfig.getInBackground(new ConfigCallback() {
                @Override
                public void done(ParseConfig config, ParseException e) {

                    if(e== null) {

                        if(config != null)
                        {

                            ConfigurationObject.captureanalytics = config.getBoolean(ConfigurationObject.ANALYTICS);
                            ConfigurationObject.pollfrequency = (config.getNumber(ConfigurationObject.POLL_FREQ)== null ?
                                    12:config.getNumber(ConfigurationObject.POLL_FREQ).intValue());
                            ConfigurationObject.uberwithin = config.getBoolean(ConfigurationObject.UBER_WITHIN);
                            ConfigurationObject.flippull = config.getBoolean(ConfigurationObject.FLIPCART_PULL);
                            ConfigurationObject.playStoreLatestAppVersion=config.getString(ConfigurationObject.PLAYSTORE_APP_VERSION);
                            ConfigurationObject.playStoreLatestAppDetails=config.getJSONObject(ConfigurationObject.PLAYSTORE_APP_VERSION1);

                            if(Constants.ENABLE_DEBUG_TOAST)
                            Toast.makeText(RydeItApplication.this,"CApture analytics changed to :"+ ConfigurationObject.captureanalytics +"", Toast.LENGTH_LONG).show();
                            PreferenceUtils.getInstance().updateConfigFetchTime(RydeItApplication.this,System.currentTimeMillis());


                            // when ever we

                        }
                    }
                    else
                    {
                        Log.i("RydeitApplication", "There is exception loading parse configuration!!");
                    }
                }
            });
        }
    }
}
