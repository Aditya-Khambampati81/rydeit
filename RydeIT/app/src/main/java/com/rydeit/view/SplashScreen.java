package com.rydeit.view;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.rydeit.R;
import com.rydeit.database.DatabaseHelper;
import com.rydeit.provider.ILocationListener;
import com.rydeit.provider.LocationProvider;
import com.rydeit.util.AndroidUtils;


/**
 * Created by Aditya Khambampati
 */
public class SplashScreen extends Activity  implements ResultCallback<LocationSettingsResult>,ILocationListener{
    private static final String TAG= "SplashScreen";
    private MyHandler myHandler = null;
    private static final int MSG_PROCEED_FURTHER = 1020;
    private static final int MSG_FINISH = 1021;
    private final int REQUEST_CHECK_SETTINGS = 121;
    private final int REQUEST_CODE_RECOVER_PLAY_SERVICES = 122;
    private long  mListenerId = -1;

    ImageView splashscreen_image;
    TextView splashscreen_tagtext;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       // hideActionBar();

        myHandler = new MyHandler();
        setContentView(R.layout.activity_splash_screen);

        splashscreen_image=(ImageView)findViewById(R.id.splashscreen_image);
        splashscreen_tagtext=(TextView)findViewById(R.id.splashscreen_tagtext);

       // FacebookSdk.sdkInitialize(getApplicationContext());

        //begin
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(SplashScreen.this);
        Log.i(TAG, "#############Result code of playservices###################" + resultCode);
        // If Google Play services is available
        if (ConnectionResult.SUCCESS != resultCode) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {

                final DialogInterface.OnCancelListener cancelListener = new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(final DialogInterface dialog) {
                        Log.i(TAG, "#############Dialog cancelled ###################");
                        Message msg = myHandler.obtainMessage(MSG_FINISH);
                        myHandler.sendMessage(msg);
                    }
                };
                Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(resultCode, this, REQUEST_CODE_RECOVER_PLAY_SERVICES, cancelListener);
                Log.i(TAG, "#############Before showing error dialog ###################");
                // If Google Play services can provide an error dialog
                if (errorDialog != null) {
                    errorDialog.setCancelable(false);
                    errorDialog.setCanceledOnTouchOutside(false);
                    errorDialog.setOnCancelListener(cancelListener);

                    errorDialog.show();
                }
            } else {
                Toast.makeText(this,"Play services not avaiable", Toast.LENGTH_LONG).show();
            }
        }
        else
        {
            if(LocationProvider.getInstance().isConnected())
            {
                LocationProvider.getInstance().checkLocationSettings(this);
            }
            else
                mListenerId = LocationProvider.getInstance().registerLocationListener(this);
        }

        AndroidUtils.setNotificationBarColor(this, R.color.notificationbar_bg);
        //end

        //Load Database
        DatabaseHelper.getInstance(this).getWritableDatabase();

    }

    @Override
    protected void onResume() {
        super.onResume();
        showFadeInAnimation();

    }

    @Override
    public void onResult(LocationSettingsResult locationSettingsResult) {
        final Status status = locationSettingsResult.getStatus();
        switch (status.getStatusCode()) {
            case LocationSettingsStatusCodes.SUCCESS:
                Log.i(TAG, "All location settings are satisfied.");
                Message msg = myHandler.obtainMessage(MSG_PROCEED_FURTHER);
                myHandler.sendMessageDelayed(msg, 500);
                break;
            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                Log.i(TAG, "Location settings are not satisfied. Show the user a dialog to" +
                        "upgrade location settings ");
                try {
                    // Show the dialog by calling startResolutionForResult(), and check the result
                    // in onActivityResult().
                    status.startResolutionForResult(SplashScreen.this, REQUEST_CHECK_SETTINGS);
                } catch (IntentSender.SendIntentException e) {
                    Log.i(TAG, "PendingIntent unable to execute request.");
                }
                break;
            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                Log.i(TAG, "Location settings are inadequate, and cannot be fixed here. Dialog " +
                        "not created.");
                // Toast.makeText(this,"Location settings are inadequate, and cannot be fixed here",Toast.LENGTH_SHORT).show();
                Message msg1 = myHandler.obtainMessage(MSG_PROCEED_FURTHER);
                myHandler.sendMessageDelayed(msg1, 500);
                break;
        }
    }

    private void showFadeInAnimation(){

        Animation fadeIn = new AlphaAnimation(0, 1);
        fadeIn.setInterpolator(new DecelerateInterpolator());
        fadeIn.setDuration(2000);
        fadeIn.setStartOffset(500);

        AnimationSet animation = new AnimationSet(false);
        animation.addAnimation(fadeIn);
        splashscreen_image.setAnimation(animation);

        Animation fadeIntext = new AlphaAnimation(0, 1);
        fadeIntext.setInterpolator(new DecelerateInterpolator());
        fadeIntext.setDuration(3500);
        fadeIntext.setStartOffset(1500);

        AnimationSet animationtext = new AnimationSet(false);
        animationtext.addAnimation(fadeIntext);
        splashscreen_tagtext.setAnimation(animationtext);

    }

    @Override
    protected void onDestroy() {
        //LocationProvider.getInstance().unregisterLocationListener(mListenerId);
        super.onDestroy();
    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void connectionStaus(ConnectionStatus status) {
        if( status == ConnectionStatus.CONNECTED)
        {
            Log.i(TAG, "#############Checking location settings ###################");
            LocationProvider.getInstance().checkLocationSettings(this);

        }
        else
        {
            // Not able to connect to google client so finishing the activity
            Message msg = myHandler.obtainMessage(MSG_FINISH);
            myHandler.sendMessage(msg);
        }

    }

    @Override
    public void locationStatus(Status resolutionStatus) {

    }

    @Override
    public void onGPSStatusChange(int event) {

    }


    private class MyHandler extends Handler
    {
        /**
         * Subclasses must implement this to receive messages.
         */
        @Override
        public void handleMessage(Message msg) {

            switch (msg.what)
            {
                case  MSG_PROCEED_FURTHER:

                    /****** Create Thread that will sleep for 5 seconds *************/
                    Thread background = new Thread() {
                        public void run() {

                            try {
                                // Thread will sleep for 5 seconds
                                sleep(2500);

                                // After 5 seconds redirect to another intent
                                //Intent i=new Intent(getBaseContext(), LoginActivity.class);
                                Intent i=new Intent(getBaseContext(), MapsActivity.class);
                                SplashScreen.this.startActivity(i);

                                //Remove activity
                                SplashScreen.this.finish();

                            } catch (Exception e) {

                            }
                        }
                    };

                    // start thread
                    background.start();


                    break;
                    //FIXME: Break has been removed deliberately here .

                case MSG_FINISH:
                    SplashScreen.this.finish();
                    break;
                default:

            }
        }

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode) {
            case REQUEST_CHECK_SETTINGS:
                final LocationSettingsStates states = LocationSettingsStates.fromIntent(data);
                Log.i(TAG, "Activity result Cancelled !!");
                Message msg = myHandler.obtainMessage(MSG_PROCEED_FURTHER);
                myHandler.sendMessage(msg);

                break;
            case REQUEST_CODE_RECOVER_PLAY_SERVICES:
                if (resultCode == RESULT_CANCELED) {
                    Toast.makeText(this, "Google Play Services must be installed/ updated",
                            Toast.LENGTH_SHORT).show();
                    Message msg1 = myHandler.obtainMessage(MSG_FINISH);
                    myHandler.sendMessage(msg1);
                }

            default:


        }
    }




}
