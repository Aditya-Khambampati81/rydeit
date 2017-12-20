package com.rydeit.view;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.CardView;
import android.text.Editable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.RelativeSizeSpan;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.rydeit.R;
import com.rydeit.api.ola.OlaAPIClient;
import com.rydeit.api.ola.OlaCallback;
import com.rydeit.api.uber.UberAPIClient;
import com.rydeit.api.uber.UberCallback;
import com.rydeit.cab.service.uber.UberManager;
import com.rydeit.map.CustomAutoCompleteTextView;
import com.rydeit.map.GeocodeJSONParser;
import com.rydeit.map.PlaceJSONParser;
import com.rydeit.model.common.ProductDetail;
import com.rydeit.model.ola.ProductCatagory;
import com.rydeit.model.ola.RideEstimateList;
import com.rydeit.model.uber.TimeEstimate;
import com.rydeit.model.uber.TimeEstimateList;
import com.rydeit.provider.ILocationListener;
import com.rydeit.provider.LocationProvider;
import com.rydeit.view.common.CabEstimateListAdapter;
import com.rydeit.uilibrary.BaseFragment;
import com.rydeit.util.Constants;
import com.rydeit.util.Log;
import com.rydeit.cab.service.ola.OlaAPIConstants;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit.client.Response;

/**
 * A simple {@link Fragment} subclass.
 */
public class MapCabFinder extends BaseFragment implements ILocationListener , OnMapReadyCallback {

    private static final String TAG = MapsActivity.class.getSimpleName();
    public static final int PLACE_AUTOCOMPLETE_REQUEST_CODE = 121;
    private static final int REQUEST_CODE_LOCATION_SETTINGS =11;
    private long listenerId = -1;

    //TODO FIX this for selecting different coordinates other than current location
    public static double PIN_LATITUDE;
    public static double PIN_LONGITUDE;

    //TODO DROP LOCATION NEEDS TO BE HANDLED
    public static double DROP_LATITUDE=12.959172f;
    public static double DROP_LONGITUDE=77.697419f;



    TextView locationTv;

    LinearLayout ll_pickuptimetext;
    private TextView pickuptimeText;

    //LinearLayout ll_AllRydeList;
    CardView rideSuggestionsCard;
    private LinearLayout markerLayout;
    private SupportMapFragment mFragment;
    private View mRootView;

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
//
//    PlacesTask placesTask;
//    PlaceParserTask parserTask;

    private int iMinimumPickupTime=999;
    private LatLng center;

    public static final String ARG_SECTION_NUMBER = "section_number";
    private static  MapCabFinder mMapCabFinder;

    private CabEstimateListAdapter mAdapter;

    public static int CAMERA_MOVE_REACT_THRESHOLD_MS = 1000;
    public long lastCallMs = Long.MIN_VALUE;

    public static final int TRACKPAGE_REFRESH_TIME=30000;


    UberManager mUberManager;
    public static Map<String, ProductDetail> ProductDetailMap;

    private Handler mHandler = new Handler();
    Runnable mRunnableTimer;

    private boolean mForceStopTimer=false;

    public MapCabFinder() {
        // Required empty public constructor
    }

    public static MapCabFinder getInstance(int sectionNumber) {

        if(mMapCabFinder == null ) {
            mMapCabFinder = new MapCabFinder();
            mMapCabFinder.setRetainInstance(true);
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            mMapCabFinder.setArguments(args);
        }

        return mMapCabFinder;
    }

    @Override
    public void onAttach(Activity activity) {

        super.onAttach(activity);
        // This will update the title in navigation bar.
        if(activity instanceof  MapsActivity) {
            ((MapsActivity)activity).onSectionAttached(
                    getArguments().getInt(ARG_SECTION_NUMBER));
        }

    }

