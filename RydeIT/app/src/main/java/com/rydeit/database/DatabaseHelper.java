package com.rydeit.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.rydeit.push.database.PushMessageDatabase;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Aditya Khambampati
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = DatabaseHelper.class.getSimpleName();

    public static final String DB_NAME="securerydeit.db" ;

    public static final String DB_CREATE_FILENAME= "databases/sql/CREATE_VERSION_1.sql";


    private static DatabaseHelper pDatabaseHelper = null;

    private final static int DB_VERSION = 1;
    private Context mContext = null;


    private DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        mContext = context;
    }

    public static DatabaseHelper getInstance(Context context) {
        if (pDatabaseHelper == null) {
            pDatabaseHelper = new DatabaseHelper(context);
        }
        return pDatabaseHelper;
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        onUpgrade(db, 0, DB_VERSION);
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
                createBaseDBTables(db);
                createPushMessageTables(db);
                break;

            default:
                throw new IllegalStateException("Don't know how to upgrade to version " + version);
        }
    }

    private void createBaseDBTables(SQLiteDatabase db) {

        execSqlFromAssets(DB_CREATE_FILENAME,db);
    }

    public void execSqlFromAssets(String filepath, SQLiteDatabase db) {
        if(db == null){
            Log.e(TAG, "No SQLiteDatabase object found");
            return;

        }
        InputStream input;
        String text;
        try {
            input = mContext.getAssets().open(filepath);
            int size = input.available();
            byte[] buffer = new byte[size];
            input.read(buffer);
            input.close();
            text = new String(buffer);
            String[] inserts = text.split(";");
            for (String insert : inserts) {
                try {
                    db.execSQL(insert);
                } catch (Exception e) {
                    String err = (e.getMessage() == null) ? "Cant execute sql"
                            + insert : e.getMessage();
                    Log.e(TAG, err);
                }
            }

        } catch (IOException e) {
            Log.e(TAG, "execSqlFromAssets: " + filepath + " file not found");
        }

    }


    private void createPushMessageTables(SQLiteDatabase db) {

        db.execSQL(PushMessageDatabase.MessageTable.getCreateQuery());

    }


}
