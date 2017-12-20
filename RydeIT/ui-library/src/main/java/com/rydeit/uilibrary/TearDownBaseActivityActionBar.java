package com.rydeit.uilibrary;

import android.app.Activity;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;

import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.ViewConfiguration;
import android.view.Window;
import android.view.WindowManager;


import com.avast.android.dialogs.fragment.DatePickerDialogFragment;
import com.avast.android.dialogs.fragment.SimpleDialogFragment;
import com.avast.android.dialogs.iface.ISimpleDialogListener;
import com.rydeit.uilibrary.dialog.MyProgressDialog;


import java.lang.ref.WeakReference;
import java.lang.reflect.Field;

/**
 * This is base activity with customizable action bar
 */
@SuppressWarnings("serial")
public abstract class TearDownBaseActivityActionBar extends AppCompatActivity implements ISimpleDialogListener {


    /**
     * Constant for Progress Dialog Time Out.
     * Derived classes can send message with this ID to the handler object.
     */
    public static int MSG_TIME_OUT = 1001;

    /**
     * Constant for dismissing a dialog.
     * Derived classes can send message with this ID to the handler object.
     */
    public static int MSG_DISMISS_WORKING = 1002;

    public final static int MSG_ROOTED_DEVICE = 1003;



    /**
     * Time constant for progress dialog time outs.
     * After 90 seconds, the ongoing progress dialog shall be dismissed.
     */
    public static long TIMEOUT_TIME = 90000;

    /**
     * Default Handler object.
     * Derived classes can send messages for Dialog timeout or dismissing dialog.
     */
    public DialogHandlerActivity mActivityHandler = null;

    /**
     *     Flag to indicate if activity is running.
     */
    public boolean mIsRunning =false;

    /**
     * Flag to turn off the listening of shake event
     *
     */
    private boolean mFeedbackStatus = false;
    /**
     * place holder for hom button disabled
     */
    private boolean mHomeDisabled = false;

    public static final String DIALOG_TAG = "BaseActivityActionBar.DIALOG_TAG";



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mIsRunning =true;
        mActivityHandler = new DialogHandlerActivity(this);
        mActivityHandler.setFragmentManager(this.getSupportFragmentManager());

        /**
         * The following is hackish way of forcing virtual menu on devices with physical menu key
         */

