package com.rydeit.view;


import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;

import com.rydeit.util.Log;

import java.lang.ref.WeakReference;

/**
 * Created by Aditya Khambampati
 */
public class MyObserver extends ContentObserver {

    //FIXME: Change to weakref or make outer handler static
    public static final int MSG_RELOAD_UI = 1212;
    WeakReference<Handler> ref = null;

    /**
     * Creates a content observer.
     *
     * @param handler The handler to run {@link #onChange} on, or null if none.
     */
    public MyObserver(Handler handler) {
        super(handler);
        ref = new WeakReference<Handler>(handler);
    }


    @Override
    public void onChange(boolean selfChange) {
        Log.i("TAG", "onChange single param  called inside observer");

        this.onChange(selfChange, null);

    }

    @Override
    public void onChange(boolean selfChange, Uri uri) {
        Log.i("TAG", "onChange called inside observer");
        if(ref.get()!=null) {
            Message msf = ref.get().obtainMessage(MSG_RELOAD_UI);
            //THis handler is from fragment ;
            ref.get().sendMessage(msf);
        }
    }
}