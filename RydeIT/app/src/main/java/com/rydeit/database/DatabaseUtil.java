package com.rydeit.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import com.rydeit.model.common.MapPoint;
import com.rydeit.model.common.MyBooking;
import com.rydeit.util.Constants;

import java.util.ArrayList;

/**
 * Created by Prakhyath on 11/22/15.
 */
public class DatabaseUtil {


    public static final String MY_RIDES_TABLE_NAME="MY_BOOKINGS";
    public static final Uri URI_HISTORY = Uri.parse("sqlite://com.rydeit/history");




    public static int deleteRydeInfo(Context cxt , String bookingId)throws IllegalAccessException {

        if (bookingId == null)
            return -1;
        int rowsUpdated = -1;
        SQLiteDatabase db = DatabaseHelper.getInstance(cxt).getWritableDatabase();

        if (db == null) {
            throw new IllegalAccessException("Sqlite database is null");
        }
        try {
            db.beginTransaction();
            String[] args = {bookingId};
            rowsUpdated = db.delete(MY_RIDES_TABLE_NAME, ("CRN" + "=" + "?"), args);
            db.setTransactionSuccessful();

        } catch (Exception ex) {
            ex.printStackTrace();

        } finally {
            db.endTransaction();
        }
        if (rowsUpdated > 0) {
            cxt.getContentResolver().notifyChange(URI_HISTORY, null);
        }
        return rowsUpdated;
    }

