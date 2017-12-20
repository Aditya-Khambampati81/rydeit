package com.rydeit.uilibrary;

import android.app.Activity;

import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBarActivity;

import com.rydeit.uilibrary.dialog.MyProgressDialog;


/**
 * Base Fragment class.
 */
public abstract class BaseFragment extends Fragment {


    /**
     * Time constant for progress dialog time outs.
     * After 90 seconds, the ongoing progress dialog shall be dismissed.
     */
    public static long TIMEOUT_TIME = 90000;

    /**
     * Default Handler object.
     * Derived classes can send messages for Dialog timeout or dismissing dialog.
     */
    public DialogHandler mHandler = null;

    /**
     *     Flag to indicate if activity is running.
     */
    public boolean mIsRunning = false;

    public BaseFragment() {
        // Required empty public constructor
    }


    @Override
    public void onPause() {
        if (mHandler != null) {
            mHandler.pause();
        }
        super.onPause();
    }


    @Override
    public void onResume() {
        if (mHandler != null) {
            mHandler.resume();
        }

        super.onResume();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mHandler= new DialogHandler(getActivity().getMainLooper());
        mHandler.setFragment(this);
        mHandler.setFragmentManager(this.getFragmentManager());
        mIsRunning =true;
    }

    /**
     * Method to call from derived class to show a progress dialog.
     * The progress dialog is shown for a max duration of 90 seconds.
     * If the derived class doesn't dismiss the dialog within 90 seconds,
     * the dialog is dismissed automatically.
     */
    public void showProgressDialog(int reqCode, Activity activity, BaseFragment fragment,
                                   FragmentManager manager, String title,
                                   String message, boolean cancelable){
        dismissProgressDialog(manager);
        MyProgressDialog.showProgressDialog(activity, manager, title, message, cancelable);
        mHandler.setFragment(fragment);
        mHandler.setFragmentManager(manager);
        Message timeOutMessage = mHandler.obtainMessage(BaseActivityActionBar.MSG_TIME_OUT);
        timeOutMessage.arg1 = reqCode;
        mHandler.sendMessageDelayed(timeOutMessage, TIMEOUT_TIME);
    }

    /**
     * Method to call from derived class to dismiss a progress dialog.
     */
    public  void dismissProgressDialog(FragmentManager manager){
        if(MyProgressDialog.dismissProgressDialog(manager)) {
            if (mHandler != null) {
                mHandler.removeMessages(BaseActivityActionBar.MSG_TIME_OUT);
            }
        }
    }

   /* @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if(hidden){
            mIsRunning = false;
        }else{
            mIsRunning = true;
        }

    }
*/
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onStart(){
        mIsRunning = true;
        super.onStart();
    }

    @Override
    public void onStop(){
        mIsRunning = false;
        super.onStop();
    }

    @Override
    public void onDestroy(){
        if(mHandler != null){
            mHandler.removeMessages(BaseActivityActionBar.MSG_TIME_OUT);
            mHandler.removeMessages(BaseActivityActionBar.MSG_DISMISS_WORKING);
        }
        super.onDestroy();
    }

    /**
     * Classes that extend are notified of progress dialog timeout.
     */
    abstract public void onDialogTimedOut(int reqCode);



    /**
     * Abstract API used to listen for custom messages posted to handler
     * @Param messageWhat -
     * @return void
     */
     public abstract void  processCustomMessage(Message messageWhat);
}
