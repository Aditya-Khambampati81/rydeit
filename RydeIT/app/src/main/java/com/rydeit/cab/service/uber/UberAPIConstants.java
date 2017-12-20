package com.rydeit.cab.service.uber;

import android.content.Context;

import com.rydeit.util.AndroidUtils;

/**
 * Created by Prakhyath on 10/17/15.
 */
public class UberAPIConstants {

    public static class RIDEREQUEST_STATUS {

        /**
         * Request A Ride
         */
        public static final String PROCESSING = "processing";

        /**
         * No Driver Available
         */
        public static final String NO_DRIVER_AVAILABLE = "no_drivers_available";

        /**
         * Driver Accepted
         */
        public static final String ACCEPTED = "accepted";

        /**
         * Driver ARRIVING
         */
        public static final String ARRIVING = "arriving";

        /**
         * Begin Trip
         */
        public static final String IN_PROGRESS = "in_progress";

        /**
         * Driver Cancelled
         */
        public static final String DRIVER_CANCELLED = "driver_canceled";

        /**
         * Rider Cancelled
         */
        public static final String RIDE_CANCELLED = "rider_canceled";

        /**
         * Trip Completed
         */
        public static final String COMPLETED = "completed";
    }

    public static String getUberRedirectUrl(Context context) {
        return AndroidUtils.getManifestData(context, "com.rydeit.UBER_REDIRECT_URL");
    }

    public static class SURGE_REDIRECT_PARAM {
        public static final String RIDE_SURGE_CONFIRM_ID= "surge_confirmation_id";
    }
}
