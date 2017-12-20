package com.rydeit.cab.service.uber;

import android.content.Context;
import android.os.Handler;

import com.rydeit.api.uber.UberAPIClient;
import com.rydeit.api.uber.UberCallback;
import com.rydeit.cab.service.uber.converter.DataConverter;
import com.rydeit.model.uber.PriceEstimate;
import com.rydeit.model.uber.PriceEstimateList;
import com.rydeit.model.uber.ProductList;
import com.rydeit.model.uber.Requests.UberStatus;
import com.rydeit.model.uber.sandbox.SandboxRequestBody;
import com.rydeit.util.Constants;
import com.rydeit.util.SharedPrefUtil;
import com.rydeit.view.MapCabFinder;

import java.util.List;

import retrofit.client.Response;

/**
 * Created by Prakhyath on 12/24/15.
 */
public class UberManager {

    private static final String TAG = UberManager.class.getSimpleName();

    private Context mContext = null;

    private Handler mHandler = new Handler();
    private boolean mForceStopTimer=false;

    private static UberManager mUberManager = null;
    DataConverter mDataConverter=null;

    private static final int WAIT_TIMER=1000;

    private UberManager(Context context) {
        mContext = context;
        mDataConverter=DataConverter.getInstance();

        init();
    }

    public static UberManager getInstance(Context context) {
        if (mUberManager == null) {
            mUberManager = new UberManager(context);
        }

        return mUberManager;
    }

    void init() {

        queryUberProductDetails();
    }

    private void queryUberProductDetails(){
        mForceStopTimer=false;

        Runnable mRunnableTimer = new Runnable() {
            @Override
            public void run() {

                if(mForceStopTimer)
                    return;

                if(MapCabFinder.PIN_LATITUDE!=0.0 && MapCabFinder.PIN_LONGITUDE!=0.0){
                    getUberProductList();
                    getUberPriceEstimates();
                }
                else
                    mHandler.postDelayed(this, WAIT_TIMER);
            }
        };
        mHandler.postDelayed(mRunnableTimer, 0);
    }

    void getUberProductList(){
        UberAPIClient.getUberV1APIClient().getProducts(getServerToken(),
                MapCabFinder.PIN_LATITUDE,
                MapCabFinder.PIN_LONGITUDE,
                new UberCallback<ProductList>() {
                    @Override
                    public void success(ProductList productList, Response response) {
                        mDataConverter.setConvertedProductList(productList);
                    }
                });
    }

    void getUberPriceEstimates(){
        UberAPIClient.getUberV1APIClient().getPriceEstimates(getServerToken(),
                MapCabFinder.PIN_LATITUDE,
                MapCabFinder.PIN_LONGITUDE,
                MapCabFinder.PIN_LATITUDE,
                MapCabFinder.PIN_LONGITUDE,
                new UberCallback<PriceEstimateList>() {
                    @Override
                    public void success(PriceEstimateList priceEstimateList, Response response) {
                        mDataConverter.setConvertedEstimateList(priceEstimateList);

//                        //Sandbox put requests
                        if (Constants.SIMULATE_BOOKING) {
                            List<PriceEstimate> prices = priceEstimateList.getPrices();
                            for (PriceEstimate priceEstimate : prices) {
                                UberSandBox.putUberProductSandBox(mContext, priceEstimate.getProductId(), true, priceEstimate.getSurgeMultiplier());
                            }
                        }

                        //Sandbox put requests-Comment above block uncomment this add artificial surge
//                        if (Constants.SIMULATE_BOOKING) {
//                            int count = 0;
//                            for (PriceEstimate priceEstimate : priceEstimateList.getPrices()) {
//                                count++;
//                                if (count == 2)
//                                    UberSandBox.putUberProductSandBox(mContext, priceEstimate.getProductId(), true, 1.3f);
//                                else if (count == 3)
//                                    UberSandBox.putUberProductSandBox(mContext, priceEstimate.getProductId(), true, 3.1f);
//                                else if (count > 3)
//                                    UberSandBox.putUberProductSandBox(mContext, priceEstimate.getProductId(), false, 2.2f);
//                            }
//                        }
                    }
                });
    }

    public void putUberSandboxRideRequestStatus(String requestId, UberStatus uberStatus){

        if (Constants.SIMULATE_BOOKING) {
            final String rydeid =requestId;
            final UberStatus SandboxRideStatus = (uberStatus.value()==null)?UberStatus.ACCEPTED : uberStatus;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    SandboxRequestBody mSandbox = new SandboxRequestBody(SandboxRideStatus);
                    UberAPIClient.getUberV1SandBoxAPIClient().putRequest(getAccessToken(Constants.CAB_GLOBAL.UBER),rydeid , mSandbox);
                }
            }).start();

        }
    }

    public void notifyLocationChange(){
        init();
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

    public String getServerToken() {
        if(getAccessToken(getCab())!=null)
            return getUberAccessToken();
        else
            return "Token " + Constants.getUberServerToken(mContext);
    }

    public boolean isLoggedIn(){
        if(getUberAccessToken()!=null)
            return true;
        else
            return false;
    }

    public String getUberAccessToken(){
        return getAccessToken(getCab());
    }

    public Constants.CABCOMPANY getCab(){
        return Constants.CAB_GLOBAL.UBER;
    }

    public void clearTasks(){
        mForceStopTimer=true;
        if(mHandler != null)
            mHandler.removeCallbacks(null);
    }
}