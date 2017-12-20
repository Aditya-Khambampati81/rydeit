package com.rydeit.util;

/**
 * Created by Aditya.Khambampati on 12/17/2015.
 * This is logger class to enable or disable logging
 *
 */
public class Log {
   private static boolean enableLogging = false;

    public static void  setLogging(boolean logging)
    {
        enableLogging= logging;
    }

    public static void i(String tag, String value)
    {
        if(enableLogging)
        {
            android.util.Log.i(tag,value);
        }
    }
    public static void e(String tag, String value)
    {
        if(enableLogging)
        {
            android.util.Log.e(tag, value);
        }
    }
    public static void v(String tag, String value)
    {
        if(enableLogging)
        {
            android.util.Log.v(tag, value);
        }
    }
    public static void d(String tag, String value)
    {
        if(enableLogging)
        {
            android.util.Log.d(tag,value);
        }
    }


}