    public static int updateRideStatus(Context cxt, String bookingId, GenericRydeState genericRydeState){
        ContentValues cv = new ContentValues();
        cv.put("BOOKING_STATUS", genericRydeState.toString());
        try {
            return DatabaseUtil.updateRideStatus(cxt, cv, bookingId);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static int updateRideStatus(Context cxt ,ContentValues cv , String bookingId) throws IllegalAccessException
    {
        if(bookingId == null)
            return -1;
        int rowsUpdated = -1;
        SQLiteDatabase db = DatabaseHelper.getInstance(cxt).getWritableDatabase();

        if (db == null) {
            throw new IllegalAccessException("Sqlite database is null");
        }
        try {
            db.beginTransaction();
            String[] args = {bookingId};
            rowsUpdated =db.update(MY_RIDES_TABLE_NAME, cv, ("CRN" + "=" + "?"), args);

            db.setTransactionSuccessful();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();

        }
        finally {
            db.endTransaction();
        }
        if(rowsUpdated > 0)
        {
            cxt.getContentResolver().notifyChange(URI_HISTORY, null);
        }

        return rowsUpdated;
    }



    public static long insertRideInfo(Context cxt, MyBooking myBooking) throws IllegalAccessException {

        if(myBooking==null)
            return -1;

        SQLiteDatabase db = DatabaseHelper.getInstance(cxt).getWritableDatabase();
        long insertedUri =0;

        if (db == null) {
            throw new IllegalAccessException("Sqlite database is null");
        }
        db.beginTransaction();

        try {

            ContentValues cv = new ContentValues();

            cv.put("CRN", myBooking.crn);
            cv.put("CAB_COMPANY", myBooking.cabCompany);
            cv.put("CAB_TYPE", myBooking.cab_type);
            cv.put("CAR_NUMBER",myBooking.cab_number);
            cv.put("CAR_MODEL",myBooking.car_model);
            cv.put("DRIVER_NAME",myBooking.driver_name);
            cv.put("PICKUP_ADDRESS",myBooking.pickUpAddress);
            cv.put("BOOKING_STATUS",myBooking.booking_status);
            cv.put("PICKUP_TIME", myBooking.pickupTime);
            if(myBooking.pickupLoction!=null) {
                cv.put("SOURCE_LATITUDE", myBooking.pickupLoction.getLattitude());
                cv.put("SOURCE_LONGITUDE", myBooking.pickupLoction.getLongitude());
            }

            insertedUri = db.insert(MY_RIDES_TABLE_NAME, null, cv);
            db.setTransactionSuccessful();
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            db.endTransaction();
        }

        if (insertedUri > 0)
        {
            Log.d("DataBaseUtil","DATA INSERTED:"+insertedUri);
            if(Constants.ENABLE_DEBUG_TOAST)
                Toast.makeText(cxt, "DATA INSERTION SUCCESS.", Toast.LENGTH_LONG).show();
                cxt.getContentResolver().notifyChange(URI_HISTORY,null);

            //cxt.getContentResolver().notifyChange(URI_MY_TABLE,null);
        }
        else {
            Log.d("DataBaseUtil", "DATA NOT INSERTED:");
            if(Constants.ENABLE_DEBUG_TOAST)
                Toast.makeText(cxt, "DATA NOT INSERTED", Toast.LENGTH_LONG).show();
        }
        return insertedUri;
    }

    public static ArrayList<MyBooking> getAllActiveRides(Context cxt) {
        SQLiteDatabase db = DatabaseHelper.getInstance(cxt).getWritableDatabase();

        ArrayList<MyBooking> list = new ArrayList<MyBooking>();


        try {
            /**
             *  query(String table, String[] columns, String selection,
             String[] selectionArgs, String groupBy, String having,
             String orderBy)
             */
            String selection = "BOOKING_STATUS" + "!=?" + " AND " +"BOOKING_STATUS" + "!=?";
            //FIXME : Add a class with all supported status.

            String [] args = {GenericRydeState.CANCELLED,GenericRydeState.COMPLETED};


            Cursor c = db.query(MY_RIDES_TABLE_NAME, null,selection ,args,null, null,"PICKUP_TIME");
            int i=c.getCount();
           // Toast.makeText(cxt,"DB My Booking data Count="+c.getCount(),Toast.LENGTH_LONG).show();
            try {

                // looping through all rows and adding to list
                if (c.moveToFirst()) {
                    do {
                        //If its more than 24 hour old booking just clear from RydeIt history & Dont show Active Rides dialogue
                        long currentTimeinSeconds=System.currentTimeMillis()/1000;
                        if(currentTimeinSeconds-c.getLong(c.getColumnIndex("PICKUP_TIME"))>(24*3600)) {
                            updateOnCompletion(cxt, c.getString(c.getColumnIndex("CRN")));
                            //Toast.makeText(cxt,"Old booking deleted",Toast.LENGTH_LONG).show();
                            continue;
                        }


                        MyBooking myBooking = new MyBooking();
                        myBooking.crn=c.getString(c.getColumnIndex("CRN"));
                        myBooking.cabCompany=c.getString(c.getColumnIndex("CAB_COMPANY"));
                        myBooking.cab_type=c.getString(c.getColumnIndex("CAB_TYPE"));
                        myBooking.cab_number=c.getString(c.getColumnIndex("CAR_NUMBER"));
                        myBooking.car_model=c.getString(c.getColumnIndex("CAR_MODEL"));
                        myBooking.driver_name=c.getString(c.getColumnIndex("DRIVER_NAME"));
                        myBooking.driver_number=c.getString(c.getColumnIndex("DRIVER_NUMBER"));
                        myBooking.pickUpAddress=c.getString(c.getColumnIndex("PICKUP_ADDRESS"));
                        myBooking.booking_status=c.getString(c.getColumnIndex("BOOKING_STATUS"));
                        myBooking.pickupTime=c.getLong(c.getColumnIndex("PICKUP_TIME"));

                        Double pickupLattitide=null, pickupLongitude=null;
                        if (!c.isNull(c.getColumnIndex("SOURCE_LATITUDE"))){
                            pickupLattitide=c.getDouble(c.getColumnIndex("SOURCE_LATITUDE"));
                        }
                        if (!c.isNull(c.getColumnIndex("SOURCE_LONGITUDE"))){
                            pickupLongitude=c.getDouble(c.getColumnIndex("SOURCE_LONGITUDE"));
                            if(pickupLattitide!=null)
                                myBooking.pickupLoction=new MapPoint(pickupLattitide, pickupLongitude);
                        }

                        Double dropLattitide=null, dropLongitude=null;
                        if (!c.isNull(c.getColumnIndex("DEST_LATITUDE"))){
                            dropLattitide=c.getDouble(c.getColumnIndex("DEST_LATITUDE"));
                        }
                        if (!c.isNull(c.getColumnIndex("DEST_LONGITUDE"))){
                            dropLongitude=c.getDouble(c.getColumnIndex("DEST_LONGITUDE"));
                            if(dropLattitide!=null)
                                myBooking.dropLocation=new MapPoint(dropLattitide, dropLongitude);
                        }

                        list.add(myBooking);
                    } while (c.moveToNext());
                }

            } catch (Exception ex) {
                ex.printStackTrace();
            }
            finally {
                try { c.close(); } catch (Exception ignore) {}
            }

        } finally {
            try { db.close(); } catch (Exception ignore) {}
        }

        return list;




    }

    public static void updateOnCompletion(Context cxt, String crn)
    {
        ContentValues cv = new ContentValues();
        cv.put("BOOKING_STATUS","COMPLETED");
        try {
            DatabaseUtil.updateRideStatus(cxt, cv, crn);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public static ArrayList<MyBooking> getAllRideList(Context cxt) {

        SQLiteDatabase db = DatabaseHelper.getInstance(cxt).getWritableDatabase();

        ArrayList<MyBooking> list = new ArrayList<MyBooking>();

        // Select All Query
        String selectQuery = "SELECT * FROM MY_BOOKINGS ORDER BY PICKUP_TIME DESC LIMIT 10";

        try {

            Cursor c = db.rawQuery(selectQuery, null);
            int i=c.getCount();
           // Toast.makeText(cxt,"DB My Booking data Count="+c.getCount(),Toast.LENGTH_LONG).show();
            try {

                // looping through all rows and adding to list
                if (c.moveToFirst()) {
                    do {
                        MyBooking myBooking = new MyBooking();
                        myBooking.crn=c.getString(c.getColumnIndex("CRN"));
                        myBooking.cabCompany=c.getString(c.getColumnIndex("CAB_COMPANY"));
                        myBooking.cab_type=c.getString(c.getColumnIndex("CAB_TYPE"));
                        myBooking.cab_number=c.getString(c.getColumnIndex("CAR_NUMBER"));
                        myBooking.car_model=c.getString(c.getColumnIndex("CAR_MODEL"));
                        myBooking.driver_name=c.getString(c.getColumnIndex("DRIVER_NAME"));
                        myBooking.driver_number=c.getString(c.getColumnIndex("DRIVER_NUMBER"));
                        myBooking.pickUpAddress=c.getString(c.getColumnIndex("PICKUP_ADDRESS"));
                        myBooking.booking_status=c.getString(c.getColumnIndex("BOOKING_STATUS"));
                        myBooking.pickupTime=c.getLong(c.getColumnIndex("PICKUP_TIME"));

                        Double pickupLattitide=null, pickupLongitude=null;
                        if (!c.isNull(c.getColumnIndex("SOURCE_LATITUDE"))){
                            pickupLattitide=c.getDouble(c.getColumnIndex("SOURCE_LATITUDE"));
                        }
                        if (!c.isNull(c.getColumnIndex("SOURCE_LONGITUDE"))){
                            pickupLongitude=c.getDouble(c.getColumnIndex("SOURCE_LONGITUDE"));
                            if(pickupLattitide!=null)
                                myBooking.pickupLoction=new MapPoint(pickupLattitide, pickupLongitude);
                        }

                        Double dropLattitide=null, dropLongitude=null;
                        if (!c.isNull(c.getColumnIndex("DEST_LATITUDE"))){
                            dropLattitide=c.getDouble(c.getColumnIndex("DEST_LATITUDE"));
                        }
                        if (!c.isNull(c.getColumnIndex("DEST_LONGITUDE"))){
                            dropLongitude=c.getDouble(c.getColumnIndex("DEST_LONGITUDE"));
                            if(dropLattitide!=null)
                                myBooking.dropLocation=new MapPoint(dropLattitide, dropLongitude);
                        }

                        list.add(myBooking);
                    } while (c.moveToNext());
                }

            } catch (Exception ex) {
                ex.printStackTrace();
            }
            finally {
                try { c.close(); } catch (Exception ignore) {}
            }

        } finally {
            try { db.close(); } catch (Exception ignore) {}
        }

        return list;
    }

}
