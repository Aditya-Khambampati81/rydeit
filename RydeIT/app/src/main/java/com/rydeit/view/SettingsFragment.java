package com.rydeit.view;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Environment;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.rydeit.BuildConfig;
import com.rydeit.R;
import com.rydeit.RydeItApplication;
import com.rydeit.uilibrary.BaseActivityActionBar;
import com.rydeit.uilibrary.BaseFragment;

import java.io.File;
import java.io.IOException;


public class SettingsFragment extends BaseFragment {
    private View mRootView;
    private static final int MSG_CAPTURE_LOG = 1001;
    public static final String ARG_SECTION_NUMBER = "section_number";
    private static SettingsFragment mSettings;
    public SettingsFragment() {
        // Required empty public constructor
    }
    public static SettingsFragment getInstance(int sectionNumber) {

        if(mSettings == null ) {
            mSettings = new SettingsFragment();
            mSettings.setRetainInstance(true);
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            mSettings.setArguments(args);
        }

        return mSettings;
    }


    @Override
    public void onAttach(Activity activity) {

        super.onAttach(activity);
        // This will update the title in navigation bar.
        if(activity instanceof  MapsActivity) {
            ((MapsActivity)activity).onSectionAttached(
                    getArguments().getInt(ARG_SECTION_NUMBER));
        }

    }
    public static void tearDown()
    {
        mSettings = null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mRootView = inflater.inflate(R.layout.fragment_settings, container, false);

        TextView sendLogs = (TextView)mRootView.findViewById(R.id.bSendLogs);
        TextView border = (TextView) mRootView.findViewById(R.id.border);

        if (BuildConfig.DEBUG) {
            sendLogs.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SendLogcatMail();
                }
            });
        }
        else
        {
            sendLogs.setVisibility(View.INVISIBLE);
            border.setVisibility(View.INVISIBLE);
        }

        TextView mOsl = (TextView) mRootView.findViewById(R.id.bosl);
        mOsl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(getActivity(), WebviewActivity.class);
                startActivity(intent);
            }
        });

        return mRootView;

    }
    public void SendLogcatMail(){

        // save logcat in file
        File outputFile = new File(Environment.getExternalStorageDirectory(),
                System.currentTimeMillis() +"logcat.txt");
        try {
            Runtime.getRuntime().exec(
                    "logcat -f " + outputFile.getAbsolutePath());

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        Message msg = mHandler.obtainMessage(MSG_CAPTURE_LOG);
        Bundle bundle = new Bundle();
        bundle.putString("fileuri",outputFile.getName());
        msg.setData(bundle);
        mHandler.sendMessageDelayed(msg, 4000);

        showProgressDialog(123, (BaseActivityActionBar) getActivity(), this, getActivity().getSupportFragmentManager(), "Capturing Logs", "Please wait while we are collecting logs", false);
    }

    private static final String TAG = MyProfileFragment.class.getSimpleName();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }



    @Override
    public void onResume() {
        super.onResume();
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

        // IN base activity there is  public DialogHandlerActivity mActivityHandler = null;
        // instead of creating a handler you can reuse it and message that cannot be handled in base class will come here.
        switch (msg.what) {
            case MSG_CAPTURE_LOG:
                if (msg.getData() != null) {
                    Bundle b = msg.getData();
                    String fileName = b.getString("fileuri");
                    // the mail subject
                    String emailBody = getFeedbackEmailBody();
                    Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                            "mailto", "info@bsoftlabs.in", null));
                    emailIntent.putExtra(Intent.EXTRA_SUBJECT, "[Test logs]");
                    emailIntent.putExtra(Intent.EXTRA_TEXT, emailBody);
                    // the attachment
                    Uri uris = Uri.fromFile(new File(Environment.getExternalStorageDirectory(), fileName));
                    emailIntent.putExtra(Intent.EXTRA_STREAM, uris);

                    getActivity().startActivity(Intent.createChooser(emailIntent, "Send email..."));

                }
                dismissProgressDialog(getFragmentManager());
                break;

        }
    }

    private String getFeedbackEmailBody() {
        String info ="";
        try {
            PackageInfo pInfo = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0);
            info="--Support Info--\n\nAppVersion: "+ RydeItApplication.VERSION_NAME+"\n" ;
        }catch (Exception e){
           e.printStackTrace();
        }

        info = info+"DeviceName: "+ Build.BRAND+"\nDeviceModel: "+Build.MODEL+"\nOsType: Android"+"\nOsVersion: "+Build.VERSION.RELEASE + "\n--Support Info--" +"\n\n";

        return info;
    }



    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }
}