    public static void tearDown()
    {
        mMapCabFinder= null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mRootView = inflater.inflate(R.layout.activity_maps, container, false);

        mAdapter = new CabEstimateListAdapter(this.getActivity());

        ListView AllRydeList=(ListView)mRootView.findViewById(R.id.allryde_list_view);
        rideSuggestionsCard=(CardView) mRootView.findViewById(R.id.ride_suggestions_card);
        rideSuggestionsCard.setVisibility(View.GONE);
        listenerId = LocationProvider.getInstance().registerLocationListener(this);
        locationTv = (TextView) mRootView.findViewById(R.id.et_place);
       //markerText = (TextView) mRootView.findViewById(R.id.locationMarkertext);
        markerLayout = (LinearLayout) mRootView.findViewById(R.id.locationMarker);

        ll_pickuptimetext=(LinearLayout)mRootView.findViewById(R.id.ll_pickuptimetext);
        pickuptimeText=(TextView)mRootView.findViewById(R.id.pickuptimetext);


        //locationTv.setThreshold(3);
        locationTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                rideSuggestionsCard.setVisibility(View.GONE);
                /** Start Activity to pick place from Google search **/
                try {
                    Intent intent =
                            new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY)
                                    .build(getActivity());
                   MapCabFinder.this.getActivity().startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE);
                } catch (GooglePlayServicesRepairableException e) {
                    // TODO: Handle the error.
                } catch (GooglePlayServicesNotAvailableException e) {
                    // TODO: Handle the error.
                }

            }


        });

//        locationTv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
//                String location = locationTv.getText().toString();
//                Log.d(TAG, "Selected Location=" + location);
//
//                if (location == null || location.equals("")) {
//                    Toast.makeText(MapCabFinder.this.getActivity().getBaseContext(), "No Place is selected", Toast.LENGTH_SHORT).show();
//                    return;
//                }
//
//                HideSoftKeyBoard(locationTv);
//
//                loadLocation();
//            }
//        });

