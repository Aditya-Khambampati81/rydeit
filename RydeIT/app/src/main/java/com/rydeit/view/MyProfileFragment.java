package com.rydeit.view;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.toolbox.NetworkImageView;

import com.avast.android.dialogs.fragment.SimpleDialogFragment;
import com.avast.android.dialogs.iface.ISimpleDialogListener;
import com.parse.LogOutCallback;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.rydeit.R;
import com.rydeit.uilibrary.BaseFragment;

import com.rydeit.util.AndroidUtils;
import com.rydeit.view.common.UberAuthFragment;
import com.rydeit.volley.VolleySingleton;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * A simple {@link android.app.Fragment} subclass.
 */
public class MyProfileFragment extends BaseFragment implements ISimpleDialogListener, View.OnClickListener{

    private View mRootView;
    TextView Name;
    TextView Email;
    TextView logout;
    NetworkImageView ProfileImage;
    public static final int DIALOG_NO_CONNECTIVITY = 1021;
    public static final int DIALOG_LOGOUT = 1022;
    public static final int DIALOG_LOGOUT_SUCCESS = 1023;
    public static final int DIALOG_LOGOUT_FAILURE = 1024;
    public static final int PROGRESS_DIALOG_LOGOUT  = 1025;
    public static final int LOGOUT_SANITY_CHECK = 1026;
    private static final String DIALOG_TAG = "MYTAG-PROFILE";

    public static final String ARG_SECTION_NUMBER = "section_number";
    private static MyProfileFragment mMyProfile;
    public MyProfileFragment() {
        // Required empty public constructor
    }
    public static MyProfileFragment getInstance(int sectionNumber) {

        if(mMyProfile == null ) {
            mMyProfile = new MyProfileFragment();
            mMyProfile.setRetainInstance(true);
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            mMyProfile.setArguments(args);
        }

        return mMyProfile;
    }


    @Override
    public void onAttach(Activity activity) {

        super.onAttach(activity);


    }
    public static void tearDown()
    {
        mMyProfile = null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mRootView = inflater.inflate(R.layout.fragment_profile_layout, container, false);

        LinearLayout lOla = (LinearLayout) mRootView.findViewById(R.id.olacash);
        LinearLayout lpaytm = (LinearLayout) mRootView.findViewById(R.id.paytm);
        lOla.setOnClickListener(this);
        lpaytm.setOnClickListener(this);
        return mRootView;

    }


