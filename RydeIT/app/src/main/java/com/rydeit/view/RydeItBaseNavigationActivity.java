package com.rydeit.view;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.avast.android.dialogs.fragment.SimpleDialogFragment;
import com.google.android.gms.ads.AdListener;
import com.parse.LogOutCallback;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.rydeit.BuildConfig;
import com.rydeit.R;
import com.rydeit.push.PushParseIntentService;
import com.rydeit.uilibrary.BaseNavigationActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Aditya.Khambampati on 11/3/2015.
 */
public class RydeItBaseNavigationActivity extends BaseNavigationActivity {
    private static final String TAG= RydeItBaseNavigationActivity.class.getName();
    public static final int DIALOG_NO_CONNECTIVITY = 1001;
    public static final int DIALOG_LOGOUT = 1002;
    public static final int DIALOG_LOGOUT_SUCCESS = 1003;
    public static final int DIALOG_LOGOUT_FAILURE = 1004;
    public static final int DIALOG_ACTIVE_RIDES = 1007;
    public static final int DIALOG_MULTIPLE_ACTIVE_RIDES = 1008;
    public static final int REQ_LOGIN = 101;
    public static final int PROGRESS_DIALOG_LOGOUT  = 1005;
    public static final int LOGOUT_SANITY_CHECK = 1006;
    public static final int DIALOG_PLAYSTORE_UPDATE_AVAILABLE = 1009;
    public  int ACTIVE_DIALOG_ID = 0;
    public boolean dialogIsRunning = false;
    public final String TAPPX_KEY = "/120940746/Pub-8214-Android-0599";
    //private com.google.android.gms.ads.doubleclick.PublisherAdView adBanner = null;
    public com.google.android.gms.ads.doubleclick.PublisherInterstitialAd adInterstitial = null;


