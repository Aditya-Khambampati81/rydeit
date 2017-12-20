package com.rydeit.cab.service.ola;

import android.content.Context;
import android.os.Handler;

import com.rydeit.cab.service.ola.converter.DataConverter;
import com.rydeit.util.Constants;
import com.rydeit.util.SharedPrefUtil;

/**
 * Created by Prakhyath on 12/24/15.
 */
public class OlaManager {

    private Context mContext = null;

    private Handler mHandler = new Handler();
    private boolean mForceStopTimer=false;

    private static OlaManager mOlaManager = null;
    DataConverter mDataConverter=null;

    private static final int WAIT_TIMER=1000;

    private OlaManager(Context context) {
        mContext = context;
        mDataConverter=DataConverter.getInstance();

        init();
    }

    public static OlaManager getInstance(Context context) {
        if (mOlaManager == null) {
            mOlaManager = new OlaManager(context);
        }

        return mOlaManager;
    }

    void init() {

    }

    private String getAccessToken(Constants.CABCOMPANY CabCompany) {

        String AccessToken=null,TokenType=null;
        if(SharedPrefUtil.getStringPreference(mContext, Constants.getAccessTokenKey(CabCompany))!=null) {
            AccessToken = SharedPrefUtil.getStringPreference(mContext, Constants.getAccessTokenKey(CabCompany));
            TokenType = SharedPrefUtil.getStringPreference(mContext, Constants.getTokenTypeKey(CabCompany));
            return TokenType + " " + AccessToken;
        }
        return null;
    }

    private String getServerToken() {
        if(getAccessToken(getCab())!=null)
            return getUberAccessToken();
        else
            return "Token " + Constants.getUberServerToken(mContext);
    }

    public String getUberAccessToken(){
        return getAccessToken(getCab());
    }

    public Constants.CABCOMPANY getCab(){
        return Constants.CAB_INDIA.OLA;
    }

    public void clearTasks(){
        mForceStopTimer=true;
        if(mHandler != null)
            mHandler.removeCallbacks(null);
    }
}