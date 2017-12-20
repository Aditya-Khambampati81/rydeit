package com.rydeit.view;

import android.graphics.drawable.ColorDrawable;
import android.os.Message;
import android.os.Bundle;

import com.rydeit.R;
import com.rydeit.uilibrary.BaseActivityActionBar;

import java.util.HashMap;

public class WebviewActivity extends BaseActivityActionBar {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setHomeDisabled(true);
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayShowHomeEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.actionbar_bg)));
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.left_arrow_white1);
        setContentView(R.layout.activity_webview);
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
}
