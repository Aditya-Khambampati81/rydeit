package com.rydeit.view;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.avast.android.dialogs.fragment.SimpleDialogFragment;
import com.rydeit.R;
import com.rydeit.api.ola.OlaCallback;
import com.rydeit.api.uber.UberCallback;
import com.rydeit.cab.service.ola.OlaSandBox;
import com.rydeit.cab.service.uber.UberAPIConstants;
import com.rydeit.cab.service.uber.UberManager;
import com.rydeit.database.DatabaseUtil;
import com.rydeit.flurry.MyEventManager;
import com.rydeit.model.common.MapPoint;
import com.rydeit.model.common.MyBooking;
import com.rydeit.model.ola.RideBooking;
import com.rydeit.model.ola.TrackRide;
import com.rydeit.model.uber.Requests.ErrorResponses.Error;
import com.rydeit.model.uber.Requests.ErrorResponses.ErrorResponse;
import com.rydeit.model.uber.Requests.ErrorResponses.Meta;
import com.rydeit.model.uber.Requests.UberStatus;
import com.rydeit.model.uber.RideRequest;
import com.rydeit.provider.CabApiClientFactory;
import com.rydeit.provider.GenericCabInterface;
import com.rydeit.uilibrary.BaseActivityActionBar;
import com.rydeit.util.AndroidUtils;
import com.rydeit.util.ConfigurationObject;
import com.rydeit.util.Constants;
import com.rydeit.util.JavaUtil;
import com.rydeit.util.Log;
import com.rydeit.view.common.CabEstimateListAdapter;

import java.util.HashMap;
import java.util.List;

import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by Prakhyath on 11/17/15.
 */
public class ConfirmBookingActivity extends BaseActivityActionBar {

    private static final String TAG = ConfirmBookingActivity.class.getSimpleName();

    public static final int REQUEST_CODE_OLA=101;
    public static final int REQUEST_CODE_UBER=102;
    public static final int MSG_RIDE_STATUS = 103;
    public static final int REQUEST_URL_SURGE_CONFIRMATION=104;

    TextView tvCabname;
    TextView tvCabLocaltion;
    TextView tvCabType;
    TextView tvCabETA;
    TextView tv3label;
    TextView tv3value;
    TextView tv4label;
    TextView tv4value;
    TextView tv5label;
    TextView tv5value;
    ImageView CabIcon;

    LinearLayout llBookCab;
    TextView tvBookingLabel;
    TextView tvBookCabLabel;

    //LinearLayout llSection5;

    CabEstimateListAdapter.Estimate estimate;

    RideBooking mRideBooking;//OLA

    MyBooking mMyBooking;
    TrackRide mTrackRide;//OLA

    static String AccessToken;
    static String TokenType;

