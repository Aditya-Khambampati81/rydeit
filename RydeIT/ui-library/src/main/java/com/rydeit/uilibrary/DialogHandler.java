package com.rydeit.uilibrary;

/**
 * Created by Aditya.Khambampati on 2/18/2015.
 */

import android.os.Looper;
import android.os.Message;
import android.support.v4.app.FragmentManager;
import android.util.Log;


import com.rydeit.uilibrary.dialog.MyProgressDialog;

import java.lang.ref.WeakReference;

/**
 * Default Handler class to handle progress dialog time out/dismiss.
 * Classes can extends this to provide additional functionality.
 */
public class DialogHandler extends MyAbstractPauseHandler {
    private WeakReference<BaseFragment> mFragment;
    private WeakReference<FragmentManager> mFragmentManager;

    /**
     * Recommended constructor
     * @param l
     */
    DialogHandler(Looper l)
    {
        super(l);
    }

    @Override
    protected void processMessage(Message message) {

        if (mFragment == null || mFragmentManager == null)
        {
            Log.e(DialogHandler.class.toString(), "DialogHandler : Not initialized properly");
            return;
        }
        BaseFragment fragment = mFragment.get();
        FragmentManager manager = mFragmentManager.get();
        if(BaseActivityActionBar.MSG_TIME_OUT == message.what){
            if(fragment.mIsRunning) {
                MyProgressDialog.dismissProgressDialog(manager);
                fragment.onDialogTimedOut(message.arg1);
            }
            else
            {
                Log.e(DialogHandler.class.toString(), "mIsRunning is false!!");
            }
        }else if(BaseActivityActionBar.MSG_DISMISS_WORKING == message.what){
            if(fragment.mIsRunning){
                MyProgressDialog.dismissProgressDialog(manager);
            }
            else
            {
                Log.e(DialogHandler.class.toString(),"mIsRunning is false!!");
            }
        }
        else{
            if(mFragment.get()!=null)
                mFragment.get().processCustomMessage(message);
            else
                Log.e(BaseFragment.class.toString(), "Weird error fragment instance is null");
            return;
        }
    }

    public void setFragment(BaseFragment fragment){

        mFragment = new WeakReference<BaseFragment>(fragment);
    }

    public void setFragmentManager(FragmentManager fragmentManager){
        mFragmentManager = new WeakReference<FragmentManager>(fragmentManager);
    }
}