    private static final String TAG = MyProfileFragment.class.getSimpleName();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }



    @Override
    public void onResume() {
        super.onResume();

        initViews();

        getUserDetails();
    }

    void initViews() {
        Name=(TextView)getActivity().findViewById(R.id.name);
        Email=(TextView)getActivity().findViewById(R.id.email);
        logout=(TextView)getActivity().findViewById(R.id.promocode);
        ProfileImage=(NetworkImageView)getActivity().findViewById(R.id.profileimage);

        LinearLayout ll = (LinearLayout) getActivity().findViewById(R.id.parent_layout);



//            if(ParseUser.getCurrentUser().getString("fbid")!=null) {
//                try {
//                    URL imgUrl = new URL("http://graph.facebook.com/" + ParseUser.getCurrentUser().getString("fbid") + "/picture?type=large");
//                    ProfileImage.setImageUrl(imgUrl.toString(), VolleySingleton.getInstance().getImageLoader());
//
//
//                }
//                catch ( MalformedURLException ex )
//                {
//                    ex.printStackTrace();
//                }
//            }
            ll.setVisibility(View.VISIBLE);
//            Name.setText("Name   : " + ParseUser.getCurrentUser().get("name"));
//            if(ParseUser.getCurrentUser().getEmail() != null) {
//                Email.setText("Email  : " + ParseUser.getCurrentUser().getEmail());
//                Email.setVisibility(View.VISIBLE);
//            }
//            else
//              Email.setVisibility(View.INVISIBLE);
            logout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if(!isNetworkConnectionAvailable())
                    {
                        showDialog(DIALOG_NO_CONNECTIVITY);
                    }
                    else
                        showDialog(DIALOG_LOGOUT);

                }
            });






    }



    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onDialogTimedOut(int reqCode) {

        //For any progress dialog if timeout happens then control will come from base class
        // you can show dialog here.

    }

    @Override
    public void processCustomMessage(Message msg) {


        if(msg.what == LOGOUT_SANITY_CHECK) {
            Toast.makeText(this.getActivity(), "User successfully logged out !!", Toast.LENGTH_LONG).show();
            dismissProgressDialog(getActivity().getSupportFragmentManager());
            showDialog(DIALOG_LOGOUT_SUCCESS);
            logout.setText("LogIn");
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void getUserDetails(){


    }

    private String getAccessToken() {
        return UberAuthFragment.getTokenType() + " " + UberAuthFragment.getAccessToken();
    }

    @Override
    public void onNeutralButtonClicked(int requestCode) {

    }



    @Override
    public void onPositiveButtonClicked(int reqCode) {
        switch(reqCode)
        {
            case DIALOG_LOGOUT :
                showProgressDialog(PROGRESS_DIALOG_LOGOUT, this.getActivity(),this,this.getActivity().getSupportFragmentManager(),"Logout", "Logging user out....", true);

                ParseUser.logOutInBackground(new LogOutCallback() {
                    @Override
                    public void done(ParseException e) {
                        if(e==null) {
                            Log.d("Profile Fragment!!", "Inside logout callback" + this.hashCode());
                            mHandler.sendMessage(mHandler.obtainMessage(LOGOUT_SANITY_CHECK));
                        }
                        else
                        {
                            showDialog(DIALOG_LOGOUT_FAILURE);
                        }
                    }
                });
                break;
            case DIALOG_LOGOUT_SUCCESS:
//
//                ParseLoginBuilder builder = new ParseLoginBuilder(MyProfileFragment.this.getActivity());
//                //builder.setTwitterLoginEnabled(true);
//                builder.setFacebookLoginEnabled(true);
//                startActivity(builder.build());
                MyProfileFragment.this.getActivity().finish();
                break;


        }


    }

    @Override
    public void onNegativeButtonClicked(int i) {

    }

    /**
     * Public api to show different dialogs
     * @param id
     */
    public void showDialog(int id) {

        SimpleDialogFragment.SimpleDialogBuilder builder = SimpleDialogFragment.createBuilder(this.getActivity()
               , this.getActivity().getSupportFragmentManager());
        dismissProgressDialog(this.getActivity().getSupportFragmentManager());
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
                builder.setMessage("Failed to logout!!");
                builder.setPositiveButtonText("OK");
                break;

            default:
                Log.e(TAG, "Dialog id not found");
                return;
        }
        if (mIsRunning) {

            builder.setRequestCode(id).setTag(DIALOG_TAG).show();
        }
        else
            Log.e(TAG,"Not able to show dialog here :-( with id "+id);


    }

    // added as an instance method to an Activity
    public boolean isNetworkConnectionAvailable() {
        ConnectivityManager cm = (ConnectivityManager) this.getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        if (info == null) return false;
        NetworkInfo.State network = info.getState();
        return (network == NetworkInfo.State.CONNECTED || network == NetworkInfo.State.CONNECTING);
    }

    @Override
    public void onClick(View v) {

            switch (v.getId())
            {
                case R.id.olacash:
                    if(AndroidUtils.appInstalledOrNot("com.olacabs.olamoney", getActivity().getApplicationContext()))
                    {
                        PackageManager pm = getActivity().getPackageManager();
                        Intent intent=pm.getLaunchIntentForPackage("com.olacabs.olamoney");
                        startActivity(intent);
                    }
                    else
                    {
                        // open market to download OLA money
                        final String appPackageName = "com.olacabs.olamoney"; // getPackageName() from Context or Activity object
                        try {
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                        } catch (android.content.ActivityNotFoundException anfe) {
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                        }
                    }

                    break;
                case R.id.paytm:
                    if(AndroidUtils.appInstalledOrNot("net.one97.paytm", getActivity().getApplicationContext()))
                    {
                        PackageManager pm = getActivity().getPackageManager();
                        Intent intent=pm.getLaunchIntentForPackage("net.one97.paytm");
                        startActivity(intent);
                    }
                    else
                    {
                        //Open market link to download PAytm.
                        // open market to download OLA money
                        final String appPackageName = "net.one97.paytm"; // getPackageName() from Context or Activity object
                        try {
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                        } catch (android.content.ActivityNotFoundException anfe) {
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                        }
                    }
                    break;
            }
        }


}
