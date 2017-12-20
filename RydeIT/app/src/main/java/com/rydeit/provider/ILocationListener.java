package com.rydeit.provider;

import android.location.Location;

import com.google.android.gms.common.api.Status;

import java.util.List;

/**
 * Any module that wants to listen for location updates needs
 * to register using this listener
 *
 */
public interface ILocationListener {
     public enum ConnectionStatus {CONNECTED, DISCONNECTED, FAILED};
    /**
     * Callback that provides a location update to listener
     * @param location
     */
    public void onLocationChanged(Location location);


    /**
     * on connected, disconnected
     */
    public void connectionStaus(ConnectionStatus status);

    /**
     * Callback invoked when location status is not as per the
     * desired level using API
     */
    public void locationStatus(Status resolutionStatus);

    /**
     * Callback invoked when GPS status is not as per the
     * desired level using API
     */
    public void onGPSStatusChange(int event);

}
