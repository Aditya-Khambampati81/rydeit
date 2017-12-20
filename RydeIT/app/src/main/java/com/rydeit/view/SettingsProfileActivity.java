package com.rydeit.view;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Message;

import com.rydeit.R;
import com.rydeit.uilibrary.BaseActivityActionBar;

import java.util.HashMap;

/**
 * Created by Aditya.Khambampati on 11/3/2015.
 */
public class SettingsProfileActivity extends BaseActivityActionBar {

    public static final String LAUNCH_TYPE= "launchtype";
    public static final int LAUNCH_SETTINGS= 1;
    public static final int LAUNCH_PROFILE = 2;
    public static final int LAUNCH_ABOUT =3;
    public static final int LAUNCH_OFFERS=4;




    @Override
    public void onCreate(Bundle savedInstanceState) {
        setHomeDisabled(true);
        super.onCreate(savedInstanceState);
        if(getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowHomeEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            setTitle(getString(R.string.settings));
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.actionbar_bg)));
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.left_arrow_white1);
        }
        setContentView(R.layout.activity_settings);
        connectAppropriateFragment();
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

    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        connectAppropriateFragment();
        super.onNewIntent(intent);
    }

    private void connectAppropriateFragment()
    {
        if (getIntent().hasExtra("launchtype"))
        {

            if (getIntent().getIntExtra(LAUNCH_TYPE, LAUNCH_SETTINGS) == LAUNCH_SETTINGS)
            {
                this.setTitle(R.string.title_activity_settings);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, SettingsFragment.getInstance(4))
                        .commit();


            }
            else  if (getIntent().getIntExtra(LAUNCH_TYPE, LAUNCH_SETTINGS) == LAUNCH_PROFILE)
            {
                this.setTitle(R.string.title_activity_profile);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, MyProfileFragment.getInstance(2))
                        .commit();

            }
            else if (getIntent().getIntExtra(LAUNCH_TYPE, LAUNCH_SETTINGS) == LAUNCH_OFFERS)
            {
                this.setTitle(R.string.title_activity_offers);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, OffersFragment.getInstance(1))
                        .commit();

            }
            else
            {
                this.setTitle(R.string.title_activity_about);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, AboutFragment.getInstance(3))
                        .commit();
            }

        }


    }
}
