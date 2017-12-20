package com.rydeit.push.database;

import android.app.DownloadManager;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Parcel;
import android.support.annotation.NonNull;
import android.widget.Switch;

import com.rydeit.push.model.PushMessage;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Aditya.Khambampati on 11/1/2015.
 */
public class PushPojoManager {

    private static PushPojoManager pPojoManager = null;
    public static final Uri URI_MY_TABLE = Uri.parse("sqlite://com.rydeit/messages");
    public static final Object lock = new Object();

    public static PushPojoManager getInstance() {
        synchronized (lock) {
            if (pPojoManager == null) {
                pPojoManager = new PushPojoManager();
            }
        }
        return pPojoManager;

    }

    public long insert(Context cxt, PushMessage pm) throws IllegalAccessException {
        SQLiteDatabase db = PushDatabaseHelper.getInstance(cxt).getWritableDatabase();
        long insertedUri =0;

        if (db == null) {
            throw new IllegalAccessException("Sqlite database is null");
        }
        db.beginTransaction();


        try {

            ContentValues cv = new ContentValues();

            cv.put(PushMessageDatabase.MessageTable.TITLE, pm.ptxt);
            cv.put(PushMessageDatabase.MessageTable.MESSAGE, pm.stxt);
            cv.put(PushMessageDatabase.MessageTable.MESSAGE_TYPE, pm.type);
            cv.put(PushMessageDatabase.MessageTable.EXPIRY, (System.currentTimeMillis() + (pm.expiry * 60 * 60 * 1000)));
            cv.put(PushMessageDatabase.MessageTable.TIMESTAMP, System.currentTimeMillis());
            cv.put(PushMessageDatabase.MessageTable.URL, pm.img);
            cv.put(PushMessageDatabase.MessageTable.LINK, pm.link);

            insertedUri = db.insert(PushMessageDatabase.MessageTable.TABLE_NAME, null,cv );
            db.setTransactionSuccessful();
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            db.endTransaction();
        }

        if (insertedUri > 0)
        {
            cxt.getContentResolver().notifyChange(URI_MY_TABLE,null);
        }
        return insertedUri;
    }


  //FIXME:This api can be added later
    public List<Long> insertBatch(Context cxt , List<PushMessage> myMessages) throws IllegalAccessException
    {

        SQLiteDatabase db = PushDatabaseHelper.getInstance(cxt).getWritableDatabase();
        List<Long> insertedUriList  = new ArrayList<Long>();

        if (db == null) {
            throw new IllegalAccessException("Sqlite database is null");
        }

        for (PushMessage pm :myMessages ) {

            try {
                db.beginTransaction();
                ContentValues cv = new ContentValues();

                cv.put(PushMessageDatabase.MessageTable.TITLE, pm.ptxt);
                cv.put(PushMessageDatabase.MessageTable.MESSAGE, pm.stxt);
                cv.put(PushMessageDatabase.MessageTable.MESSAGE_TYPE, pm.type);
                cv.put(PushMessageDatabase.MessageTable.EXPIRY, (System.currentTimeMillis() + (pm.expiry * 60 * 60 * 1000)));
                cv.put(PushMessageDatabase.MessageTable.TIMESTAMP, System.currentTimeMillis());
                cv.put(PushMessageDatabase.MessageTable.URL, pm.img);
                cv.put(PushMessageDatabase.MessageTable.LINK, pm.link);

                Long insertedUri = db.insert(PushMessageDatabase.MessageTable.TABLE_NAME, null, cv);
                insertedUriList.add(insertedUri);

                db.setTransactionSuccessful();
                db.endTransaction();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        cxt.getContentResolver().notifyChange(URI_MY_TABLE,null);
        return insertedUriList;

    }

    /**
     * get all messages that are currently active
     * @param cxt
     * @return
     */
    public Cursor getAllActiveMessages(Context cxt, long timeInMills , int limit , boolean includeFlipkart) {

        SQLiteDatabase db = PushDatabaseHelper.getInstance(cxt).getWritableDatabase();
        String[] argsflip = {Long.toString(timeInMills),"PROMO_FLIP" };

        String[] args = {Long.toString(timeInMills) };

        if(!includeFlipkart)
            return db.query(PushMessageDatabase.MessageTable.TABLE_NAME, null,
                    (PushMessageDatabase.MessageTable.EXPIRY + ">=" + "?"  + " AND " + PushMessageDatabase.MessageTable.MESSAGE_TYPE + "!="+ "?" ), argsflip, null, null, null,Integer.toString(limit));
        else
            return db.query(PushMessageDatabase.MessageTable.TABLE_NAME, null,
                    (PushMessageDatabase.MessageTable.EXPIRY + ">=" + "?"   ), args, null, null, null,Integer.toString(limit));

    }

    /**
     * get all messages that are currently active
     * @param cxt
     * @return
     */
    public Cursor getFlipkartMessages(Context cxt, long timeInMills , int limit ) {

        SQLiteDatabase db = PushDatabaseHelper.getInstance(cxt).getWritableDatabase();
        String[] args = {Long.toString(timeInMills) ,"PROMO_FLIP"};

         return db.query(PushMessageDatabase.MessageTable.TABLE_NAME, null,
                    (PushMessageDatabase.MessageTable.EXPIRY + ">=" + "?"  + " AND " + PushMessageDatabase.MessageTable.MESSAGE_TYPE + "="+ "?"   ), args, null, null, null,Integer.toString(limit));

    }


    public int deleteObsoleteMessages(Context cxt, long time) {

        SQLiteDatabase db = PushDatabaseHelper.getInstance(cxt).getWritableDatabase();
        int rows = -1;

        db.beginTransaction();
        try {
            String[] whereArgs = {Long.toString(time)};
            rows = db.delete(PushMessageDatabase.MessageTable.TABLE_NAME, PushMessageDatabase.MessageTable.EXPIRY +  "<" + "?", whereArgs);
            db.setTransactionSuccessful();
        } catch (Exception ex) {
            ex.printStackTrace();
            ;

        } finally {
          db.endTransaction();
        }
        if (rows > 0)
        {
            cxt.getContentResolver().notifyChange(URI_MY_TABLE,null);
        }
        return rows;
    }


}
