package com.rydeit.cab.service.ola.converter;

import com.rydeit.util.Constants;

/**
 * Created by Prakhyath on 12/24/15.
 */
public class DataConverter {

    private static DataConverter mDataConverter = null;

    private DataConverter() {
    }

    public static DataConverter getInstance() {
        if (mDataConverter == null) {
            mDataConverter = new DataConverter();
        }
        return mDataConverter;
    }

    private Constants.CABCOMPANY getCabType(){
        return Constants.CAB_INDIA.OLA;
    }

}
