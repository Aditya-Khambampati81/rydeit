package com.rydeit.view;

import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.toolbox.NetworkImageView;
import com.avast.android.dialogs.fragment.SimpleDialogFragment;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.rydeit.R;
import com.rydeit.api.ola.OlaCallback;
import com.rydeit.api.uber.UberCallback;
import com.rydeit.cab.service.ola.OlaAPIConstants;
import com.rydeit.cab.service.ola.OlaSandBox;
import com.rydeit.database.DatabaseUtil;
import com.rydeit.database.GenericRydeState;
import com.rydeit.model.common.MapPoint;
import com.rydeit.model.common.MyBooking;
import com.rydeit.model.ola.CancelResponse;
import com.rydeit.model.ola.TrackRide;
import com.rydeit.model.uber.Requests.ErrorResponses.Error;
import com.rydeit.model.uber.Requests.ErrorResponses.ErrorResponse;
import com.rydeit.model.uber.Requests.UberStatus;
import com.rydeit.model.uber.RideRequest;
import com.rydeit.provider.CabApiClientFactory;
import com.rydeit.uilibrary.BaseActivityActionBar;
import com.rydeit.util.Constants;
import com.rydeit.util.JavaUtil;
import com.rydeit.util.Log;
import com.rydeit.volley.VolleySingleton;

import java.util.HashMap;
import java.util.List;

import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by Prakhyath on 11/10/2015.
 */
public class TrackMyRideActivity extends BaseActivityActionBar implements OnMapReadyCallback {

    private static final String TAG = TrackMyRideActivity.class.getSimpleName();

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    LatLngBounds.Builder mLatLngBuilder = new LatLngBounds.Builder();

    NetworkImageView chauffeurImage;
    TextView tvDriverName;
    TextView tvCarModel;
    TextView tvCarRegNo;

    LinearLayout ll_Calldriver;
    LinearLayout ll_sharedetails;
    LinearLayout ll_cancelride;

    MyBooking mMyBooking;
    TrackRide mTrackRide;//OLA
    private String booking_status_previous;

    String AccessToken=ConfirmBookingActivity.AccessToken;
    String TokenType=ConfirmBookingActivity.TokenType;

    private int ACTIVE_DIALOG_ID = 0;
    private boolean dialogIsRunning = false;
    private String globalMessage = "";

    //String mPickUpAddress;

    boolean isNewBooking=false;
    boolean bIsRefreshBooking=false;
    private static final int TRACKPAGE_REFRESH_TIME=30000;
    private static final int MSG_SIMULATE_OLA_COMPLETION = 9999;
    private static final int MSG_SIMULATE_OLA_CLIENT_LOCATED = 9998;
    private static final int MSG_SIMULATE_OLA_IN_PROGRESS = 9997;
    private static final int MSG_SIMULATE_OLA_CALL_DRIVER=9996;



