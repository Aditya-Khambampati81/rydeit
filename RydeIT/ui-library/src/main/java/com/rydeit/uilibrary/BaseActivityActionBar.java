package com.rydeit.uilibrary;


import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;

import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;


import com.avast.android.dialogs.fragment.DatePickerDialogFragment;
import com.avast.android.dialogs.fragment.ProgressDialogFragment;
import com.avast.android.dialogs.fragment.SimpleDialogFragment;
import com.avast.android.dialogs.iface.ISimpleDialogListener;
import com.rydeit.uilibrary.dialog.MyProgressDialog;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.util.HashMap;
import android.support.v7.app.AppCompatActivity;



/**
 * This is base activity with customizable action bar
 */
@SuppressWarnings("serial")
public abstract class BaseActivityActionBar extends AppCompatActivity  implements ISimpleDialogListener {

    /**
     * Constant for Invalid Menu Id.
     * Activities extending this and hiding the action bar menu should use this value.
     */
    public static final int INVALID_MENU = -1;

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
     * Constant for informing super classes for back press event.
     * Derived classes can send message with this ID to the handler object.
     */
    public static int MSG_BACK_PRESSED = 1113;

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
     * place holder for hom button disabled
     */
    private boolean mHomeDisabled = false;

    private static final String DIALOG_TAG = "BaseActivityActionBar.DIALOG_TAG";

