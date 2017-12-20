package com.rydeit.view;

import android.os.Bundle;
import android.os.Message;

import com.rydeit.R;
import com.rydeit.uilibrary.BaseActivityActionBar;

import java.util.HashMap;

/**
 * Created by Aditya.Khambampati on 11/3/2015.
 */
public class HistoryActivity extends BaseActivityActionBar {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowHomeEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            setTitle(getString(R.string.myrides));
            //getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.splash_yellow)));
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.left_arrow_white1);
        }

        setContentView(R.layout.activity_history);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.history_container, HistoryFragment.getInstance(3))
                .commit();

    }

    @Override
    public int getActionBarMenuId() {
        return -1;
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
}