    // no need to create handler here use handler in base calss
   // private Handler mHandler = new Handler();
    private boolean mForceStopTimer=false;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        setHomeDisabled(true);
        super.onCreate(savedInstanceState);
        if(getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowHomeEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            setTitle(getString(R.string.trackride));
            //getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.splash_yellow)));
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.left_arrow_white1);
        }

        /*if(getIntent().hasExtra("PickUpAddress"))
           mPickUpAddress=getIntent().getStringExtra("PickUpAddress");*/

        if(getIntent().hasExtra("TrackRideInProgress")){
            mMyBooking=(MyBooking)getIntent().getSerializableExtra("TrackRideInProgress");
            Log.d(TAG,"BOOKING DETAILS: "+mMyBooking.toString());
        }
        else
            Log.e(TAG,"Invalid Booking");

        if(getIntent().hasExtra("isNewBooking")){
            isNewBooking=true;
        }

        setContentView(R.layout.activity_trackmyride);

        initViews();

        updateBookingDetails();

        setUpMapIfNeeded();

    }

    void initViews(){
        chauffeurImage=(NetworkImageView)findViewById(R.id.chauffeur_image);
        tvDriverName=(TextView)findViewById(R.id.drivername);
        tvCarModel=(TextView)findViewById(R.id.carmodel);
        tvCarRegNo=(TextView)findViewById(R.id.carregno);

        ll_Calldriver=(LinearLayout)findViewById(R.id.ll_calldriver);
        ll_sharedetails=(LinearLayout)findViewById(R.id.ll_sharedetails);
        ll_cancelride=(LinearLayout)findViewById(R.id.ll_cancelride);

        ll_cancelride.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                showCustomDialog(DIALOG_CONFIRM_CANCELLATION, "");


            }
        });

        ll_Calldriver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callDriver(mMyBooking.driver_number);
            }
        });

        ll_sharedetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.putExtra(Intent.EXTRA_TEXT, "I would like to share my cab ride details with you" + mMyBooking.printCustom());
                shareIntent.setType("text/plain");
                TrackMyRideActivity.this.startActivity(shareIntent);
            }

        });

    }

    void CancelBooking(){
        if(mMyBooking!=null && mMyBooking.cabCompany.equalsIgnoreCase(Constants.CAB_GLOBAL.UBER.toString())){
            cancelUberBooking();
        }else if(mMyBooking!=null && mMyBooking.cabCompany.equalsIgnoreCase(Constants.CAB_INDIA.OLA.toString())){
            cancelOlaBooking();
        }
    }

    private final int DIALOG_NO_CONNECTIVITY = 102;
    private final int DIALOG_CONFIRM_CANCELLATION = 103;
    private final String DIALOG_TAG = "DIALOG_TAG";
    private final int DIALOG_CANCEL_FAILED = 104;
    private final int MSG_UBER_CANCEL_SUCCESS = 201;
    private final int MSG_OLA_CANCEL_SUCCESS = 202;

    public static final int DIALOG_RIDE_COMPLETED = 1017;
    public static final int DIALOG_RIDE_TRACKING_NOT_VALID = 1018;

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
                builder.setTitle("No Network");
                builder.setMessage("You are not connected to internet");
                builder.setPositiveButtonText("OK");
                builder.setCancelableOnTouchOutside(false);

                break;
            case DIALOG_CONFIRM_CANCELLATION:
                builder.setTitle("Cancel Ride");
                builder.setMessage("Are you sure you want to Cancel Booking?");
                builder.setPositiveButtonText("OK");
                builder.setNegativeButtonText("CANCEL");
                builder.setCancelableOnTouchOutside(false);
                break;
            case DIALOG_CANCEL_FAILED:
                builder.setTitle("Cancellation Failed=");
                builder.setMessage(message);
                builder.setPositiveButtonText("OK");
                break;
            case DIALOG_RIDE_TRACKING_NOT_VALID:
                builder.setTitle("Not a valid Ride");
                builder.setMessage("This ride is already Cancelled or Completed. Thanks you.");
                builder.setPositiveButtonText("OK");
                break;
            case DIALOG_RIDE_COMPLETED:
                builder.setTitle("Ride Completed");
                if(message != null && !message.isEmpty())
                {
                    builder.setMessage(message);
                }
                else
                    builder.setMessage("Your Ride is completed. \n Kindly check your email for receipt.");
                builder.setPositiveButtonText("OK");
                builder.setCancelableOnTouchOutside(false);
                break;
        }
            if (mIsRunning) {

                builder.setRequestCode(id).setTag(DIALOG_TAG).show();
                ACTIVE_DIALOG_ID = id;
                globalMessage = message;

            }
            else
                android.util.Log.e(TAG, "Not able to show dialog here :-( with id " + id);

        }
    void cancelOlaBooking(){

        if(mMyBooking!=null && mMyBooking.crn!=null) {
//            OlaAPIClient.getOlaV1APIClient().cancelRide(OlaAPIConstants.getOlaXAppToken(this),
//                    TokenType + " " + AccessToken,
//                    mMyBooking.crn,
              CabApiClientFactory.getCabProvider(CabApiClientFactory.PROVIDER_OLA).cancelRide(mMyBooking.crn,TokenType,AccessToken,
                    new OlaCallback<CancelResponse>() {
                        @Override
                        public void success(CancelResponse cancelResponse, Response response) {
                            Toast.makeText(TrackMyRideActivity.this, "Booking is Cancelled", Toast.LENGTH_SHORT).show();
                            ContentValues cv = new ContentValues();
                            cv.put("BOOKING_STATUS", GenericRydeState.CANCELLED);
                            try {
                                DatabaseUtil.updateRideStatus(TrackMyRideActivity.this, cv, mMyBooking.crn);
                            } catch (IllegalAccessException e) {
                                e.printStackTrace();
                            }
                            if (mActivityHandler != null)
                                mActivityHandler.sendMessage(mActivityHandler.obtainMessage(MSG_OLA_CANCEL_SUCCESS));
                        }

                        @Override
                        public void failure(RetrofitError error) {
                            Log.d(TAG, "CANCEL BOOKING: errro:" + error.getResponse());

                            showCustomDialog(DIALOG_CANCEL_FAILED, error.getMessage());
                            super.failure(error);

                        }

                    }


            );
        }
        else{
            Toast.makeText(this,"Invalid Booking ID, Cannot be cancelled",Toast.LENGTH_LONG).show();
        }

    }

    void cancelUberBooking(){

        if(mMyBooking!=null && mMyBooking.crn!=null) {
            //            UberAPIClient.getUberV1APIClient().deleteRequest(CabTracker.getInstance().getAccessToken(Constants.CAB_GLOBAL.UBER),
//                    mMyBooking.crn,

            CabApiClientFactory.getCabProvider(CabApiClientFactory.PROVIDER_UBER).cancelRide(mMyBooking.crn ,"","",
                new UberCallback<Response>() {
                        @Override
                        public void success(Response response, Response response2) {

                            Log.d(TAG,"CANCEL BOOKING: status:"+response.getStatus());
                            Toast.makeText(TrackMyRideActivity.this,"Booking is Cancelled",Toast.LENGTH_SHORT).show();

                            //Updating DB post cancellation ride status has to be updated to Cancelled?
                            ContentValues cv = new ContentValues();
                            cv.put("BOOKING_STATUS","CANCELLED");
                            try {
                                DatabaseUtil.updateRideStatus(TrackMyRideActivity.this, cv, mMyBooking.crn);
                            } catch (IllegalAccessException e) {
                                e.printStackTrace();
                            }
                            if(mActivityHandler!=null)
                                 mActivityHandler.sendMessage(mActivityHandler.obtainMessage(MSG_UBER_CANCEL_SUCCESS));

                        }

                        @Override
                        public void failure(RetrofitError error) {
                            Log.d(TAG,"CANCEL BOOKING: errro:"+error.getResponse());

                            showCustomDialog(DIALOG_CANCEL_FAILED, error.getMessage());
                            super.failure(error);

                        }
                    });
        }
        else{
            Toast.makeText(this, "Invalid Booking ID, Cannot be cancelled", Toast.LENGTH_LONG).show();
        }

    }

    boolean doubleBackToExitPressedOnce = false;
    private final int MSG_BACK_EXIT = 1010;
    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Please click back again to exit", Toast.LENGTH_SHORT).show();
        if(mActivityHandler != null)
        mActivityHandler.sendMessageDelayed(mActivityHandler.obtainMessage(MSG_BACK_EXIT), 2000);

    }

    void callDriver(String phoneno){
        Intent intent = new Intent(Intent.ACTION_CALL);

        intent.setData(Uri.parse("tel:" + phoneno));
        startActivity(intent);
    }

    void updateBookingDetails(){
        if(mMyBooking!=null){

            tvDriverName.setText(mMyBooking.driver_name);
            tvCarModel.setText(mMyBooking.car_model);
            tvCarRegNo.setText(mMyBooking.cab_number);

            if(mMyBooking.chauffeurImageUrl!=null)
                chauffeurImage.setImageUrl(mMyBooking.chauffeurImageUrl, VolleySingleton.getInstance().getImageLoader());
            else
                chauffeurImage.setDefaultImageResId(R.drawable.chauffeur_icon);

            //We are hardcoding to OLA lets make sure that

        }
    }

    void trackMyBooking(){

        if(mMyBooking!=null && mMyBooking.cabCompany.equalsIgnoreCase(Constants.CAB_GLOBAL.UBER.toString())){
           // CabTracker.getInstance().trackUberBooking(mMyBooking.crn, callbackRideRequest);
            CabApiClientFactory.getCabProvider(CabApiClientFactory.PROVIDER_UBER).getRideStatus(mMyBooking.crn,callbackRideRequest);
        }else if(mMyBooking!=null && mMyBooking.cabCompany.equalsIgnoreCase(Constants.CAB_INDIA.OLA.toString())){
            trackOlaBooking();
        }
    }





    void trackOlaBooking(){

        if(Constants.SIMULATE_BOOKING || (mMyBooking!=null && mMyBooking.booking_status!=null
                && mMyBooking.booking_status.equals("SIMULATION"))){
            Log.d(TAG, "SIMULATION MODE: TRACK CAB");

            if(booking_status_previous==null) {
                mTrackRide = OlaSandBox.getSimulatedClientLocated();
                olaCallback.success(mTrackRide, null);
            }else if(booking_status_previous.equals((OlaAPIConstants.BOOKING_STATUS.CLIENT_LOCATED))) {
                mActivityHandler.sendMessageDelayed(mActivityHandler.obtainMessage(MSG_SIMULATE_OLA_CALL_DRIVER), 1000);
            }else if(booking_status_previous.equals((OlaAPIConstants.BOOKING_STATUS.CALL_DRIVER))) {
                mActivityHandler.sendMessageDelayed(mActivityHandler.obtainMessage(MSG_SIMULATE_OLA_IN_PROGRESS), 1000);
            }else if(booking_status_previous.equals((OlaAPIConstants.BOOKING_STATUS.IN_PROGRESS)))
                mActivityHandler.sendMessageDelayed(mActivityHandler.obtainMessage(MSG_SIMULATE_OLA_COMPLETION), 1000);
            /*if(isNewBooking) {
                insertBookingInfo();
                isNewBooking=false;
            }*/
            return;
        }

        CabApiClientFactory.getCabProvider(CabApiClientFactory.PROVIDER_OLA).getRideStatus(null, olaCallback);
        //CabTracker.getInstance().trackOlaBooking(olaCallback, AccessToken, TokenType);

    }


    private OlaCallback<TrackRide>   olaCallback = new OlaCallback<TrackRide>() {
        @Override
        public void success(TrackRide trackRide, Response response) {
            if (trackRide != null && trackRide.duration != null && Constants.ENABLE_DEBUG_TOAST)
                Toast.makeText(TrackMyRideActivity.this, "Status:\"+trackRide.status+\"\n" +
                        " Booking Status:" + trackRide.booking_status + "+ \nCar Arrival Time:" + trackRide.duration.value, Toast.LENGTH_LONG).show();
            else if (trackRide != null && Constants.ENABLE_DEBUG_TOAST)
                Toast.makeText(TrackMyRideActivity.this, "Status:" + trackRide.status + "\n Booking Status:" + trackRide.booking_status, Toast.LENGTH_LONG).show();

            //proper shallow copy
            mTrackRide = (TrackRide) trackRide.clone();

            Log.d(TAG, "TRACK RIDE Details: " + mTrackRide.toString());

            if (mTrackRide.booking_status.equals(OlaAPIConstants.BOOKING_STATUS.NO_BOOKING)){
                showCustomDialog(DIALOG_RIDE_TRACKING_NOT_VALID, null);
            }else if (mTrackRide.booking_status.equals(OlaAPIConstants.BOOKING_STATUS.COMPLETED)){
                String displayBill = " Total amount is Rs :" + trackRide.trip_info.amount + "\n Ola cash balance :"+ trackRide.ola_money_balance +  "\n Payable amount :"+ trackRide.trip_info.payable_amount;
                showCustomDialog(DIALOG_RIDE_COMPLETED, displayBill);
                ll_cancelride.setVisibility(View.INVISIBLE);

            }else if (mTrackRide.booking_status.equals(OlaAPIConstants.BOOKING_STATUS.IN_PROGRESS)){
                ll_cancelride.setVisibility(View.INVISIBLE);

                if(booking_status_previous==null || booking_status_previous.equals(OlaAPIConstants.BOOKING_STATUS.CLIENT_LOCATED))
                    Toast.makeText(TrackMyRideActivity.this,"Have a pleasant Ride",Toast.LENGTH_LONG).show();

            }else if (mTrackRide.booking_status.equals(OlaAPIConstants.BOOKING_STATUS.CLIENT_LOCATED)
                    || mTrackRide.booking_status.equals(OlaAPIConstants.BOOKING_STATUS.CALL_DRIVER)){
                ll_cancelride.setVisibility(View.VISIBLE);
            }else
                ll_cancelride.setVisibility(View.INVISIBLE);

            booking_status_previous=mTrackRide.booking_status;
            Log.d(TAG, "DRIVER POSITION: lat:" + mTrackRide.driver_lat + " log:" + mTrackRide.driver_lng);

            setUpMap();

        }
    };







    private UberCallback<RideRequest> callbackRideRequest = new UberCallback<RideRequest>()
    {
        @Override
        public void success(RideRequest rideRequest, Response response) {
            dismissProgressDialog(TrackMyRideActivity.this.getSupportFragmentManager());

            if(rideRequest==null && Constants.ENABLE_DEBUG_TOAST){
                Toast.makeText(TrackMyRideActivity.this, "RESPONSE: RideRequest is null", Toast.LENGTH_LONG).show();
                return;
            }

            if(rideRequest!=null && rideRequest.getRequest_id()!=null) {

                if (rideRequest.getStatus().equalsIgnoreCase(UberStatus.PROCESSING.toString())
                        || rideRequest.getStatus().equalsIgnoreCase(UberStatus.ARRIVING.value().toString())
                        || rideRequest.getStatus().equalsIgnoreCase(UberStatus.ACCEPTED.value().toString())
                        || rideRequest.getStatus().equalsIgnoreCase(UberStatus.IN_PROGRESS.value().toString())
                        || rideRequest.getStatus().equalsIgnoreCase(UberStatus.RIDER_CANCELED.value().toString())) {

                    if (rideRequest.getStatus().equalsIgnoreCase(UberStatus.ARRIVING.toString()))
                        Toast.makeText(TrackMyRideActivity.this, "Your cab has arrived", Toast.LENGTH_LONG).show();


                    Log.d(TAG, "REQUEST ID: " + rideRequest.getRequest_id());
                    if(Constants.ENABLE_DEBUG_TOAST)
                        Toast.makeText(TrackMyRideActivity.this, "TRACK BOOKING SUCCESS\n"+rideRequest.toString(), Toast.LENGTH_SHORT).show();

                    JavaUtil.printHTTPResponse(response);

                    mTrackRide=new TrackRide();//TODO FIXME IMPLEMENT COMMON MODEL CLASS
                    if(rideRequest.getLocation()!=null) {
                        mTrackRide.driver_lat = rideRequest.getLocation().latitude;
                        mTrackRide.driver_lng = rideRequest.getLocation().longitude;
                    }

                    adaptUberMyBooking(rideRequest);
                    updateBookingDetails();

                    setUpMap();

                }else if (rideRequest.getStatus().equalsIgnoreCase(UberStatus.COMPLETED.toString())){
                    Toast.makeText(TrackMyRideActivity.this,"Your Ride is completed. Kindly check your email :"+rideRequest.getRequest_id() , Toast.LENGTH_LONG).show();
                    showCustomDialog(DIALOG_RIDE_COMPLETED,null);
                }

            }
            else{
                Toast.makeText(TrackMyRideActivity.this,"rideRequest Request Id Id is null" , Toast.LENGTH_LONG).show();
            }

        }

        @Override
        public void failure(RetrofitError error) {
            dismissProgressDialog(TrackMyRideActivity.this.getSupportFragmentManager());
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

            //TODO THESE ERROS NEED NOT BE HANDLED HERE
            //TODO HANDLE RIDE TRACKING STATUS HERE
            if (errors!=null && errors.size()>0){
                Error responseError=errors.get(0);
                if(responseError!=null && responseError.getCode().contains("insufficient_balance")){
                    //showCustomDialog(DIALOG_NO_FUNDS);
                }
            }

            if(Constants.ENABLE_DEBUG_TOAST)
                Toast.makeText(TrackMyRideActivity.this, error.getBody().toString(), Toast.LENGTH_LONG).show();
        }
    };



    private void adaptUberMyBooking(RideRequest rideRequest){
        if(mMyBooking==null)
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
        mMyBooking.cabCompany= Constants.CAB_GLOBAL.UBER.toString();

        Log.d(TAG, "Uber: Mybooking=" + mMyBooking.toString());
    }

    private void initRefreshTrackPageTimer(){
        mForceStopTimer=false;

        Runnable mRunnableTimer = new Runnable() {
            @Override
            public void run() {

                if(mForceStopTimer)
                    return;

                bIsRefreshBooking=true;
                trackMyBooking();
                mActivityHandler.postDelayed(this, TRACKPAGE_REFRESH_TIME);
            }
        };
        mActivityHandler.postDelayed(mRunnableTimer, 0);
    }

    private void stopRefreshTrackPageTimer(){
        mForceStopTimer=true;

        if(mActivityHandler!=null)
            mActivityHandler.removeCallbacks(null);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        if (mMap == null)
            setUpMapIfNeeded();
        else
            setUpMap();
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link com.google.android.gms.maps.SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {
        if(mMap==null)
            return;
       mMap.clear();;
        mLatLngBuilder=new LatLngBounds.Builder();

        if(mMyBooking!=null && mMyBooking.pickupLoction!=null) {
            LatLng latLng = new LatLng(mMyBooking.pickupLoction.getLattitude(), mMyBooking.pickupLoction.getLongitude());
            mMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.drawable.pin))
                    .position(latLng));
            if(!bIsRefreshBooking)
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng ,14.0f) );
            mLatLngBuilder.include(latLng);
        }

        if(mTrackRide!=null) {
            mMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.drawable.cab_tracking))
                    .position(new LatLng(mTrackRide.driver_lat, mTrackRide.driver_lng)));
            mLatLngBuilder.include(new LatLng(mTrackRide.driver_lat, mTrackRide.driver_lng));
        }

        Log.d(TAG, "MAP DRAWING: mMyBooking:"+mMyBooking.toString());
        if(mTrackRide!=null)
            Log.d(TAG, "MAP DRAWING: mTrackRide:"+mTrackRide.toString());

        fixZoom();
    }

    private void fixZoom() {
        mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                //mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(mLatLngBuilder.build(), 50));
                if (mMap == null || mLatLngBuilder.build() == null)
                    return;
                mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(mLatLngBuilder.build(), 150));
            }
        });
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
            case MSG_OLA_CANCEL_SUCCESS:
            case MSG_UBER_CANCEL_SUCCESS:
                finish();
                break;
            case MSG_BACK_EXIT:
                doubleBackToExitPressedOnce=false;
                break;
            case MSG_SIMULATE_OLA_COMPLETION:

                olaCallback.success(OlaSandBox.getSimulatedTripEnded(), null);
                break;
            case MSG_SIMULATE_OLA_CLIENT_LOCATED:

                olaCallback.success(OlaSandBox.getSimulatedClientLocated(), null);
                break;

            case MSG_SIMULATE_OLA_CALL_DRIVER:

                olaCallback.success(OlaSandBox.getSimulatedCallDriver(), null);
                break;

            case MSG_SIMULATE_OLA_IN_PROGRESS:
                olaCallback.success(OlaSandBox.getSimulatedRideTrackObject(), null);
                break;



        }

    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        super.onNewIntent(intent);
    }



    @Override
    protected void onResume() {
        super.onResume();
        if (dialogIsRunning) {
            showCustomDialog(ACTIVE_DIALOG_ID, globalMessage);
        }
        initRefreshTrackPageTimer();




    }

    @Override
    protected void onPause() {
        super.onPause();
        DialogFragment df = (DialogFragment) getSupportFragmentManager().findFragmentByTag(DIALOG_TAG);
        if (df != null) {
            df.dismissAllowingStateLoss();
            dialogIsRunning = true;
        }
        stopRefreshTrackPageTimer();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        stopRefreshTrackPageTimer();
    }

    @Override
    public void onPositiveButtonClicked(int reqCode) {
        ACTIVE_DIALOG_ID = 0;
        dialogIsRunning = false;
        if(reqCode == DIALOG_CONFIRM_CANCELLATION)
            CancelBooking();
        else if (reqCode == DIALOG_RIDE_COMPLETED){
            updateOnCompletion();
            finish();
        }else if (reqCode == DIALOG_RIDE_TRACKING_NOT_VALID){
            try {
                if(mMyBooking!=null && mMyBooking.crn!=null)
                    DatabaseUtil.deleteRydeInfo(TrackMyRideActivity.this, mMyBooking.crn.toString());
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            finish();
        }
        super.onPositiveButtonClicked(reqCode);
    }

    @Override
    public void onNegativeButtonClicked(int reqCode) {
        ACTIVE_DIALOG_ID = 0;
        dialogIsRunning = false;
        super.onNegativeButtonClicked(reqCode);
    }

    private void updateOnCompletion()
    {
        ContentValues cv = new ContentValues();
        cv.put("BOOKING_STATUS","COMPLETED");
        try {
            DatabaseUtil.updateRideStatus(TrackMyRideActivity.this, cv, mMyBooking.crn);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

}
