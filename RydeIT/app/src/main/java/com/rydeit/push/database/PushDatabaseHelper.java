package com.rydeit.push.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Aditya.Khambampati on 10/30/2015.
 */
public class PushDatabaseHelper extends SQLiteOpenHelper {

    private static PushDatabaseHelper pDatabaseHelper = null;

    private final static int DATABASE_VERSION = 1;
    private final static String DATABASE_NAME = PushMessageDatabase.DATABASE_NAME;
    private Context mContext = null;


    private PushDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        mContext = context;
    }

    public static PushDatabaseHelper getInstance(Context context) {
        if (pDatabaseHelper == null) {
            pDatabaseHelper = new PushDatabaseHelper(context);
        }
        return pDatabaseHelper;
    }






    @Override
    public void onCreate(SQLiteDatabase db) {
        onUpgrade(db, 0, DATABASE_VERSION);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        for (int version = oldVersion + 1; version <= newVersion; version++) {
            upgradeTo(db, version);
        }
    }

    private void upgradeTo(SQLiteDatabase db, int version) {
        switch (version) {
            case 1:
                createPushMessageTables(db);
                break;

            default:
                throw new IllegalStateException("Don't know how to upgrade to version " + version);
        }
    }


    private void createPushMessageTables(SQLiteDatabase db) {

        db.execSQL(PushMessageDatabase.MessageTable.getCreateQuery());

    }

}
