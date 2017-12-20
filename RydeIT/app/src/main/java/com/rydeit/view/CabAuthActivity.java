package com.rydeit.view;

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.rydeit.R;
import com.rydeit.uilibrary.BaseActivityActionBar;
import com.rydeit.util.AndroidUtils;
import com.rydeit.view.common.OlaAuthFragment;
import com.rydeit.view.common.UberAuthFragment;

import java.util.HashMap;

public class CabAuthActivity extends BaseActivityActionBar {

    LinearLayout pb = null;
    FrameLayout fl = null;
    private static final int MSG_DISMISS_PROGRESS = 112;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setHomeDisabled(true);
        super.onCreate(savedInstanceState);

        if(getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowHomeEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            setTitle(getString(R.string.authentication));
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.actionbar_bg)));
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.left_arrow_white1);
        }
        setContentView(R.layout.activity_cab_auth);

        pb = (LinearLayout) findViewById(R.id.progresslayout);
        fl = (FrameLayout) findViewById(R.id.container);
        showProgressBar();
        mActivityHandler.sendMessageDelayed(mActivityHandler.obtainMessage(MSG_DISMISS_PROGRESS),2500);

        int requestCode = getIntent().getExtras().getInt("requestCode");

        if (savedInstanceState == null) {

            if(requestCode==ConfirmBookingActivity.REQUEST_CODE_OLA)
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.container, OlaAuthFragment.getInstance())
                        .commit();
            else if(requestCode==ConfirmBookingActivity.REQUEST_CODE_UBER)
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.container, UberAuthFragment.getInstance(1))
                        .commit();
        }

        AndroidUtils.setNotificationBarColor(this,com.rydeit.uilibrary.R.color.notificationbar_bg);
    }


    public void hideProgressBar()
    {
        if(pb != null)
        {
            pb.setVisibility(View.GONE);

        }
        if(fl!= null)
        {
            fl.setVisibility(View.VISIBLE);
        }
    }

    public void showProgressBar()
    {
        if(pb != null)
        {
            pb.setVisibility(View.VISIBLE);


        }
        if(fl!= null)
        {
            fl.setVisibility(View.INVISIBLE);
        }
    }



    @Override
    public int getActionBarMenuId() {
        return INVALID_MENU;


    }

    @Override
    public HashMap<Integer, MenuItem> getMenuItems() {
        return null;
    }

    @Override
    public void onDialogTimedOut(int reqCode) {

    }

    @Override
    public void processCustomMessage(Message msg) {
        if(msg.what== MSG_DISMISS_PROGRESS)
        {
            hideProgressBar();
        }

    }

}
