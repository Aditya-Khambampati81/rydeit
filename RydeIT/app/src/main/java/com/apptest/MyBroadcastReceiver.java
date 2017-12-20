package com.apptest;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class MyBroadcastReceiver extends BroadcastReceiver {
    public MyBroadcastReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null) {
            if(intent.getAction().equals("com.android.vending.INSTALL_REFERRER")) {
                //Tappx Track Install
                try {
//                    com.tappx.TrackInstall tappx = new com.tappx.TrackInstall();
//                    tappx.onReceive(context, intent);
                } catch (Exception p_ex) {
                    p_ex.printStackTrace();
                }
                //Other tracking sdk/networks
            }
        }
    }
}