//        locationTv.setOnEditorActionListener(new EditText.OnEditorActionListener() {
//
//            @Override
//            public boolean onEditorAction(TextView v, int actionId,
//                                          KeyEvent event) {
//                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER))
//                        || (actionId == EditorInfo.IME_ACTION_DONE)) {
//
//                    HideSoftKeyBoard(v);
//
//                    loadLocation();
//                    return true;
//                }
//                return false;
//            }
//        });

        setUpMapIfNeeded();

        //checkRydeAvailaibility();

        AllRydeList.setAdapter(mAdapter);

        AllRydeList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if(mAdapter.getItemViewType(i)== CabEstimateListAdapter.TYPE_ITEM) {
                    Intent intent = new Intent(MapCabFinder.this.getActivity().getBaseContext(), ConfirmBookingActivity.class);
                    intent.putExtra("bookitem", mAdapter.getItem(i));
                    intent.putExtra("pickupLattitude",PIN_LATITUDE);
                    intent.putExtra("pickupLongitude", PIN_LONGITUDE);
                    Log.i(TAG, "Book item :" + mAdapter.getItem(i) + "lat :" + PIN_LATITUDE + " Long :" + PIN_LONGITUDE);
                    MapCabFinder.this.getActivity().startActivity(intent);
                }
            }
        });



        initRefreshRideAvailabilityTimer();

        return mRootView;

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        init();
    }

    void init(){
        ProductDetailMap=new HashMap<String, ProductDetail>();

        loadCabServices();
    }

    void loadCabServices(){
        mUberManager=UberManager.getInstance(this.getActivity());
    }

    @Override
    public void onResume() {
        super.onResume();
        //setUpMapIfNeeded();

        initRefreshRideAvailabilityTimer();
    }

    @Override
    public void onPause() {
        stopRefreshRideAvailabilityTimer();
        super.onPause();
    }

    private void initRefreshRideAvailabilityTimer(){

        mForceStopTimer=false;

        mRunnableTimer = new Runnable() {
            @Override
            public void run() {

                if(mForceStopTimer)
                    return;

                checkRydeAvailaibility();
                mHandler.postDelayed(this, TRACKPAGE_REFRESH_TIME);
            }
        };
        mHandler.postDelayed(mRunnableTimer, TRACKPAGE_REFRESH_TIME);
    }

    private void stopRefreshRideAvailabilityTimer(){
        mForceStopTimer=true;

        if(mHandler != null)
            mHandler.removeCallbacks(mRunnableTimer);
    }

    void HideSoftKeyBoard(View v)
    {
        InputMethodManager in = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        in.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }


    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
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

            //SupportMapFragment mapFragment = (SupportMapFragment) getActivity().getSupportFragmentManager().findFragmentById(R.id.map);
            SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
            mMap = mapFragment.getMap();

            // Get the button view
            View locationButton = ((View) mRootView.findViewById(0x1).getParent()).findViewById(0x2);

            //set show mycurrent location button to bottom right (as Google Maps app)
            RelativeLayout.LayoutParams rlp = (RelativeLayout.LayoutParams) locationButton.getLayoutParams();
            // position on right bottom
            rlp.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
           // rlp.addRule(RelativeLayout.ALIGN_PARENT_START, RelativeLayout.TRUE);
            rlp.setMargins(30, 150,0, 0);


            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mFragment == null) {
            mFragment = SupportMapFragment.newInstance();
            try {
                getChildFragmentManager().beginTransaction().replace(R.id.map, mFragment).commit();
            }
            catch (IllegalStateException ex)
            {
                ex.printStackTrace();
                Log.e(TAG,"Crash fix :" +ex.getMessage());
                try {
                    getChildFragmentManager().beginTransaction().replace(R.id.map, mFragment).commitAllowingStateLoss();
                }
                catch (Exception ex2)
                {
                    Log.e(TAG,"Crash fix :" +ex2.getMessage());

                }
            }
            try {
                mFragment.getMapAsync(this);
            }
            catch (Exception ex)
            {
                ex.printStackTrace();
            }
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    //TODO do this on  onConnected(GooglePlayServicesClient.ConnectionCallbacks)
    private void setUpMap() {
        //mMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Marker"));

        mMap.setMyLocationEnabled(true);
        //LocationManager locationManager = (LocationManager)  getActivity().getSystemService(Context.LOCATION_SERVICE);
        //Criteria criteria = new Criteria();
        //String bestProvider = locationManager.getBestProvider(criteria, true);
        //Location location = locationManager.getLastKnownLocation(bestProvider);
        Location location = LocationProvider.getInstance().getLastKnownLocation(this.getActivity().getApplicationContext());
        if (location != null) {
            updateLocationPin(location);
        }




        //locationManager.requestLocationUpdates(bestProvider, 20000, 0, this);
        mMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {

            @Override
            public void onCameraChange(CameraPosition arg0) {


                final long snap = System.currentTimeMillis();
                if (lastCallMs + CAMERA_MOVE_REACT_THRESHOLD_MS > snap) {
                    lastCallMs = snap;
                    return;
                }
                rideSuggestionsCard.setVisibility(View.GONE);
                // TODO Auto-generated method stub
                center = mMap.getCameraPosition().target;

                //markerText.setText(R.string.set_your_location);
                mMap.clear();
                markerLayout.setVisibility(View.VISIBLE);


                Double[] lat_long = new Double[]{center.latitude, center.longitude};

                Log.d(TAG, "CAMERA CHANGE CALLBACK: lat_long: " + Arrays.deepToString(lat_long));
                Log.d(TAG, "REVERSE GEO CODING: lat_long: " + Arrays.deepToString(lat_long));
                // Executing ReverseGeocodingTask to get Address
                new ReverseGeocodingTask(MapCabFinder.this.getActivity().getBaseContext()).execute(lat_long);

                PIN_LATITUDE = center.latitude;
                PIN_LONGITUDE = center.longitude;

                notifyLocationChange();

                lastCallMs = snap;
            }
        });
    }

    void notifyLocationChange(){
        mUberManager.notifyLocationChange();//TODO ADD LOGIC TO AVOID REPEATED CALLS

        checkRydeAvailaibility();
    }

    void updateLocationPin(Location location) {
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        LatLng latLng = new LatLng(latitude, longitude);
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,15));
        setPlaceFromLocation(location);
        PIN_LATITUDE=location.getLatitude();
        PIN_LONGITUDE=location.getLongitude();
    }


    void moveCamera(LatLng latLng) {

        if (latLng != null) {
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
            PIN_LATITUDE = latLng.latitude;
            PIN_LONGITUDE = latLng.longitude;
        }
    }



    void setPlaceFromLocation(Location location) {
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        //Double[] lat_long = new Double[] { latitude/1E6, longitude/1E6 };
        Double[] lat_long = new Double[]{latitude, longitude};

        Log.d(TAG, "REVERSE GEO CODING: lat_long: " + Arrays.deepToString(lat_long));
        // Executing ReverseGeocodingTask to get Address
        new ReverseGeocodingTask( getActivity()).execute(lat_long);
    }




    private boolean isGooglePlayServicesAvailable() {
        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getActivity());
        if (ConnectionResult.SUCCESS == status) {
            return true;
        } else {
            GooglePlayServicesUtil.getErrorDialog(status, getActivity(), 0).show();
            return false;
        }
    }

    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);
            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();
            Log.d(TAG, "urlConnection:" + urlConnection);

            // Connecting to url
            urlConnection.connect();

            // Reading data from url
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb = new StringBuffer();

            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            data = sb.toString();
            br.close();
            Log.d(TAG, "data:" + data);

        } catch (Exception e) {
            Log.d(TAG, "Exception while downloading url " + e.toString());
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }

        return data;
    }

    @Override
    public void onLocationChanged(Location location) {
        //updateLocationPin(location);
    }

    @Override
    public void connectionStaus(ILocationListener.ConnectionStatus status) {

    }

    @Override
    public void locationStatus(Status resolutionStatus) {
        switch (resolutionStatus.getStatusCode()) {
            case LocationSettingsStatusCodes.SUCCESS:
                Log.i(getClass().toString(), "All location settings are satisfied.");

                break;
            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                Log.i(getClass().toString(), "Location settings are not satisfied. Show the user a dialog to" +
                        "upgrade location settings ");


                try {
                    // Show the dialog by calling startResolutionForResult(), and check the result
                    // in onActivityResult().
                    resolutionStatus.startResolutionForResult(getActivity(), REQUEST_CODE_LOCATION_SETTINGS);
                } catch (IntentSender.SendIntentException e) {
                    Log.i(getClass().toString(), "PendingIntent unable to execute request.");
                }
                break;
            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                Toast.makeText(getActivity(),  "Location settings are inadequate, and cannot be fixed here. Dialog " +
                        "not created." , Toast.LENGTH_SHORT).show();
                //getActivity().finish();

                break;
        }

    }

    @Override
    public void onGPSStatusChange(int event) {
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        //setUpMap();
    }

    //For Reverse Geo-Coding
    private class ReverseGeocodingTask extends AsyncTask<Double, Void, String> {
        Context mContext;

        public ReverseGeocodingTask(Context context) {
            super();
            mContext = context;
        }

        @Override
        protected String doInBackground(Double... params) {
            Geocoder geocoder = new Geocoder(mContext);
            double latitude = params[0].doubleValue();
            double longitude = params[1].doubleValue();

            List<Address> addresses = null;
            String addressText = "";

            try {
                addresses = geocoder.getFromLocation(latitude, longitude, 1);
                Log.d(TAG, "REVERSE GEO CODING: addresses: " + addresses);
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (addresses != null && addresses.size() > 0) {
                Address address = addresses.get(0);

                addressText = String.format("%s, %s, %s",
                        address.getMaxAddressLineIndex() > 0 ? address.getAddressLine(0) : "",
                        address.getLocality(),
                        address.getCountryName());
            }
            Log.d(TAG, "REVERSE GEO CODING: addressText: " + addressText);

            return addressText;
        }

        @Override
        protected void onPostExecute(String addressText) {
            if(isCancelled())
                return;
            // Setting address of the touched Position
            locationTv.setText(addressText);
        }

    }

    //AllRydeList

    void checkRydeAvailaibility()
    {
        resetMinimumPickuptime();
        Log.i(TAG, "Before making a network request for Uber !!");
        getUberRydeAvailability();
        Log.i(TAG, "Before making a network request for OLA !!");
        getOlaRydeAvailability();
    }

    void getUberRydeAvailability()
    {
        UberAPIClient.getUberV1APIClient().getTimeEstimates(getAccessToken(),
                PIN_LATITUDE,
                PIN_LONGITUDE,
                new UberCallback<TimeEstimateList>() {
                    @Override
                    public void success(TimeEstimateList timeEstimateList, Response response) {
                        Log.i(TAG, "Response to Uber Cabs : "+ response.toString());
                        setupListAdapter(Constants.CAB_GLOBAL.UBER, timeEstimateList);
                    }
                });
    }

    void getOlaRydeAvailability()
    {
        OlaAPIClient.getOlaV1APIClient().getRydeEstimates(OlaAPIConstants.getOlaXAppToken(this.getActivity()),
                PIN_LATITUDE,//Constants.START_LATITUDE,
                PIN_LONGITUDE,//Constants.START_LONGITUDE,
                Constants.END_LATITUDE,
                Constants.END_LONGITUDE,
                null,
                new OlaCallback<RideEstimateList>() {
                    @Override
                    public void success(RideEstimateList rideEstimate, Response response) {
                        Log.i(TAG, "Response to Ola Cabs : "+ response.toString());
                        setupListAdapter(Constants.CAB_INDIA.OLA, rideEstimate);
                    }
                }
                );
    }

    private void setupListAdapter(Constants.CABCOMPANY cabcompany, Object rideobjectList) {

        if(cabcompany== Constants.CAB_GLOBAL.UBER)
        {
            mAdapter.clearRidesItems(cabcompany);

            TimeEstimateList timeEstimateList=(TimeEstimateList)rideobjectList;
            CabEstimateListAdapter.Estimate estimate;

            if(timeEstimateList!=null && timeEstimateList.getTimes()!=null && timeEstimateList.getTimes().size()>0) {
                estimate = new CabEstimateListAdapter.Estimate();
                estimate.cabcompany = cabcompany;
                estimate.display_name = cabcompany.toString();
                estimate.cabImageResource = R.drawable.uber_icon_36px;
                mAdapter.addSectionHeaderItem(estimate);

                Toast.makeText(this.getActivity(),"Uber rides size:"+ timeEstimateList.getTimes().size(), Toast.LENGTH_SHORT).show();
                for (TimeEstimate timeEstimate : timeEstimateList.getTimes()) {
                    Log.d(TAG, "timeEstimate:" + timeEstimate.toString() + timeEstimate.getDisplayName() + " " + timeEstimate.getEstimate());
                    estimate = new CabEstimateListAdapter.Estimate();
                    estimate.cabcompany = cabcompany;
                    estimate.id = timeEstimate.getProductId();
                    estimate.display_name = timeEstimate.getLocalizedDisplayName();
                    estimate.eta = getETAinMinutes(timeEstimate.getEstimate());
                    estimate.address = locationTv.getText().toString();
                    if (ProductDetailMap != null && ProductDetailMap.containsKey(estimate.id) && ProductDetailMap.get(estimate.id) != null) {
                        if (ProductDetailMap.get(estimate.id).getCostPerDistance() > 0)
                            estimate.costPerDistance = ProductDetailMap.get(estimate.id).getCostPerDistance();
                        if (ProductDetailMap.get(estimate.id).getSurgeCharge() > 1.0)
                            estimate.surcharge = ProductDetailMap.get(estimate.id).getSurgeCharge();
                    }
                    //estimate.surcharge=-1;//TO identify no surge charge info presence
                    mAdapter.addItem(estimate);//timeEstimate.getDisplayName()+" "+timeEstimate.getEstimate());

                    updateMinimumPickTime(estimate.eta);
                }
                mAdapter.clearHeaderIfNoItems(cabcompany);
                mAdapter.notifyDataSetChanged();
            }
        }
        else if(cabcompany==Constants.CAB_INDIA.OLA)
        {
            mAdapter.clearRidesItems(cabcompany);

            RideEstimateList rideEstimateList=(RideEstimateList)rideobjectList;
            CabEstimateListAdapter.Estimate estimate;
            if(rideEstimateList.getCategories()!=null && rideEstimateList.getCategories().size()>0) {
                Toast.makeText(this.getActivity(),"OLA rides size:"+ rideEstimateList.getCategories().size(), Toast.LENGTH_SHORT).show();

                estimate = new CabEstimateListAdapter.Estimate();
                estimate.cabcompany = cabcompany;
                estimate.display_name = cabcompany.toString();
                estimate.cabImageResource = R.drawable.ola_icon;
                mAdapter.addSectionHeaderItem(estimate);

                for (ProductCatagory categories : rideEstimateList.getCategories()) {
                    estimate = new CabEstimateListAdapter.Estimate();
                    estimate.cabcompany = cabcompany;
                    estimate.id = categories.id;
                    estimate.display_name = categories.display_name;
                    estimate.eta = categories.eta;
                    if(estimate.eta==-1)
                        continue;

                    if (categories.fare_breakup != null && categories.fare_breakup.get(0) != null)
                        estimate.costPerDistance = categories.fare_breakup.get(0).cost_per_distance;
                    estimate.address = locationTv.getText().toString();
                    if (categories.fare_breakup != null && categories.fare_breakup.size() > 0 && categories.fare_breakup.get(0).surcharge.size() > 0)
                        estimate.surcharge = categories.fare_breakup.get(0).surcharge.get(0).value;
                    mAdapter.addItem(estimate);

                    updateMinimumPickTime(estimate.eta);
                }
                mAdapter.clearHeaderIfNoItems(cabcompany);
                mAdapter.notifyDataSetChanged();
            }
        }

        mAdapter.notifyDataSetChanged();
        updateMinimumPickUpTimeText();

        if(mAdapter.isEmpty()) {

                            rideSuggestionsCard.setVisibility(View.GONE);
            // rideSuggestionsCard.setVisibility(View.GONE);
        }
        else{
              rideSuggestionsCard.setVisibility(View.VISIBLE);

        }
            //rideSuggestionsCard.setVisibility(View.VISIBLE);



    }

    void updateMinimumPickTime(int minimumPickupTime){

        if(iMinimumPickupTime>minimumPickupTime) {
            iMinimumPickupTime = minimumPickupTime;
        }
    }

    void updateMinimumPickUpTimeText(){

        if(iMinimumPickupTime!=999) {
            ll_pickuptimetext.setVisibility(View.VISIBLE);
            SpannableString stext = new SpannableString(iMinimumPickupTime + "\nMIN");
            stext.setSpan(new RelativeSizeSpan(1.5f), 0, 2, 0); // set size
            //stext.setspan(new foregroundcolorspan(color.red), 0, 2, 0);// set color
            pickuptimeText.setText(stext);
        }
        else
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if(iMinimumPickupTime==999)
                        ll_pickuptimetext.setVisibility(View.GONE);
                }
            },1000);
    }

    void resetMinimumPickuptime(){
        iMinimumPickupTime=999;
    }

    int getETAinMinutes(int seconds){
        return seconds>60?( seconds%60>20 ? (seconds/60)+1 : seconds/60):1;
    }

    private String getAccessToken() {
        //if(loggedin)
        //return getIntent().getStringExtra("token_type") + " " + getIntent().getStringExtra("access_token");
        //else
        return "Token " + Constants.getUberServerToken(getActivity());
    }

    ILocationListener iLocationListener;


    @Override
    public void onDestroy() {
        super.onDestroy();

//        //To cancel the async task on Activity finish
//        if(parserTask!=null && !parserTask.isCancelled())
//            parserTask.cancel(true);
//        if(placesTask!=null && !placesTask.isCancelled())
//            placesTask.cancel(true);

        LocationProvider.getInstance().unregisterLocationListener(listenerId);

        stopRefreshRideAvailabilityTimer();

        mUberManager.clearTasks();
    }

    @Override
    public void onDialogTimedOut(int reqCode) {

        //For any progress dialog if timeout happens then control will come from base class
        // you can show dialog here.

    }

    @Override
    public void processCustomMessage(Message msg) {

        // IN base activity there is  public DialogHandlerActivity mActivityHandler = null;
        // instead of creating a handler you can reuse it and message that cannot be handled in base class will come here.


    }




    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

       if(requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE)
        {

            if (resultCode == Activity.RESULT_OK) {
                Place place = PlaceAutocomplete.getPlace(this.getActivity(), data);
                Log.i(TAG, "Place: " + place.getName());
                moveCamera(place.getLatLng());
            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(this.getActivity(), data);
                // TODO: Handle the error.
                Log.i(TAG, status.getStatusMessage());

            } else if (resultCode == Activity.RESULT_CANCELED) {
                // The user canceled the operation.
            }



          //  getActivity().finish();
        }


    }
    ///

}