        try {
            ViewConfiguration config = ViewConfiguration.get(this);
            Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
            /**
             * If there is only one item in menu then it has to be added in top right corner as text
             */
            if(menuKeyField != null ) {
                menuKeyField.setAccessible(true);
                menuKeyField.setBoolean(config, false);
            }
        } catch (Exception ex) {
            // Ignore
        }
    }
    public void setNotificationBarColor(int color){
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(getResources().getColor(color));
        }
    }

    @Override
    protected void onPause() {
        if (mActivityHandler != null) {
            mActivityHandler.pause();
        }
        if (mFeedbackStatus)
        {
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        //Reset boolean when activity resumes after any dialog dismiss
        mIsRunning = true;
        if (mActivityHandler != null) {
            mActivityHandler.resume();
        }
        if (mFeedbackStatus)
        {

        }
        super.onResume();
    }

    /**
     * Activities in which feedback has to be launched on shake event
     * can return false in extended class if it is not needed
     * By default it is disabled
     *
     * @return
     */
    public void setFeedbackForCurrentActivity(boolean fback)
    {
        mFeedbackStatus = fback;
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mIsRunning = false;
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
        if(mActivityHandler != null){
            mActivityHandler.removeMessages(MSG_TIME_OUT);
            mActivityHandler.removeMessages(MSG_DISMISS_WORKING);
        }
        super.onDestroy();
    }



    /**
     * Method to call from derived class to show a progress dialog.
     * The progress dialog is shown for a max duration of 90 seconds.
     * If the derived class doesn't dismiss the dialog within 90 seconds,
     * the dialog is dismissed automatically.
     */
    public void showProgressDialog(int reqCode, Activity activity, FragmentManager manager, String title,
                                   String message, boolean cancelable){
        dismissProgressDialog(manager);
        MyProgressDialog.showProgressDialog(activity, manager, title, message, cancelable);
        mActivityHandler.setFragmentManager(manager);
        Message timeOutMessage = mActivityHandler.obtainMessage(MSG_TIME_OUT);
        timeOutMessage.arg1 = reqCode;
        mActivityHandler.sendMessageDelayed(timeOutMessage, TIMEOUT_TIME);
    }

    /**
     * Method to call from derived class to dismiss a progress dialog.
     */
    public void dismissProgressDialog(FragmentManager manager){
        if(MyProgressDialog.dismissProgressDialog(manager)) {
            if (mActivityHandler != null) {
                mActivityHandler.removeMessages(MSG_TIME_OUT);
            }
        }
    }



    /**
     * Classes that extend are notified of progress dialog timeout.
     */
    abstract public void onDialogTimedOut(int reqCode);

    @Override
    public void onPositiveButtonClicked(int reqCode) {
        if (reqCode == MSG_TIME_OUT) {
            DialogFragment df = (DialogFragment) getSupportFragmentManager()
                    .findFragmentByTag(DIALOG_TAG);
            if (df != null) {
                df.dismissAllowingStateLoss();
            }
        } else if (reqCode == MSG_ROOTED_DEVICE) {

            onDialogTimedOut(reqCode);
            //finish the existing activity as the device is rooted device.
            this.finish();
        }
    }
    @Override
    public void onNegativeButtonClicked ( int reqCode){

    }


    @Override
    public void onNeutralButtonClicked ( int reqCode){

    }
   public void showTimeoutError(){
        String title = "Timed Out";
        String message = "Not able to reach server!!";
        showTimeoutError(title, message);
    }

    public void showTimeoutError(String title, String message){
        Context context = getApplicationContext();
        if(context != null && mIsRunning){
            dismissProgressDialog(getSupportFragmentManager());
            SimpleDialogFragment.SimpleDialogBuilder builder = SimpleDialogFragment.createBuilder(
                    context, getSupportFragmentManager());
            builder.setTitle(title);
            builder.setMessage(message);
            builder.setPositiveButtonText("Ok");
            builder.setRequestCode(MSG_TIME_OUT).setTag(DIALOG_TAG).show();
        }
    }



    public abstract void processCustomMessage(Message msg);
    /**
     * Default Handler class to handle progress dialog time out/dismiss.
     * Classes can extends this to provide additional functionality.
     */
    public class DialogHandlerActivity extends MyAbstractPauseHandler{

        private FragmentManager mFragmentManager;
        private int mReqCode;
        private WeakReference<TearDownBaseActivityActionBar> mSelf;
        public DialogHandlerActivity(TearDownBaseActivityActionBar activity)
        {
            super(activity.getMainLooper());
            mSelf = new WeakReference<TearDownBaseActivityActionBar>(activity);
        }

        @Override
        protected  void processMessage(Message message)
        {

            if(message != null) {
                if (mFragmentManager == null) {
                    Log.e(DialogHandlerActivity.class.toString(), "Fragment manager is not initialized!!");
                    return;
                }
                if (MSG_TIME_OUT == message.what) {
                    if (mIsRunning) {
                        MyProgressDialog.dismissProgressDialog(mFragmentManager);
                        onDialogTimedOut(message.arg1);
                    }
                } else if (MSG_DISMISS_WORKING == message.what) {
                    if (mIsRunning) {
                        MyProgressDialog.dismissProgressDialog(mFragmentManager);
                    }
                } else {
                    /**
                     * extended classes can make use of base class handler and
                     * reuse the handler instead of creating one for them
                     */
                    processCustomMessage(message);
                    return;
                }
            }
        }

        public void setFragmentManager(FragmentManager manager){
            mFragmentManager = manager;
        }
    }
}
