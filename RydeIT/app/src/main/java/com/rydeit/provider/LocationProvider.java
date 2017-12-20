package com.rydeit.provider;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;


import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


/**
 * This is singleton class used to monitor location changes
 *
 * Created by Aditya Khambampati
 */

public class LocationProvider implements GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener,
        LocationListener, ResultCallback<LocationSettingsResult> {
    private static LocationProvider sInstance = new LocationProvider();
    public LocationManager mLocationManager = null;
    public static final String PREF_LOCATION_PROVIDER_LATITUDE = "KEY_LP_LATITUDE";
    public static final String PREF_LOCATION_PROVIDER_LONGITUDE = "KEY_LP_LONGITUDE";
    private ConcurrentHashMap<Long, ILocationListener> myListenerMap = new ConcurrentHashMap<Long, ILocationListener>();
    private GoogleApiClient mGoogleApiClient = null;
    private GpsStatus.Listener gpsListner = null;

    private Boolean monitoringLocation = false;
    public static final int NOTIFY_ALL = 999;
    public long seed = 1011;

    private Context mContext;
    private ExecutorService executor;
    private Location mLastKnownLocation = null;

    private Future longRunningTaskFuture;

    private boolean isLocationRequested;

    /**
     * Stores parameters for requests to the FusedLocationProviderApi.
     */
    protected LocationRequest mLocationRequest;

    /**
     * Stores the types of location  the client is interested in using. Used for checking
     * settings to determine if the device has optimal location settings.
     */
    protected LocationSettingsRequest mLocationSettingsRequest;

    private  Runnable mTask = null;
    private void LocationProvider() {
    }

    public LocationManager getmLocationManager( ) {
        if (mLocationManager ==null) {
            mLocationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
        }
        return mLocationManager;
    }
    public static LocationProvider getInstance() {
        if (sInstance == null) {
            sInstance = new LocationProvider();
        }
        return sInstance;
    }

    /**
     * Init only once with application context
     *
     * @param context
     */
    public void init(Context context) {
        mContext = context;
        executor = Executors.newSingleThreadExecutor();

        mGoogleApiClient = new GoogleApiClient.Builder(mContext)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

  /**
     * start listeneing for location
     */
    private void startListeningForLocationChanges() {
        if (mGoogleApiClient != null) {

            if (!mGoogleApiClient.isConnected()) {
                mGoogleApiClient.connect();
            }
        }
    }
     /**
     * Callback method when current location will change
     *
     * @param location
     */
    @Override
    public void onLocationChanged(Location location) {

        //As we are not tracing regular location update then remove call back and allow user to fetch using refresh btn
        if (location != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            notifyLocationChange(location, NOTIFY_ALL);
            mLastKnownLocation = location;
        }
        isLocationRequested = false;
    }

    /**
     * Api to know if location client is connected or not
     *
     * @return
     */
    public boolean isConnected() {
        return (mGoogleApiClient == null ? false : mGoogleApiClient.isConnected());
    }


    /**
     * Api to check if the location is enabled
     *
     * @return
     */
    public boolean isGPSAndNetworkEnabled(Context cxt) {

        if (mLocationManager == null) {
            mLocationManager = (LocationManager) cxt.getSystemService(Context.LOCATION_SERVICE);
        }

        return mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    /**
     * API to check if GPS is enabled
     *
     * @return - boolean if GPS is enabled or not
     */
    public boolean isGpsEnabled(Context cxt) {

        if (mLocationManager == null) {
            mLocationManager = (LocationManager) cxt.getSystemService(Context.LOCATION_SERVICE);
        }

        return mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    /**
     * Api to do reconnect  not needed from outside AFAIK but FIXME later.
     */

    public void reConnect() {
        mGoogleApiClient.reconnect();
    }

    /**
     * Stop listening for location updates it is not public method
     */
    private void stopListeningForLocationChanges() {
        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
            monitoringLocation = false;
        }
    }

    /**
     * Api to monitor all location updatess , the id of the listener is passed back to the caller
     * caller can cache it and use it while de-registration
     *
     * @param listener
     */
    public long registerLocationListener(final ILocationListener listener) {
        myListenerMap.put(++seed, listener);
        if (!monitoringLocation) {
            startListeningForLocationChanges();
        }
        else
        {
            notifyLocationChange(mLastKnownLocation,NOTIFY_ALL);
        }




        if(gpsListner == null) {

            gpsListner = new GpsStatus.Listener() {
                @Override
                public void onGpsStatusChanged(int event) {
                    notifyGpsChangeEvent(event, NOTIFY_ALL);
                }
            };
            getmLocationManager().addGpsStatusListener(gpsListner);
        }


       return seed;
    }


    /**
     * Function used to notify all or particular listenr based on Id we pass
     *
     * @param
     * @param id          = NOTIFY_ALL  notifys all
     */
    private void notifyGpsChangeEvent(int event, long id) {

        if (id != NOTIFY_ALL) {

            ILocationListener myListener = myListenerMap.get(id);
            if (myListener == null) {
                Log.e(LocationProvider.class.toString(), "There is no resource associated with the key" + id);
                return;
            }
            myListener.onGPSStatusChange(event);
            return;
        }

        // process only if id is NOTIFY ALL
        for (Map.Entry<Long, ILocationListener> entry : myListenerMap.entrySet()) {
            ILocationListener listener = entry.getValue();
            if (listener == null) {
                Log.e(LocationProvider.class.toString(), "There is no resource associated with the key" + entry.getKey());
                continue;
            }
            listener.onGPSStatusChange(event);
        }
    }



    /**
     * Function to load filters
     *
     * @param listenerId
     * @throws IllegalStateException
     */
    public void getAllFilters(long listenerId) throws IllegalStateException {

        if (!myListenerMap.contains(listenerId)) {
            throw new IllegalStateException("listener is not yet initialized !! suggest calling requestForLocationUpdates with listener");
        }

        //FIXME : write code in background thread to fetch the filters and revert back on listener id
        // for now we don't need filters but if we already know API lets implement it


    }

    /**
     * Stop listening for location updates
     *
     * @param listenerId
     */
    public void unregisterLocationListener(Long listenerId) {
        //removing GPS listner

        myListenerMap.remove(listenerId);
        if (myListenerMap.size() == 0) {
            stopListeningForLocationChanges();
            if (gpsListner!=null) {
                mLocationManager.removeGpsStatusListener(gpsListner);
            }
        }


    }

    /**
     * Function used to notify all or particular listenr based on Id we pass
     *
     * @param newLocation
     * @param id          = NOTIFY_ALL  notifys all
     */
    private void notifyLocationChange(Location newLocation, long id) {

        if (id != NOTIFY_ALL) {

            ILocationListener myListener = myListenerMap.get(id);
            if (myListener == null) {
                Log.e(LocationProvider.class.toString(), "There is no resource associated with the key" + id);
                return;
            }
            myListener.onLocationChanged(newLocation);
            return;
        }

        // process only if id is NOTIFY ALL
        for (Map.Entry<Long, ILocationListener> entry : myListenerMap.entrySet()) {
            ILocationListener listener = entry.getValue();
            if (listener == null) {
                Log.e(LocationProvider.class.toString(), "There is no resource associated with the key" + entry.getKey());
                continue;
            }
            listener.onLocationChanged(newLocation);
        }
    }
    /**
     * Function used to notify all or particular listenr based on Id we pass
     *
     * @param status -
     *               ILocationListener.ConnectionStatus.CONNECTED / DISCONNECTED
     * @param id     = NOTIFY_ALL  notifys all otherwise only the id of the listener
     */
    private void notifyConnectionStatus(ILocationListener.ConnectionStatus status, long id) {

        if (id != NOTIFY_ALL) {

            ILocationListener myListener = myListenerMap.get(id);
            if (myListener == null) {
                Log.e(LocationProvider.class.toString(), "There is no resource associated with the key" + id);
                return;
            }
            myListener.connectionStaus(status);
            return;
        }

        // process only if id is NOTIFY ALL
        for (Map.Entry<Long, ILocationListener> entry : myListenerMap.entrySet()) {
            ILocationListener value = entry.getValue();
            if (value == null) {
                Log.e(LocationProvider.class.toString(), "There is no resource associated with the key" + entry.getKey());
                continue;
            }
            value.connectionStaus(status);
        }
    }

    /**
     * Function used to notify all or particular listenr based on Id we pass
     *
     * @param status -
     *               status of device location settings
     * @param id     = NOTIFY_ALL  notifys all otherwise only the id of the listener
     */
    private void notifyLocationStatus(Status status, long id) {

        if (id != NOTIFY_ALL) {

            ILocationListener myListener = myListenerMap.get(id);
            if (myListener == null) {
                Log.e(LocationProvider.class.toString(), "There is no resource associated with the key" + id);
                return;
            }
            myListener.locationStatus(status);
            return;
        }

        // process only if id is NOTIFY ALL
        for (Map.Entry<Long, ILocationListener> entry : myListenerMap.entrySet()) {
            ILocationListener value = entry.getValue();
            if (value == null) {
                Log.e(LocationProvider.class.toString(), "There is no resource associated with the key" + entry.getKey());
                continue;
            }
            value.locationStatus(status);
        }
    }



    public void tearDownLocationProvider() {

        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
        }

        mLocationManager = null;
        sInstance = null;
    }

    /**
     * API to check if google services are available
     *
     * @return
     */
    public boolean isPlayServicesAvailable(Context activityContext) {
        // Check that Google Play services is available
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(activityContext);
        // If Google Play services is available
        if (ConnectionResult.SUCCESS == resultCode) {
            return true;
            // Google Play services was not available for some reason
        } else {
            return false;
        }
    }

    /**
     * Get last known location
     *
     * @return
     */
    public Location getLastKnownLocation(Context cxt) {
        Location me = null;
        if (isConnected()) {
            me = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            SharedPreferences  preferences = PreferenceManager.getDefaultSharedPreferences(cxt);
            if (me != null) {
                SharedPreferences.Editor edit = preferences.edit();
                edit.putString(PREF_LOCATION_PROVIDER_LATITUDE, "" + me.getLatitude());
                edit.putString(PREF_LOCATION_PROVIDER_LONGITUDE, "" + me.getLongitude());
                edit.commit();
            }else{
                try {
                    Location location = new Location("temp");
                    //Defaulting to RCP location
                    location.setLatitude(Double.parseDouble( preferences.getString(PREF_LOCATION_PROVIDER_LATITUDE,"")));
                    location.setLongitude(Double.parseDouble(preferences.getString(PREF_LOCATION_PROVIDER_LONGITUDE,"")));
                    me = location;
                }catch (Exception e){
                    Log.e(LocationProvider.class.toString(), "Incorrect double value for Lat Lon into preference file");
                }

            }
        }else{
            Log.e(LocationProvider.class.toString(), "location client is not connected");

        }

        return me;
    }


    private void availUserLocation() {
        //Assume that google map had already requested user location as setCurrentLocation is true in Finder map
        Location loc = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (loc != null) {
            onLocationChanged(loc);
        } else {
            //Send location request if no client is attached to location service and current location is not available
            sendLocationRequest();
        }

    }

    /**
     * Force req for new location update
     */
    public boolean sendLocationRequest() {

        boolean isSuccess = false;
        if (isConnected() && !isLocationRequested) {
            isLocationRequested = true;
            isSuccess = true;
            mLocationRequest = LocationRequest.create();
            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, mLocationRequest, LocationProvider.getInstance());

        }

        return isSuccess;
    }

    @Override
    public void onConnected(Bundle bundle) {
        //Request for new location when map launches first time.
        availUserLocation();
      //Location location =  getLastKnownLocation();
      notifyConnectionStatus(ILocationListener.ConnectionStatus.CONNECTED,seed);
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(LocationProvider.class.toString(), "Location manager connection is suspended!");

    }


    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.i(LocationProvider.class.toString(), "Location manager connection is failed!");
    }


    public String getFormattedDistance(Context cxt,Location searchedLocation, double lat, double lon) {
        String sDistance = "unknown";
        Location dLocation = new Location("marker");
        dLocation.setLatitude(lat);
        dLocation.setLongitude(lon);

        searchedLocation = getLocation(cxt ,searchedLocation);

        if (searchedLocation != null) {
            sDistance = formatDistance(dLocation.distanceTo(searchedLocation));
        }

        return sDistance;

    }

    public float getFormattedDistanceFloat(Context cxt, Location searchedLocation, double lat, double lon) {
        float sDistance = -1;
        Location dLocation = new Location("marker");
        dLocation.setLatitude(lat);
        dLocation.setLongitude(lon);
        searchedLocation = getLocation(cxt,searchedLocation);

        if (searchedLocation != null) {
            sDistance = dLocation.distanceTo(searchedLocation);
        } else {

        }

        return sDistance;

    }

    private Location getLocation(Context cxt , Location searchedLocation) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(cxt);

        if (searchedLocation == null) {
            searchedLocation = getLastKnownLocation(cxt);
        }

        if (searchedLocation == null) {
            try {
            Location location = new Location("temp");
            location.setLatitude(Double.parseDouble(preferences.getString(PREF_LOCATION_PROVIDER_LATITUDE,"")));
            location.setLongitude(Double.parseDouble(preferences.getString(PREF_LOCATION_PROVIDER_LONGITUDE,"")));
            searchedLocation = location;
            }catch (Exception e){
                Log.e(LocationProvider.class.toString(), "Incorrect double value for Lat Lon into preference file");
            }
        }
        return searchedLocation;
    }

    public String formatDistance(float meters) {
        if (meters > -1) {
            if (meters < 1000) {
                return ((int) meters) + "m";
            } else if (meters < 10000) {
                return formatDecimal(meters / 1000f, 1) + "km";
            } else {
                return ((int) (meters / 1000f)) + "km";
            }
        } else {
            return "unknown";
        }
    }

    private String formatDecimal(float val, int dec) {
        int factor = (int) Math.pow(10, dec);
        int front = (int) (val);
        int back = (int) Math.abs(val * (factor)) % factor;
        return front + "." + back;
    }


    public void getLocationAddress(final Location location, final Handler handler, final int what) {

        //Fix for my location null issue
        if (location != null) {
            new Thread() {
                @Override
                public void run() {

                    Geocoder geocoder = new Geocoder(mContext, Locale.getDefault());
                    String detail = null;
                    String name = null;
                    try {
                        List<Address> list = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                        if (list != null && list.size() > 0) {
                            Address address = list.get(0);
                            StringBuilder sb = new StringBuilder();
                            for (int i = 0; i < address.getMaxAddressLineIndex(); i++) {
                                if (i == 0) {
                                    name = address.getAddressLine(i);
                                    continue;
                                }
                                sb.append(address.getAddressLine(i)).append("\n");
                            }
                            sb.append(address.getCountryName());
                            detail = sb.toString();
                        }

                    } catch (IOException e) {
                        Log.e(LocationProvider.class.toString(), e.getMessage());
                    } finally {
                        Message msg = Message.obtain();
                        msg.setTarget(handler);
                        msg.what = what;
                        Bundle bundle = new Bundle();
                        if (detail != null) {
                            bundle.putString("ADDRESS", detail);
                        } else {
                            bundle.putString("ADDRESS", "Unknown");
                        }

                        if (name != null) {
                            bundle.putString("NAME", name);
                        } else {
                            bundle.putString("NAME", "Unknown");
                        }

                        msg.setData(bundle);
                        msg.sendToTarget();
                    }
                }
            }.start();

        }
    }





    protected void buildLocationSettingsRequest() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        if(mLocationRequest == null) {
            mLocationRequest = LocationRequest.create();
            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        }
        builder.addLocationRequest(mLocationRequest);
        mLocationSettingsRequest = builder.build();
    }



    public void checkLocationSettings(ResultCallback<LocationSettingsResult> resultCallback) {
        if (mLocationSettingsRequest == null)
        {
            buildLocationSettingsRequest();
        }

        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(
                        mGoogleApiClient,
                        mLocationSettingsRequest
                );
        result.setResultCallback(resultCallback);
    }

    /**
     * The callback invoked when
     * {@link com.google.android.gms.location.SettingsApi#checkLocationSettings(GoogleApiClient,
     * LocationSettingsRequest)} is called. Examines the
     * {@link LocationSettingsResult} object and determines if
     * location settings are adequate. If they are not, begins the process of presenting a location
     * settings dialog to the user.
     */
    @Override
    public void onResult(LocationSettingsResult locationSettingsResult) {
        final Status status = locationSettingsResult.getStatus();
        switch (status.getStatusCode()) {
            case LocationSettingsStatusCodes.SUCCESS:
                Log.i(getClass().toString(), "All location settings are satisfied.");

                break;
            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                Log.i(getClass().toString(), "Location settings are not satisfied. Show the user a dialog to" +
                        "upgrade location settings ");

                notifyLocationStatus(status,NOTIFY_ALL);

                break;
            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                Log.i(getClass().toString(), "Location settings are inadequate, and cannot be fixed here. Dialog " +
                        "not created.");
                notifyLocationStatus(status,NOTIFY_ALL);
                break;
        }
    }



}