    private int ACTIVE_DIALOG_ID = 0;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mIsRunning =true;
        mActivityHandler = new DialogHandlerActivity(this);
        mActivityHandler.setFragmentManager(this.getSupportFragmentManager());

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
        {
            actionBar.setCustomView(R.layout.ab_title);
            actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            // customize the background if build version is > honeycomb
            if (Build.VERSION.SDK_INT>= Build.VERSION_CODES.HONEYCOMB) {
                getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.actionbar_bg)));
            }
            actionBar.show();

            registerHomeUpListener();

        }
        else
        {
            //FIXME:Add common logger library
            Log.e(BaseActivityActionBar.class.toString(), " Seems action bar is not available");

        }
        setNotificationBarColor(R.color.notificationbar_bg);
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


    public void handleBackPressed(){
        BaseActivityActionBar.super.onBackPressed();
    }

    private void registerHomeUpListener()
    {
        if (getSupportActionBar()!=null){
            View v= getSupportActionBar().getCustomView();
            ImageView iv = (ImageView)v.findViewById(R.id.img1);

            if (mHomeDisabled)
            {
                iv.setVisibility(View.GONE);
            }
            else {
                iv.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Message msg = mActivityHandler.obtainMessage(MSG_BACK_PRESSED);
                        BaseActivityActionBar.this.processCustomMessage(msg);
                        handleBackPressed();
                    }
                });
            }
        }

    }

    @Override
    protected void onPause() {
       if (mActivityHandler != null) {
                mActivityHandler.pause();
        }


        super.onPause();
    }


    @Override
    protected void onResume() {
        if (mActivityHandler != null) {
            mActivityHandler.resume();
        }

        super.onResume();
    }


    /**
     * By default home icon is enabled ,
     * can be disabled
     */
    public void setHomeDisabled(boolean disabled )
    {
        mHomeDisabled  = disabled;
        if (getSupportActionBar()!=null){
            View v= getSupportActionBar().getCustomView();
            if(v!=null) {
                ImageView iv = (ImageView) v.findViewById(R.id.img1);
                if (!disabled)
                    iv.setVisibility(View.VISIBLE);
                else
                    iv.setVisibility(View.GONE);
            }
         }
    }
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //mIsRunning = false;
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
     * Action bar background colour can be customized from activities that extend this
     * class
     *
     * @param drawableId of drawable R.drawable.xxxx
     */
    public void setActionBarBackground(int drawableId)throws IllegalArgumentException
    {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setBackgroundDrawable(this.getResources().getDrawable(drawableId));
        }
        else
            throw new IllegalArgumentException("Action Bar not supported !!");
    }

    /**
     * Set actionBar title
     * @param stringId
     */
    public void setTitle(int stringId)throws IllegalArgumentException
    {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar!= null) {
            View v= actionBar.getCustomView();
             if(v!=null) {
                 TextView tv = (TextView) v.findViewById(R.id.text1);
                 if (tv != null)
                     tv.setText(stringId);
             }
        }
        else
            throw new IllegalArgumentException("Action Bar not supported !!");
    }

    /**
     * Set actionBar title color
     * @param stringId
     */
    public void setTitleColor(int stringId)throws IllegalArgumentException
    {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar!= null) {
            View v= actionBar.getCustomView();
            if(v!=null) {
                TextView tv = (TextView) v.findViewById(R.id.text1);
                if (tv != null)
                    tv.setTextColor(stringId);
            }
        }
        else
            throw new IllegalArgumentException("Action Bar not supported !!");
    }

    /**
     * Set actionBar back button background
     * @param id
     */
    public void setBackButtonBackground(int id)throws IllegalArgumentException
    {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar!= null) {
            View v= actionBar.getCustomView();
            if(v!=null) {
                ImageView iv = (ImageView) v.findViewById(R.id.img1);
                if (iv != null)
                    iv.setImageResource(id);
            }
        }
        else
            throw new IllegalArgumentException("Action Bar not supported !!");
    }

    /**
     * Set actionBar title
     * @param stringId
     */
    public void setTitle(CharSequence stringId)throws IllegalArgumentException
    {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar!= null) {
            View v= actionBar.getCustomView();
            if(v!=null) {
                TextView tv = (TextView) v.findViewById(R.id.text1);
                if (tv != null)
                    tv.setText(stringId);
            }
        }
        else
            throw new IllegalArgumentException("Action Bar not supported !!");
    }

    /**
     * API to customize the icon for drawable
     *
     * @param drawableId
     */
    public void setIcon(int drawableId)throws IllegalArgumentException
    {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar!= null)
             actionBar.setIcon(drawableId);
        else
            throw new IllegalArgumentException("Action Bar not supported for your activity!!");
    }

    /**
     * API to hide action bar
      */
    public void hideActionBar()throws IllegalArgumentException
    {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar!= null)
            actionBar.hide();
        else
            throw new IllegalArgumentException("Action Bar not supported for your activity!!");
    }

    /**
     * Method to call from derived class to show a progress dialog.
     * The progress dialog is shown for a max duration of 90 seconds.
     * If the derived class doesn't dismiss the dialog within 90 seconds,
     * the dialog is dismissed automatically.
     */
    public void showProgressDialog(int reqCode, BaseActivityActionBar activity, FragmentManager manager, String title,
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
     * Classes that extend need to provide the
     * layout of menu here as return parameter
     * @return  -1 in case you are not adding any menu in action bar
     */

    abstract public int getActionBarMenuId();


    /**
     * Classes that extend this need to inform the base activity
     * about the action menu items
     *
     *  HashMap First element/ Key  is menu item id R.menu.XXXXXX
     *  MenuItem has menu item related information (drawable id , menu id and launch intent)
     *
     * @return
     */
    abstract public HashMap<Integer,MenuItem> getMenuItems();

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

        @Override
        public boolean onCreateOptionsMenu (Menu menu){
            // Inflate the menu; this adds items to the action bar if it is present.
            if (getActionBarMenuId() == INVALID_MENU)
                return false;
            getMenuInflater().inflate(getActionBarMenuId(), menu);
            return true;
        }

        @Override
        public boolean onPrepareOptionsMenu (Menu menu){
            HashMap<Integer, MenuItem> myMenuMap = getMenuItems();
            if (myMenuMap == null)
                return true;
            for (Integer key : myMenuMap.keySet()) {
                MenuItem item = myMenuMap.get(key);
                if (item != null) {
                    for (int i = 0; i < menu.size(); i++) {
                        if (menu.getItem(i) != null) {
                            if (menu.getItem(i).getItemId() == key) {
                                menu.getItem(i).setEnabled(item.isEnabled());
                                menu.getItem(i).setVisible(item.isVisible());

                                break;
                            }
                        }
                    }
                }
            }

            return super.onPrepareOptionsMenu(menu);
        }

    /**
     * Extended classes can refresh the menu items
     * by makign call to this API
     */
    public void refreshMenuItems()
    {
        supportInvalidateOptionsMenu();
    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement

        HashMap<Integer,MenuItem>  myMenuMap = getMenuItems();
        if ( android.R.id.home == id)
        {
            super.onBackPressed();
            return true;
        }


        if(myMenuMap != null && myMenuMap.size() > 0)
        {
            MenuItem temp = myMenuMap.get(id);
            if ( temp != null)
            {
                if (!temp.isSendBroadcast()) {
                    if(temp.getAction()!= null)
                        startActivity(temp.getAction());
                }
                else {
                    //on the current app can receive this broadcast as the protection level is signature.
                    if(temp.getAction()!= null)
                        this.getApplicationContext().sendBroadcast(temp.getAction(),"com.permission.broadcast");
                }
                return true;
            }
        }

        return super.onOptionsItemSelected(item);
    }


    /**
     * Function used to hide particular menu items , you need to call
     * refreshMenuItems() for changes to take effect, if changes in menu
     * are made while activity is already created.
     *
     * @param rIdOfMenuItem
     * @return
     * @throws IllegalArgumentException
     */
    public boolean hideMenuItem(int rIdOfMenuItem ) throws IllegalArgumentException
    {
        HashMap<Integer,MenuItem>  myMenuMap = getMenuItems();

        if (myMenuMap != null && myMenuMap.size() > 0)
        {
            if (myMenuMap.containsKey(rIdOfMenuItem))
            {
                MenuItem item = myMenuMap.get(rIdOfMenuItem);
                item.setVisible(false);
                myMenuMap.put(rIdOfMenuItem,item);
            }
            else
               throw new IllegalArgumentException("Menu id not present!!");
        }
        else
            return false;

        return true;
    }

    /**
     * Function used to hide set of menu items
     * You can call refreshMenuItems() for changes to take effect if
     * you are altering menu while activity is already created.
     * @param rIdOfMenuItem
     * @return
     */

    public boolean hideMenuItem(int[] rIdOfMenuItem)
    {
        if (rIdOfMenuItem == null)
            return false;
        int i = 0;

        do {
            hideMenuItem(rIdOfMenuItem[i]);
            i++;
        }while( i <rIdOfMenuItem.length );
        return true;
    }



    public void showTimeoutError(){
        String title = "Time Out";
        String message =  "Not able to reach server!!";
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

    public static  class MenuItem
    {

        public int getDarwableId() {
            return mDarwableId;
        }

        public void setDarwableId(int darwableId) {
            this.mDarwableId = darwableId;
        }

        public int getIdentifier() {
            return mMenuIdentifier;
        }

        public void setIdentifier(int identifier) {
            this.mMenuIdentifier = identifier;
        }

        public int getDisplayMessaageId() {
            return mDisplayMessaageId;
        }

        public void setDisplayMessaageId(int displayMessaageId) {
            this.mDisplayMessaageId = displayMessaageId;
        }

        public Intent getAction() {
            return mAction;
        }

        public void setAction(Intent action) {
            this.mAction = action;
        }

        public MenuItem(int menuId, int drawableId, int displayMessaageId, Intent i)
        {
            mMenuIdentifier = menuId;
            mDarwableId= drawableId ;
            mDisplayMessaageId = displayMessaageId;
            mAction = i;
        }

        public void setmShowAsIcon (boolean value)
        {
            mShowAsIcon = value;
        }

        public boolean getIconSettings()
        {
            return mShowAsIcon;
        }
        public boolean isSendBroadcast() {
            return sendBroadcast;
        }

        public void setSendBroadcast(boolean sendBroadcast) {
            this.sendBroadcast = sendBroadcast;
        }


        public boolean isVisible() {
            return isVisible;
        }

        public void setVisible(boolean isVisible) {
            this.isVisible = isVisible;
        }

        public boolean isEnabled(){
            return mIsEnbled;
        }

        public void setEnabled(boolean enabled){
            mIsEnbled = enabled;
        }

        // by default all menu items are enabled
        private boolean isVisible = true;
        private int mMenuIdentifier;
        private int mDarwableId;
        private int mDisplayMessaageId;
        private Intent mAction = null;
        private boolean mShowAsIcon = false;
        //Bydefault we will start activity , we can send broadcast if needed
        private boolean sendBroadcast = false;
        private boolean mIsEnbled = true;

    }


    /**
     * Hides the action bar menu of Activity
     *
     * @param hide
     */
    public void hideActionBarMenu(boolean hide) throws IllegalArgumentException
    {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.hide();
        else
            throw new IllegalArgumentException("Action Bar not supported for your activity!!");
    }
    public abstract void processCustomMessage(Message msg);



    // added as an instance method to an Activity
    public boolean isNetworkConnectionAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        if (info == null) return false;
        NetworkInfo.State network = info.getState();
        return (network == NetworkInfo.State.CONNECTED || network == NetworkInfo.State.CONNECTING);
    }

    public static final int DLG_NO_CONNECTIVITY=11111;

    /**
     * Public api to show different dialogs
     * @param id
     */
    public void showNetworkErrorDialog(int id) {

       SimpleDialogFragment.SimpleDialogBuilder builder = SimpleDialogFragment.createBuilder(
                this, getSupportFragmentManager());
        dismissProgressDialog(getSupportFragmentManager());
        switch (id) {

            case DLG_NO_CONNECTIVITY:
                builder.setTitle("No Netowork");
                builder.setMessage("You are not connected to internet");
                builder.setPositiveButtonText("OK");
                builder.setCancelableOnTouchOutside(false);
                break;

        }


        if (mIsRunning)
            builder.setRequestCode(id).setTag(DIALOG_TAG).show();
        else
            Log.e("TAG", "Not able to show dialog here :-( with id " + id);

    }
    /**
     * Default Handler class to handle progress dialog time out/dismiss.
     * Classes can extends this to provide additional functionality.
     */
    public class DialogHandlerActivity extends MyAbstractPauseHandler {

        private FragmentManager mFragmentManager;
        private int mReqCode;
        private WeakReference<BaseActivityActionBar> mSelf;
        public DialogHandlerActivity(BaseActivityActionBar activity)
        {
            super(activity.getMainLooper());
            mSelf = new WeakReference<BaseActivityActionBar>(activity);
        }

        @Override
        protected  void processMessage(Message message)
        {


            if (mFragmentManager == null)
            {
                Log.e(DialogHandlerActivity.class.toString(), "Fragment manager is not initialized!!");
                return;
            }
            if(MSG_TIME_OUT == message.what){
                if(mIsRunning) {

                    MyProgressDialog.dismissProgressDialog(mFragmentManager);
                    onDialogTimedOut(message.arg1);
                }
                else
                {
                    Log.e(DialogHandlerActivity.class.toString(),"mIsRunning is false!!");
                }
            }else if(MSG_DISMISS_WORKING == message.what){
                if(mIsRunning){
                    MyProgressDialog.dismissProgressDialog(mFragmentManager);
                }
                else
                {
                    Log.e(DialogHandlerActivity.class.toString(),"mIsRunning is false!!");
                }
            }
            else{
                /**
                 * extended classes can make use of base class handler and
                 * reuse the handler instead of creating one for them
                 */
                processCustomMessage(message);
                return;
            }

        }



        public void setFragmentManager(FragmentManager manager){
            mFragmentManager = manager;
        }
    }
}