    public double PIN_LATITUDE;
    public double PIN_LONGITUDE;
    public  static final int PROGRESS_DIALOG_CABBOOKING=11212;
    public static final int WAIT_TIMER=2000;
    GenericCabInterface mGenericInterface = null;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        setHomeDisabled(true);
        super.onCreate(savedInstanceState);
        if(getSupportActionBar() !=null) {
            getSupportActionBar().setDisplayShowHomeEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.actionbar_bg)));
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.left_arrow_white1);
            setTitle(R.string.confirmbooking);
        }
        if(getIntent()!= null) {
            estimate=(CabEstimateListAdapter.Estimate) getIntent().getSerializableExtra("bookitem");

            PIN_LATITUDE = getIntent().getDoubleExtra("pickupLattitude", 0);
            PIN_LONGITUDE = getIntent().getDoubleExtra("pickupLongitude", 0);
        }
        else
        {
            Log.i(TAG,"Something weird !!");
        }
        Log.i(TAG, "Book item :" + estimate + "lat :" + PIN_LATITUDE + " Long :" + PIN_LONGITUDE);
        setContentView(R.layout.activity_confirm_booking);
        initViews();

        if (Constants.SIMULATE_BOOKING)
            Toast.makeText(this, "SIMULATION MODE : ON",Toast.LENGTH_SHORT).show();
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
        switch (msg.what)
        {
            case MSG_RIDE_STATUS :
                Bundle b = msg.getData();
                String rydeid = b.getString("req");
               // CabTracker.getInstance().queryUberRideRequestDetails(rydeid,callbackRideRequest);
                CabApiClientFactory.getCabProvider(CabApiClientFactory.PROVIDER_UBER).getRideStatus(rydeid, callbackRideRequest);
                break;
            default:
                ;
        }


    }

    void initViews(){
        tvCabname=(TextView)findViewById(R.id.cabname);
        tvCabLocaltion=(TextView)findViewById(R.id.location);
        tvCabType=(TextView)findViewById(R.id.cabtype);
        tvCabETA=(TextView)findViewById(R.id.eta);

        tv3value =(TextView)findViewById(R.id.text3value);
        tv3label =(TextView)findViewById(R.id.text3label);

        tv4value =(TextView)findViewById(R.id.text4value);
        tv4label =(TextView)findViewById(R.id.text4label);

        //llSection5=(LinearLayout)findViewById(R.id.ll_section5);
        tv5value =(TextView)findViewById(R.id.text4value);
        tv5label =(TextView)findViewById(R.id.text4label);

        CabIcon=(ImageView)findViewById(R.id.cabicon);
        llBookCab=(LinearLayout)findViewById(R.id.ll_bookcab);
        tvBookingLabel=(TextView)findViewById(R.id.bookinglabel);
        tvBookCabLabel=(TextView)findViewById(R.id.btnbookCab);

        if(estimate!=null){
            tvCabname.setText(estimate.cabcompany.toString());
            tvCabLocaltion.setText(estimate.address);
            tvCabType.setText(String.valueOf(estimate.display_name));
            tvCabETA.setText(estimate.eta+" mins");
            tv3value.setText(estimate.costPerDistance + "/km");
            if(estimate.surcharge>0) {
                tv4value.setText(String.valueOf(estimate.surcharge));
                tv4label.setTextColor(Color.RED);
            }
            else if(estimate.surcharge==-1) {
                tv4value.setVisibility(View.GONE);
                tv4label.setVisibility(View.GONE);
            }

            if(estimate.cabcompany== Constants.CAB_GLOBAL.UBER)
                CabIcon.setImageResource(R.drawable.uber_icon_36px);
            else if(estimate.cabcompany== Constants.CAB_INDIA.OLA)
                CabIcon.setImageResource(R.drawable.ola_icon);
        }

        llBookCab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(!ConfirmBookingActivity.this.isNetworkConnectionAvailable())
                {
                    showNetworkErrorDialog(DLG_NO_CONNECTIVITY);
                    return;
                }

                showProgressDialog(PROGRESS_DIALOG_CABBOOKING, ConfirmBookingActivity.this,ConfirmBookingActivity.this.getSupportFragmentManager(),"Getting Cab", "Moving you shortly....", true);

                if(estimate.cabcompany== Constants.CAB_GLOBAL.UBER) {
                    //FIXME: when we integrate UBER we have to move to appropriate place.
                    MyEventManager.getInstance().loadBookingStatus("cs", "UBER");

                    if(!ConfigurationObject.uberwithin) {
                        launchUberApp();
                        finish();
                    }
                    else {
                        Intent intent=new Intent(ConfirmBookingActivity.this, CabAuthActivity.class);
                        intent.putExtra("requestCode", REQUEST_CODE_UBER);
                        startActivityForResult(intent,REQUEST_CODE_UBER);
                    }
                }else if(estimate.cabcompany== Constants.CAB_INDIA.OLA){
                    Intent intent=new Intent(ConfirmBookingActivity.this, CabAuthActivity.class);
                    intent.putExtra("requestCode", REQUEST_CODE_OLA);
                    startActivityForResult(intent,REQUEST_CODE_OLA);
                }
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        clearBookingDetails();
        if(data  == null)
        {
            dismissProgressDialog(this.getSupportFragmentManager());
        }

        if(resultCode==REQUEST_CODE_OLA && data!=null) {
            String AccessToken = data.getStringExtra("access_token");//TODO FIX THIS LOGIC- NO NEED PASS ACCESS TOKEN
            String TokenType = data.getStringExtra("token_type");

            if(TokenType.equals("bearer")){
                TokenType="Bearer";//FIXME- TEMP FIX TO OLA API ISSUE
            }

            this.AccessToken=AccessToken;
            this.TokenType=TokenType;
            MyEventManager.getInstance().loadBookingStatus("cs", "OLA");
            bookOlaCab(AccessToken, TokenType);
        }
        else if (resultCode==REQUEST_CODE_UBER && data!=null) {
            //launchUberApp();
            bookUberCab(estimate.id, null);
        }
        else if (resultCode==REQUEST_URL_SURGE_CONFIRMATION && data!=null) {
            bookUberCab(estimate.id, data.getStringExtra(UberAPIConstants.SURGE_REDIRECT_PARAM.RIDE_SURGE_CONFIRM_ID));
        }
        else
            MyEventManager.getInstance().loadBookingStatus("cf", "OLA");//FIXME- WRONG ANALYTICS LOGGING

    }

    /**
     *
     * OLA METHODS STARTS HERE
     *
     * */
    void bookOlaCab(String AccessToken, String TokenType)
    {
        CabApiClientFactory.getCabProvider(CabApiClientFactory.PROVIDER_OLA)
                .bookCab(estimate.id,TokenType,AccessToken,MapCabFinder.PIN_LATITUDE,MapCabFinder.PIN_LONGITUDE,callbackRideBooking);


   /**
        if(Constants.SIMULATE_BOOKING){
            Log.d(TAG,"SIMULATION MODE: BOOK CAB");
            mRideBooking = OlaSandBox.getSimulateRideBookingObject();

            trackOlaBooking();

            return;
        }

        OlaAPIClient.getOlaV1APIClient().bookRide(OlaAPIConstants.getOlaXAppToken(this),
                TokenType + " " + AccessToken,
                MapCabFinder.PIN_LATITUDE,
                MapCabFinder.PIN_LONGITUDE,
                "NOW",
                estimate.id,
                new OlaCallback<RideBooking>() {

                    @Override
                    public void success(RideBooking rideBooking, Response response) {
                        dismissProgressDialog(ConfirmBookingActivity.this.getSupportFragmentManager());

                        mRideBooking = rideBooking;
                        Log.d(TAG, "Booking Details: " + mRideBooking.toString());
                        if (mRideBooking == null || (mRideBooking!= null && mRideBooking.crn == null))
                        {
                            showCustomDialog(DIALOG_GENERIC_ERROR ,"Some weird error in booking try after few  mins!!");
                            return;
                        }
                        trackOlaBooking();

                        Toast.makeText(ConfirmBookingActivity.this, "Booking is Success", Toast.LENGTH_LONG).show();
                        JavaUtil.printHTTPResponse(response);
                    }


                    @Override
                    public void failure(RetrofitError error) {
                        dismissProgressDialog(ConfirmBookingActivity.this.getSupportFragmentManager());
                        Log.e(TAG, "MESSAGE=" + error.toString());
                        Log.e(TAG, "MESSAGE RESPONSE=" + error.getResponse());

                        OErrorResponse errorResponse  = null;
                        errorResponse=(OErrorResponse)error.getBodyAs(OErrorResponse.class);

                        if (errorResponse != null)
                        {


                            if(errorResponse.getEcode().compareToIgnoreCase("NO_CABS_AVAILABLE")==0)
                            {
                                // no cabs available.
                                showCustomDialog(DIALOG_NO_DRIVER_AVAILABLE ,"");
                            }
                            else if (errorResponse.getEcode().compareToIgnoreCase("INVALID_USER")==0)
                            {
                                // user is not valid
                                showCustomDialog(DIALOG_GENERIC_ERROR ,"User account is invalid!!");
                            }
                            else if(errorResponse.getEcode().compareToIgnoreCase("INVALID_LAT_LNG")==0)
                            {
                                // lat long are not valid.
                                showCustomDialog(DIALOG_GENERIC_ERROR ,"Invalid user location!!");

                            }
                            else if(errorResponse.getEcode().compareToIgnoreCase("INVALID_CAR_CATEGORY")==0)
                            {
                                //car category is not now
                                showCustomDialog(DIALOG_GENERIC_ERROR ,"Car category is not valid");
                            }
                            else if(errorResponse.getEcode().compareToIgnoreCase("INVALID_PICKUP_MODE")==0)
                            {
                                // pickup mode is not now
                                showCustomDialog(DIALOG_GENERIC_ERROR ,"Car category is not valid");
                            }

                            else {
                                showCustomDialog(DIALOG_GENERIC_ERROR ,"Some weird error in booking try after few  mins!!");
                            }

                        }

                    }

                });
    **/

    }


   private OlaCallback<RideBooking> callbackRideBooking =  new OlaCallback<RideBooking>() {

        @Override
        public void success(RideBooking rideBooking, Response response) {
            dismissProgressDialog(ConfirmBookingActivity.this.getSupportFragmentManager());

            mRideBooking = rideBooking;
            Log.d(TAG, "Booking Details: " + mRideBooking.toString());
            if (mRideBooking == null || (mRideBooking!= null && mRideBooking.crn == null))
            {
                showCustomDialog(DIALOG_GENERIC_ERROR ,"Some weird error in booking try after few  mins!!");
                return;
            }
            trackOlaBooking();

            Toast.makeText(ConfirmBookingActivity.this, "Booking is Success", Toast.LENGTH_LONG).show();
            JavaUtil.printHTTPResponse(response);
        }


        @Override
        public void failure(RetrofitError error) {
            dismissProgressDialog(ConfirmBookingActivity.this.getSupportFragmentManager());
            Log.e(TAG, "MESSAGE=" + error.toString());
            Log.e(TAG, "MESSAGE RESPONSE=" + error.getResponse());



            showCustomDialog(DIALOG_GENERIC_ERROR, error.getMessage());


            /**
            OErrorResponse errorResponse  = null;
            errorResponse=(OErrorResponse)error.getBodyAs(OErrorResponse.class);

            if (errorResponse != null)
            {


                if(errorResponse.getEcode().compareToIgnoreCase("NO_CABS_AVAILABLE")==0)
                {
                    // no cabs available.
                    showCustomDialog(DIALOG_NO_DRIVER_AVAILABLE ,"");
                }
                else if (errorResponse.getEcode().compareToIgnoreCase("INVALID_USER")==0)
                {
                    // user is not valid
                    showCustomDialog(DIALOG_GENERIC_ERROR ,"User account is invalid!!");
                }
                else if(errorResponse.getEcode().compareToIgnoreCase("INVALID_LAT_LNG")==0)
                {
                    // lat long are not valid.
                    showCustomDialog(DIALOG_GENERIC_ERROR ,"Invalid user location!!");

                }
                else if(errorResponse.getEcode().compareToIgnoreCase("INVALID_CAR_CATEGORY")==0)
                {
                    //car category is not now
                    showCustomDialog(DIALOG_GENERIC_ERROR ,"Car category is not valid");
                }
                else if(errorResponse.getEcode().compareToIgnoreCase("INVALID_PICKUP_MODE")==0)
                {
                    // pickup mode is not now
                    showCustomDialog(DIALOG_GENERIC_ERROR ,"Car category is not valid");
                }

                else {
                    showCustomDialog(DIALOG_GENERIC_ERROR ,"Some weird error in booking try after few  mins!!");
                }

            }
             **/

        }
    };




    void movetoTrackingPage(){
        //updateBookingDetails(mRideBooking);
        Intent intent = new Intent(ConfirmBookingActivity.this, TrackMyRideActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        //intent.putExtra("PickUpAddress", estimate.address);
        intent.putExtra("TrackRideInProgress", mMyBooking);
        intent.putExtra("isNewBooking", true);
        startActivity(intent);
        finish();
    }

    void trackOlaBooking(){

        if(Constants.SIMULATE_BOOKING){
            Log.d(TAG, "SIMULATION MODE: TRACK CAB");

            mTrackRide=OlaSandBox.getSimulatedClientLocated();

            adaptOlaMyBooking(mRideBooking);

            insertBookingInfo();

            movetoTrackingPage();

            return;
        }

        CabApiClientFactory.getCabProvider(CabApiClientFactory.PROVIDER_OLA).getRideStatus(mRideBooking.crn, callbackOlaTrack);

/**
        OlaAPIClient.getOlaV1APIClient().trackRide(OlaAPIConstants.getOlaXAppToken(this),
                TokenType + " " + AccessToken,
                new OlaCallback<TrackRide>() {
                    @Override
                    public void success(TrackRide trackRide, Response response) {
                        if(trackRide!=null && trackRide.duration!=null && Constants.ENABLE_DEBUG_TOAST)
                            Toast.makeText(ConfirmBookingActivity.this, "Status:\"+trackRide.status+\"\n"
                                    +" Booking Status:"+trackRide.booking_status+"+ \nCar Arrival Time:"
                                    +trackRide.duration.value, Toast.LENGTH_LONG).show();
                        else if(trackRide!=null && Constants.ENABLE_DEBUG_TOAST)
                            Toast.makeText(ConfirmBookingActivity.this, "Status:"+trackRide.status+"\n " +
                                    "Booking Status:"+trackRide.booking_status, Toast.LENGTH_LONG).show();

                        mTrackRide=trackRide;
                        Log.d(TAG,"TRACK RIDE Details: "+mTrackRide.toString());
                        Log.d(TAG,"DRIVER POSITION: lat:"+mTrackRide.driver_lat+" log:"+mTrackRide.driver_lng);

                        adaptOlaMyBooking(mRideBooking);

                        insertBookingInfo();

                        movetoTrackingPage();
                    }
                }
        );
**/
    }

    private OlaCallback<TrackRide> callbackOlaTrack = new OlaCallback<TrackRide>() {
        @Override
        public void success(TrackRide trackRide, Response response) {
            if(trackRide!=null && trackRide.duration!=null && Constants.ENABLE_DEBUG_TOAST)
                Toast.makeText(ConfirmBookingActivity.this, "Status:\"+trackRide.status+\"\n"
                        +" Booking Status:"+trackRide.booking_status+"+ \nCar Arrival Time:"
                        +trackRide.duration.value, Toast.LENGTH_LONG).show();
            else if(trackRide!=null && Constants.ENABLE_DEBUG_TOAST)
                Toast.makeText(ConfirmBookingActivity.this, "Status:"+trackRide.status+"\n " +
                        "Booking Status:"+trackRide.booking_status, Toast.LENGTH_LONG).show();

            mTrackRide=trackRide;
            Log.d(TAG,"TRACK RIDE Details: "+mTrackRide.toString());
            Log.d(TAG,"DRIVER POSITION: lat:"+mTrackRide.driver_lat+" log:"+mTrackRide.driver_lng);
            adaptOlaMyBooking(mRideBooking);
            insertBookingInfo();
            movetoTrackingPage();
        }
    };


    private void adaptOlaMyBooking(RideBooking rideBooking){
        mMyBooking=new MyBooking();
        mMyBooking.crn=rideBooking.crn;
        mMyBooking.driver_name=rideBooking.driver_name;
        mMyBooking.cab_number=rideBooking.cab_number;
        mMyBooking.cab_type=rideBooking.cab_type;
        mMyBooking.car_model=rideBooking.car_model;
        mMyBooking.eta=rideBooking.eta;
        mMyBooking.pickupLoction=new MapPoint(PIN_LATITUDE,PIN_LONGITUDE);
        mMyBooking.pickUpAddress= estimate.address;
        mMyBooking.cabCompany = Constants.CAB_INDIA.OLA.toString();
        mMyBooking.driver_number=String.valueOf(rideBooking.driver_number);

        if(mTrackRide!=null)
            mMyBooking.booking_status=mTrackRide.booking_status;

        Log.d(TAG,"OLA: Mybooking="+mMyBooking.toString());
    }
    /**
     *
     * OLA METHODS ENDS HERE
     *
     * */


    /**
     *
     * UBER METHODS STARTS HERE
     *
     * */

    private UberCallback<RideRequest> callbackRideRequest = new UberCallback<RideRequest>()
    {
        @Override
        public void success(RideRequest rideRequest, Response response) {

            if(rideRequest==null && Constants.ENABLE_DEBUG_TOAST){
                Toast.makeText(ConfirmBookingActivity.this, "RESPONSE: RideRequest is null", Toast.LENGTH_LONG).show();
                return;
            }

            if(rideRequest!=null && rideRequest.getRequest_id()!=null) {

                if (rideRequest.getStatus().equalsIgnoreCase(UberStatus.PROCESSING.toString())
                    || rideRequest.getStatus().equalsIgnoreCase(UberStatus.ARRIVING.value().toString())
                    || rideRequest.getStatus().equalsIgnoreCase(UberStatus.ACCEPTED.value().toString())
                    || rideRequest.getStatus().equalsIgnoreCase(UberStatus.IN_PROGRESS.value().toString())) {

                    Log.d(TAG, "REQUEST ID: " + rideRequest.getRequest_id());


                    Log.d(TAG, "RESPONSE:RESULT1 :  "+rideRequest.getStatus());
                    Log.d(TAG, "RESPONSE:RESULT2 :  "+UberStatus.PROCESSING.value());
                    Log.d(TAG, "RESPONSE:RESULT3 :  "+rideRequest.getVehicle());
                    Log.d(TAG, "RESPONSE:RESULT4 :  "+rideRequest.getStatus().equalsIgnoreCase(UberStatus.PROCESSING.value()));

                    if(rideRequest.getStatus().equalsIgnoreCase(UberStatus.PROCESSING.value()) || rideRequest.getVehicle()==null){

                        Log.d(TAG, "RESPONSE:RESULT : PROCESSING ");

                        if (Constants.SIMULATE_BOOKING)
                            UberManager.getInstance(ConfirmBookingActivity.this).putUberSandboxRideRequestStatus(rideRequest.getRequest_id(), UberStatus.ACCEPTED);

                        if(mActivityHandler !=null) {
                            Message msg = mActivityHandler.obtainMessage(MSG_RIDE_STATUS);
                            Bundle b = new Bundle();
                            b.putString("req", rideRequest.getRequest_id());
                            msg.setData(b);
                            mActivityHandler.sendMessageDelayed(msg, WAIT_TIMER);
                        }
                        else
                            Log.e(TAG,"Something really weird here");

                    }else{
                        Log.d(TAG, "RESPONSE: rideRequestDetails:Success =" + rideRequest.toString());
                        dismissProgressDialog(ConfirmBookingActivity.this.getSupportFragmentManager());
                        if(Constants.ENABLE_DEBUG_TOAST)
                            Toast.makeText(ConfirmBookingActivity.this, "BOOKING SUCCESS:\n"+rideRequest.toString(), Toast.LENGTH_LONG).show();
                        JavaUtil.printHTTPResponse(response);

                        adaptUberMyBooking(rideRequest);

                        insertBookingInfo();

                        movetoTrackingPage();
                    }

                }
                else {
                    Toast.makeText(ConfirmBookingActivity.this, "Request Id is not valid :" + rideRequest.getStatus(), Toast.LENGTH_LONG).show();
                    dismissProgressDialog(ConfirmBookingActivity.this.getSupportFragmentManager());
                }
            }else{
                //FIXME add mapping for other codes and show dialog here.
                dismissProgressDialog(ConfirmBookingActivity.this.getSupportFragmentManager());
                Toast.makeText(ConfirmBookingActivity.this,"rideRequest Request Id Id is null" , Toast.LENGTH_LONG).show();
            }

        }

        @Override
        public void failure(RetrofitError error) {
            dismissProgressDialog(ConfirmBookingActivity.this.getSupportFragmentManager());
            Log.e(TAG,"MESSAGE="+error.toString());
            Log.e(TAG,"MESSAGE RESPONSE="+error.getResponse());

            ErrorResponse errorRideResponse=null;
            List<Error> errors=null;
            try {
                errorRideResponse=(ErrorResponse)error.getBodyAs(ErrorResponse.class);
                errors=errorRideResponse.getErrors();
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (errors!=null && errors.size()>0){
                Error responseError=errors.get(0);
                if(responseError!=null && responseError.getCode().contains("insufficient_balance")){//TODO HANDLE ERROR MODEL CLASS
                    showCustomDialog(DIALOG_NO_FUNDS,"");
                }
                else if(responseError!=null && responseError.getCode().contains("surge")){
                    Meta meta=errorRideResponse.getMeta();
                    Log.d(TAG,"Surge Data:"+meta.getSurgeConfirmation().getHref());
                    Intent intent = new Intent(ConfirmBookingActivity.this, WebviewActivity.class);
                    intent.putExtra(WebviewActivityFragment.INTENT_STRING_REQUEST_URL_TYPE, WebviewActivityFragment.REQUEST_URL_TYPE_SURGE_CONFIRMATION);
                    intent.putExtra(WebviewActivityFragment.INTENT_STRING_REQUEST_URL, meta.getSurgeConfirmation().getHref());
                    startActivityForResult(intent, REQUEST_URL_SURGE_CONFIRMATION);
                }
                else if(responseError.getCode().contains(UberStatus.NO_DRIVERS_AVAILABLE.value())){
                    showCustomDialog(DIALOG_NO_DRIVER_AVAILABLE,"");
                }
                else
                {
                    //Any other error please show appropriate error informative dialog
                    showCustomDialog(DIALOG_GENERIC_ERROR, error.getMessage());
                }
            }

            //Toast.makeText(ConfirmBookingActivity.this, error.getBody().toString(), Toast.LENGTH_LONG).show();
        }
    };

    void bookUberCab(String productId, String surgeConfirmationId)
    {
      CabApiClientFactory.getCabProvider(CabApiClientFactory.PROVIDER_UBER).bookCab(productId,surgeConfirmationId,"" ,MapCabFinder.PIN_LATITUDE,MapCabFinder.PIN_LONGITUDE,callbackRideRequest);

        /**
        surgeConfirmationId=surgeConfirmId;;
        UberRequestBody uberRequestBody = new UberRequestBody(productId, MapCabFinder.PIN_LATITUDE, MapCabFinder.PIN_LONGITUDE, surgeConfirmationId);

        if (Constants.SIMULATE_BOOKING) {
            UberAPIClient.getUberV1SandBoxAPIClient().rideRequest(getAccessToken(Constants.CAB_GLOBAL.UBER), uberRequestBody, callbackRideRequest);
        }
        else{
            UberAPIClient.getUberV1APIClient().rideRequest(getAccessToken(Constants.CAB_GLOBAL.UBER), uberRequestBody, callbackRideRequest);
        }
       **/
    }

    @Override
    public void onPositiveButtonClicked(int reqCode) {

        switch (reqCode)
        {
            case DIALOG_NO_FUNDS:
                //Take user to paytm app.
                if(AndroidUtils.appInstalledOrNot("net.one97.paytm", getApplicationContext()))
                {
                    PackageManager pm = this.getPackageManager();
                    Intent intent=pm.getLaunchIntentForPackage("net.one97.paytm");
                    startActivity(intent);
                }
                else
                {
                    //Open market link to download PAytm.
                    // open market to download OLA money
                    final String appPackageName = "net.one97.paytm"; // getPackageName() from Context or Activity object
                    try {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                    } catch (android.content.ActivityNotFoundException anfe) {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                    }
                }

                break;
            case DIALOG_CONFIRM_SURGE:

                //FIXME Take user to booking here
                Toast.makeText(this,"Prakhyath add code here!!", Toast.LENGTH_LONG).show();


                break;

        }



        super.onPositiveButtonClicked(reqCode);
    }


    @Override
    public void onNegativeButtonClicked(int reqCode) {
        super.onNegativeButtonClicked(reqCode);
    }

    public static final int DIALOG_NO_CONNECTIVITY = 1012;
    public static final int DIALOG_NO_FUNDS= 1013;
    public static final int DIALOG_CONFIRM_SURGE = 1014;
    public static final int DIALOG_NO_DRIVER_AVAILABLE = 1016;
    public static final int DIALOG_GENERIC_ERROR = 1015;


    public static final String DIALOG_TAG = "ConfirmBooking";


    /**
     * Public api to show different dialogs
     * @param id
     */
    public void showCustomDialog(int id, String message) {

        SimpleDialogFragment.SimpleDialogBuilder builder = SimpleDialogFragment.createBuilder(this
                , this.getSupportFragmentManager());
        dismissProgressDialog(this.getSupportFragmentManager());
        switch (id) {
            case DIALOG_NO_CONNECTIVITY:
                builder.setTitle("No Netowork");
                builder.setMessage("You are not connected to internet");
                builder.setPositiveButtonText("OK");
                builder.setCancelableOnTouchOutside(false);

                break;

            case DIALOG_NO_FUNDS:
                builder.setTitle("Insufficient Funds");
                builder.setMessage("You don't seem to have Enough Funds!! Add funds through Paytm");
                builder.setPositiveButtonText("PAYTM");
                builder.setCancelableOnTouchOutside(false);

                break;

            case DIALOG_CONFIRM_SURGE:
                builder.setTitle("Confirm Surge Price");
                builder.setMessage("The fares have gone up by Surge factor, you total fare gets multipled by surge factor");
                builder.setPositiveButtonText("CONFIRM");
                builder.setCancelableOnTouchOutside(false);
                break;

            case DIALOG_NO_DRIVER_AVAILABLE:
                builder.setTitle("No Drivers Available");
                builder.setMessage("Sorry, we're working real hard to add more cars. Please try us again soon.");
                builder.setPositiveButtonText("OK");
                builder.setCancelableOnTouchOutside(false);

                break;

            case DIALOG_GENERIC_ERROR:
                builder.setTitle("Booking Error!!");
                builder.setMessage(message);
                builder.setPositiveButtonText("OK");
                builder.setCancelableOnTouchOutside(false);


            default:
                android.util.Log.e(TAG, "Dialog id not found");
                return;
        }
        if (mIsRunning) {
            builder.setRequestCode(id).setTag(DIALOG_TAG).show();
        }
        else
            android.util.Log.e(TAG, "Not able to show dialog here :-( with id " + id);


    }



    private void adaptUberMyBooking(RideRequest rideRequest){
        mMyBooking=new MyBooking();

        if(rideRequest!=null) {
            mMyBooking.crn = rideRequest.getRequest_id();
            if (rideRequest.getDriver() != null) {
                mMyBooking.driver_name = rideRequest.getDriver().name;
                mMyBooking.cab_number = rideRequest.getDriver().phone_number;
            }

            if (rideRequest.getVehicle() != null) {
                mMyBooking.cab_type = rideRequest.getVehicle().make;//TODO CORRECT THIS
                mMyBooking.cab_number = rideRequest.getVehicle().license_plate;
                mMyBooking.car_model = rideRequest.getVehicle().model;
            } else
                mMyBooking.cab_type = "SEDAN";//TODO CORRECT THIS

            if (rideRequest.getLocation() != null)
                mMyBooking.driverLocation=new MapPoint(rideRequest.getLocation().latitude, rideRequest.getLocation().longitude);

            if(rideRequest.getDriver()!=null) {
                if(rideRequest.getDriver().picture_url!=null)
                    mMyBooking.chauffeurImageUrl = rideRequest.getDriver().picture_url;
                if(rideRequest.getDriver().phone_number!=null && !rideRequest.getDriver().phone_number.equals(""))
                    mMyBooking.driver_number=rideRequest.getDriver().phone_number;
            }

            mMyBooking.eta=rideRequest.getEta();
            mMyBooking.booking_status=rideRequest.getStatus();//TODO INSERT CORRECT DISPLAY STRING
        }

        mMyBooking.pickupLoction=new MapPoint(PIN_LATITUDE,PIN_LONGITUDE);
        mMyBooking.pickUpAddress= estimate.address;
        mMyBooking.cabCompany= Constants.CAB_GLOBAL.UBER.toString();

        Log.d(TAG,"Uber: Mybooking="+mMyBooking.toString());
    }

    void launchUberApp()
    {
        try {
            PackageManager pm = getPackageManager();
            pm.getPackageInfo("com.ubercab", PackageManager.GET_ACTIVITIES);
            String uri =buildUberAppUrl();
            //"uber://?action=setPickup&pickup=my_location&client_id=YOUR_CLIENT_ID";
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(uri));
            startActivity(intent);
        } catch (PackageManager.NameNotFoundException e) {
            // No Uber app! Open mobile website.
            //String url = "https://m.uber.com/sign-up?client_id="+Constants.getUberClientId(this);
            String url=buildUberMobileSiteUrl();
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            startActivity(i);
        }
    }

    private String buildUberAppUrl() {
        Uri.Builder uriBuilder = Uri.parse("uber://").buildUpon();
        uriBuilder.appendQueryParameter("action", "setPickup");
        uriBuilder.appendQueryParameter("client_id", Constants.getUberClientId(this));
        uriBuilder.appendQueryParameter("pickup[latitude]", String.valueOf(MapCabFinder.PIN_LATITUDE));
        uriBuilder.appendQueryParameter("pickup[longitude]", String.valueOf(MapCabFinder.PIN_LONGITUDE));
        uriBuilder.appendQueryParameter("pickup[formatted_address]", estimate.address);
        //uriBuilder.appendQueryParameter("dropoff[latitude]", String.valueOf(MapCabFinder.PIN_LATITUDE));
        //uriBuilder.appendQueryParameter("dropoff[longitude]", String.valueOf(MapCabFinder.PIN_LONGITUDE));
        //uriBuilder.appendQueryParameter("dropoff[formatted_address]", "1%20Telegraph%20Hill%20Blvd%2C%20San%20Francisco%2C%20CA%2094133");
        uriBuilder.appendQueryParameter("product_id", estimate.id);
        android.util.Log.d(TAG, "URL====" + uriBuilder.toString());
        return uriBuilder.build().toString().replace("%20", "+");
    }

    private String buildUberMobileSiteUrl() {
        Uri.Builder uriBuilder = Uri.parse("https://m.uber.com/sign-up").buildUpon();
        uriBuilder.appendQueryParameter("action", "setPickup");
        uriBuilder.appendQueryParameter("client_id", Constants.getUberClientId(this));
        uriBuilder.appendQueryParameter("pickup_latitude", String.valueOf(MapCabFinder.PIN_LATITUDE));
        uriBuilder.appendQueryParameter("pickup_longitude", String.valueOf(MapCabFinder.PIN_LONGITUDE));
        uriBuilder.appendQueryParameter("pickup_address", estimate.address);
        //uriBuilder.appendQueryParameter("dropoff_latitude", String.valueOf(MapCabFinder.PIN_LATITUDE));
        //uriBuilder.appendQueryParameter("dropoff_longitude", String.valueOf(MapCabFinder.PIN_LONGITUDE));
        //uriBuilder.appendQueryParameter("dropoff[formatted_address]", "1%20Telegraph%20Hill%20Blvd%2C%20San%20Francisco%2C%20CA%2094133");
        uriBuilder.appendQueryParameter("product_id", estimate.id);
        android.util.Log.d(TAG, "URL====" + uriBuilder.toString());
        return uriBuilder.build().toString().replace("%20", "+");
    }


    void insertBookingInfo(){

        if(mMyBooking==null)
            return;

        //mMyBooking.pickUpAddress= mPickUpAddress;
        //mMyBooking.booking_status=mTrackRide.booking_status;
        mMyBooking.pickupTime=(System.currentTimeMillis()/1000);

        try {
            DatabaseUtil.insertRideInfo(ConfirmBookingActivity.this, mMyBooking);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        //Log cab data to backend
        MyEventManager.getInstance().logCabData(mMyBooking.crn, mMyBooking.driver_name,
                mMyBooking.driver_number, mMyBooking.cab_number, mMyBooking.cab_type, mMyBooking.cabCompany);//TODO correct hard coding
    }

    private void clearBookingDetails(){
        mRideBooking=null;
        mMyBooking=null;
        mTrackRide=null;
    }

    void updateBookingDetails(RideBooking rideBooking){
        if(rideBooking!=null){

            this.mRideBooking =rideBooking;

            tvCabType.setText(String.valueOf(rideBooking.cab_type));
            tvCabETA.setText(rideBooking.eta+" "+R.string.mins);

            tv3label.setText(R.string.drivername);
            tv3value.setText(rideBooking.driver_name);

            tv4label.setText(R.string.driverno);
            tv4value.setText(String.valueOf(rideBooking.driver_number));

            //llSection5.setVisibility(View.VISIBLE);
            tv5value.setText(rideBooking.car_model+",\n "+rideBooking.cab_number);

            tvBookingLabel.setText(R.string.bookingconfirmed);
            tvBookCabLabel.setText("");

            llBookCab.setVisibility(View.GONE);
        }
    }
}
