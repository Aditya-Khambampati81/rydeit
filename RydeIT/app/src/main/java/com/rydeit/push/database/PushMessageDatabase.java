package com.rydeit.push.database;

import android.provider.BaseColumns;
/**
 * Created by Aditya.Khambampati on 10/30/2015.
 */
public class PushMessageDatabase {

        public static final String DATABASE_NAME ;

        static{
            DATABASE_NAME = "PushMessage.db";
        }

        /**
         * Column specs for the Inbox DB table1 [Main table]
         */
        public static final class MessageTable implements BaseColumns {

            public static final String TABLE_NAME ;

            //Columns
            public static final String PRIMARY_ID ;
            public static final String TITLE ;
            public static final String MESSAGE ;
            public static final String MESSAGE_TYPE ;
            public static final String URL ;
            public static final String TIMESTAMP;
            public static final String EXPIRY;
            public static final String LINK;

            static{
                TABLE_NAME = "Messages";
                //Columns
                PRIMARY_ID = "_id";
                TITLE = "title";
                MESSAGE = "message";
                MESSAGE_TYPE = "messageType";
                URL = "url";
                TIMESTAMP = "timestamp";
                EXPIRY = "expiry";
                LINK = "link";
            }

            public static String getCreateQuery() {
                return "Create table IF NOT EXISTS "
                        + TABLE_NAME
                        + "( " + PRIMARY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                        + MESSAGE + " TEXT UNIQUE,"
                        + MESSAGE_TYPE + " TEXT,"
                        + TITLE + " TEXT,"
                        + URL + " TEXT ,"
                        + TIMESTAMP + " INTEGER ,"
                        + EXPIRY + " INTEGER ,"
                        + LINK + " TEXT"
                        + ");";
            }

            public static String getDropQuery() {
                return "DROP TABLE IF EXISTS " + TABLE_NAME;
            }
        }


}