    @Override
    public List<DrawerObject> getDrawerContents() {
        List<BaseNavigationActivity.DrawerObject> doList = new ArrayList<DrawerObject>();
        if (doList.size() == 0) {

            doList.add(new BaseNavigationActivity.DrawerObject(R.drawable.dm_caricon, R.string.bookmyride, -1, R.string.rydeit, null));
            doList.add(new BaseNavigationActivity.DrawerObject(R.drawable.offers, R.string.title_activity_offers, -1, R.string.rydeit, null));

//            doList.add(new BaseNavigationActivity.DrawerObject(R.drawable.dm_profile, R.string.profile, -1, R.string.profile, null));
            doList.add(new BaseNavigationActivity.DrawerObject(R.drawable.dm_history, R.string.history, -1, R.string.history, null));
          //  doList.add(new BaseNavigationActivity.DrawerObject(R.drawable.dm_settings, R.string.settings, -1, R.string.settings, null));

            String emailBody = "Hi,\n";
            Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", "info@bsoftlabs.in", null));
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Feedback from :" + (ParseUser.getCurrentUser() == null ? "Rydeit User" : ParseUser.getCurrentUser().getUsername()));
            emailIntent.putExtra(Intent.EXTRA_TEXT, emailBody);
            doList.add(new BaseNavigationActivity.DrawerObject(android.R.drawable.ic_menu_edit, R.string.feedback, -1, R.string.feedback, emailIntent));

            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);
            //FIXME : later change it to google playstore url.
            shareIntent.putExtra(Intent.EXTRA_TEXT, "Try Rydeit, Easy way to book OLA/UBER cabs " + Uri.parse("https://play.google.com/store/apps/details?id=com.rydeit"));
            shareIntent.setType("text/plain");
            doList.add(new BaseNavigationActivity.DrawerObject(android.R.drawable.ic_menu_share, R.string.share, -1, R.string.share, shareIntent));
            doList.add(new BaseNavigationActivity.DrawerObject(R.drawable.ic_about, R.string.about, -1, R.string.about, null));
            if(BuildConfig.DEBUG)
            {
                //Add test code to dummy push
                doList.add(new BaseNavigationActivity.DrawerObject(android.R.drawable.ic_menu_share, R.string.simulate, -1, R.string.simulate, null));

            }



        }
        return doList;
    }



    @Override
    protected void onResume() {
        if(ParseUser.getCurrentUser()!=null)
        updateTextStatus((String)ParseUser.getCurrentUser().get("name"), null, R.drawable.loging_selector ,true);
        super.onResume();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getSupportActionBar()!= null) {
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
           // getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.actionbar_bg)));
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ham);

        }
        setNotificationBarColor(R.color.notificationbar_bg);
        if(!isNetworkConnectionAvailable())
        {
            showAppDialog(DIALOG_NO_CONNECTIVITY);
        }
        if (ParseUser.getCurrentUser()!=null && ParseUser.getCurrentUser().getUsername()!=null)
            updateTextStatus((String)ParseUser.getCurrentUser().get("name"), null, R.drawable.loging_selector, true);
        hideToolBarTitle();
    }

    @Override
    public void onDrawerItemSelected(int position) {

       Log.i(TAG, "onDrawerItem called for Finder activity @ Position" + position);
        switch (position) {
            case 0:
                Intent  i =new Intent(this,MapsActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
                break;

            case 1:
                Intent  launchDeals = new Intent(this, SettingsProfileActivity.class);
                launchDeals.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                launchDeals.putExtra(SettingsProfileActivity.LAUNCH_TYPE,SettingsProfileActivity.LAUNCH_OFFERS);
                startActivity(launchDeals);

                break;

//            case 2:
//                //FIXME choose list view or maps view based on state saved last time.?
//                //MY PROFILE
//                Intent  launchProfile = new Intent(this, SettingsProfileActivity.class);
//                launchProfile.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                launchProfile.putExtra(SettingsProfileActivity.LAUNCH_TYPE,SettingsProfileActivity.LAUNCH_PROFILE);
//                startActivity(launchProfile);
//
//                break;
            case 2:

                //History
                startActivity(new Intent(this, HistoryActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                break;
            case 5:

                Intent  launchAbout = new Intent(this, SettingsProfileActivity.class);
                launchAbout.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                launchAbout.putExtra(SettingsProfileActivity.LAUNCH_TYPE,SettingsProfileActivity.LAUNCH_ABOUT);
                startActivity(launchAbout);

                break;
            case LAUNCH_LOGIN:

//                    //MY PROFILE
                    Intent  launchProfileAct = new Intent(this, SettingsProfileActivity.class);
                    launchProfileAct.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    launchProfileAct.putExtra(SettingsProfileActivity.LAUNCH_TYPE,SettingsProfileActivity.LAUNCH_PROFILE);
                    startActivity(launchProfileAct);
                break;
            case 6:
                //Simulate push
                String myJson ="{\"expiry\":\"24\",\"img\":\"https://www.dropbox.com/s/hiayozbloxni9sh/beanbag.jpg?dl=0\",\"link\":\"http://tinyurl.com/oac5rrs\",\"ptxt\":\"NEARBUY DEALS\",\"stxt\":\"Use Coupon : BEANBAG and get additional 50% OFF on all bean bags\",\"type\":\"PRM\"}";

                //String myJson = "{\"expiry\":\"48\",\"img\":\"https://www.dropbox.com/s/26ha8yhm0exax0k/laptop-deal.jpg?dl=0\",\"link\":\"http: //www.flipkart.com/computers/laptops?sid=6bo%2Cb5g\u0026affid=reachryde\",\"ptxt\":\"Blockbuster Sale\",\"stxt\":\"Biggest sale in flipkart lets not miss this december 25th, we have special offers for you \",\"type\":\"PRM\"}";
                PushParseIntentService.startParseIntentService(this, myJson);
                break;
            default :
                // launch intent in hamburger items
                if (getDrawerContents()!= null && getDrawerContents().size() >= position)
                {
                    if(getDrawerContents().get(position).actionIntent!= null){
                        startActivity(getDrawerContents().get(position).actionIntent);
                    }
                }

        }


    }
    @Override
    public void onPositiveButtonClicked(int reqCode) {

        switch(reqCode)
        {
            case DIALOG_LOGOUT :
                Log.d("RydeitActivity","Postivit button clicked !!"+ RydeItBaseNavigationActivity.this.hashCode());
                showProgressDialog(PROGRESS_DIALOG_LOGOUT, this, this.getSupportFragmentManager(),
                        "Logout", "Logging user out....", true);
                ParseUser.logOutInBackground(new LogOutCallback() {
                    @Override
                    public void done(ParseException e) {
                        Log.d("RydeitActivity","Inside logout callback"+ RydeItBaseNavigationActivity.this.hashCode());
                        mActivityHandler.sendMessage(mActivityHandler.obtainMessage(LOGOUT_SANITY_CHECK));
                    }
                });
                break;



        }


        super.onPositiveButtonClicked(reqCode);
    }

    @Override
    public void onNegativeButtonClicked(int reqCode) {
        super.onNegativeButtonClicked(reqCode);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //This is nolonger valid

//        switch (requestCode)
//        {
//            case REQ_LOGIN:
//                if (resultCode == RESULT_OK)
//                {
//                    updateTextStatus((String)ParseUser.getCurrentUser().get("name"), null, R.drawable.loging_selector ,true);
//                }
//                else
//                    Toast.makeText(this, "Login failed!!", Toast.LENGTH_LONG).show();
//
//                break;
//
//        }


    }

    /**
     * Public api to show different dialogs
     * @param id
     */
    public void showAppDialog(int id) {

        SimpleDialogFragment.SimpleDialogBuilder builder = SimpleDialogFragment.createBuilder(
                this, getSupportFragmentManager());
        dismissProgressDialog(getSupportFragmentManager());
        switch (id) {

            case DIALOG_NO_CONNECTIVITY:
                builder.setTitle("No Netowork");
                builder.setMessage("You are not connected to internet");
                builder.setPositiveButtonText("OK");
                builder.setCancelableOnTouchOutside(false);
                break;
            case DIALOG_LOGOUT:
                builder.setTitle("Logout");
                builder.setMessage("Are you sure you want to logout!!");
                builder.setPositiveButtonText("OK");
                builder.setNegativeButtonText("CANCEL");
                builder.setRequestCode(DIALOG_LOGOUT);
                builder.setCancelableOnTouchOutside(false);
                break;

            case DIALOG_LOGOUT_SUCCESS:
                builder.setTitle("Logout");
                builder.setMessage("You are successfully logged out!!");
                builder.setPositiveButtonText("OK");
                builder.setCancelable(false);
                break;

            case DIALOG_LOGOUT_FAILURE:
                builder.setTitle("Logout");
                builder.setMessage("Failed to logout");
                builder.setPositiveButtonText("OK");
                break;
            case DIALOG_ACTIVE_RIDES:
                builder.setTitle("Active Rides");
                builder.setMessage("Take me to active rides");
                builder.setPositiveButtonText("OK");
                builder.setNegativeButtonText("CANCEL");
                break;
            case DIALOG_MULTIPLE_ACTIVE_RIDES:
                builder.setTitle("Active Rides");
                builder.setMessage("Few of your previous rides are active!! would you like to revisit History.");
                builder.setPositiveButtonText("OK");
                builder.setNegativeButtonText("CANCEL");
                break;

            case DIALOG_PLAYSTORE_UPDATE_AVAILABLE:
                builder.setTitle("Update Available");
                builder.setMessage("Latest version of app is available. \nUpdate the app for awesome experiences.");
                builder.setPositiveButtonText("OK");
                builder.setNegativeButtonText("CANCEL");
                break;

            default:
                Log.e(TAG, "Dialog id not found");
                return;
        }
        if (mIsRunning) {
            builder.setRequestCode(id).setTag(DIALOG_TAG).show();
            ACTIVE_DIALOG_ID = id;


        }
        else
            Log.e(TAG,"Not able to show dialog here :-( with id "+id);


    }


    @Override
    public void onDialogTimedOut(int reqCode) {
        if (reqCode == PROGRESS_DIALOG_LOGOUT)
        {
            dismissProgressDialog(getSupportFragmentManager());
            Toast.makeText(this, "Logging out failed !!", Toast.LENGTH_LONG).show();

        }
    }

    @Override
    public void processCustomMessage(Message msg) {
        // mActivity handler is defined in base class, you can reuse the public member and
        // control will come here once u post message to handler.

       if(msg.what == LOGOUT_SANITY_CHECK) {
           Log.d("RydeitActivity","Inside sanity checkk"+ RydeItBaseNavigationActivity.this.hashCode());
           Toast.makeText(this, "User successfully logged out !!", Toast.LENGTH_LONG).show();
           dismissProgressDialog(getSupportFragmentManager());
           updateTextStatus(getResources().getString(R.string.login_hint), null, R.drawable.loging_selector, false);
       }


    }

    @Override
    public void onBackPressed() {

        if(mNavigationDrawerFragment != null && mNavigationDrawerFragment.isDrawerOpen())
        {
            mNavigationDrawerFragment.closeDrawer();

        }
        else {
            if (RydeItBaseNavigationActivity.this instanceof  MapsActivity) {
                adInterstitial = com.tappx.TAPPXAdInterstitial.Configure(this, TAPPX_KEY,
                        new AdListener() {
                            @Override
                            public void onAdLoaded() {
                                com.tappx.TAPPXAdInterstitial.Show(adInterstitial);
                            }
                        });
            }
            super.onBackPressed();
        }
    }

    // added as an instance method to an Activity
    public boolean isNetworkConnectionAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        if (info == null) return false;
        NetworkInfo.State network = info.getState();
        return (network == NetworkInfo.State.CONNECTED || network == NetworkInfo.State.CONNECTING);
    }

}
