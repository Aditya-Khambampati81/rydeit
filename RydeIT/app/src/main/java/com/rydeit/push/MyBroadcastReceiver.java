package com.rydeit.push;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;


import com.parse.ParsePushBroadcastReceiver;
import com.rydeit.R;
import com.rydeit.push.model.PushMessage;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;
import java.util.Random;

/**
 * Created by Aditya.Khambampati on 10/30/2015.  This is the broadcast receiver that gets invoked
 *
 */
public class MyBroadcastReceiver extends ParsePushBroadcastReceiver{


    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        String intentAction = intent.getAction();
        switch (intentAction) {
            //FIXME : see if there is need to distinguish these actions
            case ACTION_PUSH_RECEIVE:

                JSONObject pushData = null;
                try {
                    pushData = new JSONObject(intent.getStringExtra(KEY_PUSH_DATA));
                } catch (JSONException e) {
                    Log.e("MyBroadcastReceiver", "Unexpected JSONException when receiving push data: ", e);
                }
                Toast.makeText(context, pushData.toString(),Toast.LENGTH_LONG).show();

                // Pick an id that probably won't overlap anything

                // start service to parse the offers ?
             if(pushData != null)
                PushParseIntentService.startParseIntentService(context, pushData.toString());

        }


    }


}
