package com.rydeit.view;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.parse.ParsePush;
import com.parse.ParseUser;
import com.rydeit.BuildConfig;
import com.rydeit.R;
import com.rydeit.api.uber.UberCallback;
import com.rydeit.database.DatabaseUtil;
import com.rydeit.model.common.MyBooking;
import com.rydeit.model.uber.Requests.UberStatus;
import com.rydeit.model.uber.RideRequest;
import com.rydeit.util.ConfigurationObject;
import com.rydeit.util.Log;

import org.json.JSONException;

import java.util.ArrayList;

import retrofit.client.Response;

public class MapsActivity extends RydeItBaseNavigationActivity
{



    private static final String TAG = MapsActivity.class.getSimpleName();

    private View mTestView = null;
    ArrayList<MyBooking> myBookings = null;





    @Override
    public void onCreate(Bundle savedInstanceState) {
        //getWindow().requestFeature(Window.FEATURE_PROGRESS);
        super.onCreate(savedInstanceState);
       // hideToolBarTitle();
        //setToolBarImage(R.drawable.title_image1);
        if (getIntent().hasExtra("openoffers") && getIntent().getBooleanExtra("openoffers",false))
        {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.finder_container, MapCabFinder.getInstance(1))
                    .commit();

            //START ACTIVITY to launch offers

            Intent  launchAbout = new Intent(this, SettingsProfileActivity.class);
            launchAbout.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            launchAbout.putExtra(SettingsProfileActivity.LAUNCH_TYPE,SettingsProfileActivity.LAUNCH_OFFERS);
            startActivity(launchAbout);


        }
        else
        {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.finder_container, MapCabFinder.getInstance(1))
                    .commit();

        }



        if(BuildConfig.DEBUG)
        {
            ParsePush.subscribeInBackground("TESTRIDE");
        }
        else
        {
            ParsePush.subscribeInBackground("INDIA");
        }


        if(!getIntent().hasExtra("open_offers"))
        {
            // find if there are any active rides if yes then take user to Myrides

            new Thread() {
                @Override
                public void run() {
                     myBookings =  DatabaseUtil.getAllActiveRides(MapsActivity.this.getApplicationContext());
                    if((myBookings == null ) || (myBookings != null && myBookings.size()==0))
                    {
                       // Toast.makeText(MapsActivity.this.getApplicationContext(), "There are no active rides !!", Toast.LENGTH_SHORT).show();
                        checkForAppUpdates();
                        return ;
                    }
                    //Handle active rides here ...
                    // we have some active bookings
                    if (myBookings.size() == 1)
                    {
                        if(mActivityHandler!=null)
                        mActivityHandler.sendMessage(mActivityHandler.obtainMessage(DIALOG_ACTIVE_RIDES));
                    }
                    else
                    {
                        if(mActivityHandler!=null)
                            mActivityHandler.sendMessage(mActivityHandler.obtainMessage(DIALOG_MULTIPLE_ACTIVE_RIDES));
                    }



                }
            }.start();

            //adBanner = com.tappx.TAPPXAdBanner.ConfigureAndShowAtBottom(this, adBanner, TAPPX_KEY);

        }

    }

    void checkForAppUpdates(){
        /*if(ConfigurationObject.playStoreLatestAppVersion!=null) {

            PackageInfo pInfo = null;
            try {
                pInfo = MapsActivity.this.getPackageManager().getPackageInfo(getPackageName(), 0);
                if(Float.parseFloat(pInfo.versionName)<Float.parseFloat(ConfigurationObject.playStoreLatestAppVersion)){
                    showAppDialog(DIALOG_PLAYSTORE_UPDATE_AVAILABLE);
                }
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            } catch (Exception e){
                e.printStackTrace();
            }

        }*/
        if(ConfigurationObject.playStoreLatestAppDetails!=null) {

            PackageInfo pInfo = null;
            try {
                pInfo = MapsActivity.this.getPackageManager().getPackageInfo(getPackageName(), 0);
                if(pInfo.versionCode<ConfigurationObject.playStoreLatestAppDetails.getInt("Current_version")){
                    showAppDialog(DIALOG_PLAYSTORE_UPDATE_AVAILABLE);
                }
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }


    /**
     *
     * UBER METHODS STARTS HERE
     *
     * */

    private UberCallback<RideRequest> callbackRideRequest = new UberCallback<RideRequest>()
    {
        @Override
        public void success(RideRequest rideRequest, Response response) {
            dismissProgressDialog(MapsActivity.this.getSupportFragmentManager());
            if(rideRequest==null){
               // Toast.makeText(MapsActivity.this, "RESPONSE: RideRequest is null", Toast.LENGTH_LONG).show();
                Log.i(TAG,"RIde request is null!!");
                return;
            }

            if(rideRequest!=null && rideRequest.getRequest_id()!=null) {

                if (rideRequest.getStatus().equalsIgnoreCase(UberStatus.COMPLETED.toString())) {
                    //Currently active ride is completed. ..



                }
                else
                    Log.i(TAG,"Request id is not valid.!!");

            }else{
                Log.i(TAG,"RIde request is null!!");
            }

        }

    };

    @Override
    public void processCustomMessage(Message msg) {

        switch(msg.what)
        {
            case DIALOG_ACTIVE_RIDES:
                showAppDialog(DIALOG_ACTIVE_RIDES);
                break;
            case DIALOG_MULTIPLE_ACTIVE_RIDES:
                showAppDialog(DIALOG_MULTIPLE_ACTIVE_RIDES);
            break;

        }

            super.processCustomMessage(msg);



    }
    @Override
    protected void onResume() {
        super.onResume();
        if (dialogIsRunning) {
            showAppDialog(ACTIVE_DIALOG_ID);
        }
        startService(new Intent(this, DrawTopService.class));
      //  com.tappx.TAPPXAdBanner.Resume(adBanner);
    }

    @Override
    protected void onPause() {
        super.onPause();
        DialogFragment df = (DialogFragment) getSupportFragmentManager().findFragmentByTag(DIALOG_TAG);
        if (df != null) {
            df.dismissAllowingStateLoss();
            dialogIsRunning = true;
        }
        stopService(new Intent(this, DrawTopService.class));
       // com.tappx.TAPPXAdBanner.Pause(adBanner);
    }

    @Override
    public void onPositiveButtonClicked(int reqCode) {
        super.onPositiveButtonClicked(reqCode);
        ACTIVE_DIALOG_ID = 0;
        dialogIsRunning = false;
        switch (reqCode) {
            case DIALOG_ACTIVE_RIDES:
                // There is single active ride , take user to tracking page
                Intent intent = new Intent(this, TrackMyRideActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("TrackRideInProgress", myBookings.get(0));
                intent.putExtra("isNewBooking", false);
                startActivity(intent);


                break;
            case DIALOG_MULTIPLE_ACTIVE_RIDES:
                //There are more than 1 active rides , take user to My rides screen
                startActivity(new Intent(this, HistoryActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                break;

            case DIALOG_PLAYSTORE_UPDATE_AVAILABLE:
                final String appPackageName = getPackageName(); // getPackageName() from Context or Activity object
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                } catch (android.content.ActivityNotFoundException anfe) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                }
                break;
            default:
                ;
        }

    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        setIntent(intent);
        if (getIntent().hasExtra("openoffers") && getIntent().getBooleanExtra("openoffers",false))
        {

            // open offers

            Intent  launchAbout = new Intent(this, SettingsProfileActivity.class);
            launchAbout.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            launchAbout.putExtra(SettingsProfileActivity.LAUNCH_TYPE,SettingsProfileActivity.LAUNCH_OFFERS);
            startActivity(launchAbout);

        }
        else
        {
          //Keep it as it was before ...

        }

    }

    @Override
    public void onNegativeButtonClicked(int reqCode) {
        super.onNegativeButtonClicked(reqCode);
        ACTIVE_DIALOG_ID = 0;
        dialogIsRunning = false;
        switch (reqCode) {
            case DIALOG_PLAYSTORE_UPDATE_AVAILABLE:

                try {
                    if(ConfigurationObject.playStoreLatestAppDetails!=null && ConfigurationObject.playStoreLatestAppDetails.getBoolean("IsForced")) {

                        PackageInfo pInfo = null;
                        try {
                            pInfo = MapsActivity.this.getPackageManager().getPackageInfo(getPackageName(), 0);
                            if(pInfo.versionCode<=ConfigurationObject.playStoreLatestAppDetails.getInt("forced_mini_version")){
                                finish();
                            }
                        } catch (PackageManager.NameNotFoundException e) {
                            e.printStackTrace();
                        } catch (Exception e){
                            e.printStackTrace();
                        }

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                break;
            default:
                ;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode)
        {
            case REQ_LOGIN:
                if (resultCode == RESULT_OK)
                {
                    updateTextStatus((String)ParseUser.getCurrentUser().get("name"), null, R.drawable.loging_selector ,true);
                }
                else
                    Toast.makeText(this, "Login cancelled!!", Toast.LENGTH_LONG).show();

                break;
            case MapCabFinder.PLACE_AUTOCOMPLETE_REQUEST_CODE:

                MapCabFinder.getInstance(1).onActivityResult(requestCode,resultCode,data);
                break;

        }


    }



    @Override
    public void onDestroy() {
        super.onDestroy();
       // com.tappx.TAPPXAdBanner.Destroy(adBanner);
//        if(BuildConfig.DEBUG)
//        {
//            ParsePush.unsubscribeInBackground("TEST");
//        }
//        else
//        {
//            ParsePush.unsubscribeInBackground("INDIA");
//        }
        //stopService(new Intent(this, DrawTopService.class));
        MapCabFinder.tearDown();
        if (mTestView != null) {
            WindowManager windowManager = (WindowManager) getBaseContext().getSystemService(Context.WINDOW_SERVICE);
            if (mTestView.isShown()) {
                windowManager.removeViewImmediate(mTestView);
            }
        }
    }


}