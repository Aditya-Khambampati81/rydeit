package com.rydeit.push;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.util.DisplayMetrics;
import com.rydeit.util.Log;
import android.widget.RemoteViews;

import com.rydeit.BuildConfig;
import com.rydeit.R;
import com.rydeit.flurry.MyEventManager;
import com.rydeit.push.database.PushPojoManager;
import com.rydeit.push.model.PushMessage;
import com.rydeit.util.AndroidUtils;
import com.rydeit.view.MapsActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.URL;
import java.util.Random;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class PushParseIntentService extends IntentService {
    // TODO: Rename actions, choose action names that describe tasks that this
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    public static final String ACTION_PARSE = "com.rydeit.push.action.PARSE_DATA";

    public static final String ACTION_CLEANUP= "com.rydeit.push.action.CLEAN_DATA";
    public static final String PAYLOAD = "payload";
    private static final int notificationId =1212;

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startParseIntentService(Context context, String pushData) {
        Intent intent = new Intent(context, PushParseIntentService.class);
        intent.setAction(ACTION_PARSE);
        intent.putExtra(PAYLOAD, pushData);
        context.startService(intent);
    }
    public static void startCleanDb(Context context) {
        Intent intent = new Intent(context, PushParseIntentService.class);
        intent.setAction(ACTION_CLEANUP);
        context.startService(intent);
    }

    private Bitmap getBitmap(String url) {
        try {

            DisplayMetrics displaymetrics = this.getResources().getDisplayMetrics();

            int height = 256;
            int width = displaymetrics.widthPixels-20;

            InputStream is = (InputStream) new URL(url).getContent();
            Bitmap d = BitmapFactory.decodeStream(is);
//            d =Bitmap.createScaledBitmap(d,d.getScaledWidth(displaymetrics),256,true);
//            d.setDensity(displaymetrics.densityDpi);

            is.close();
            return d;
        } catch (Exception e) {
            return null;
        }
    }
    public PushParseIntentService() {
        super("PushParseIntentService");
    }

    @Override
    protected void onHandleIntent(final Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_PARSE.equals(action)) {

                if(handleActionParse(intent.getStringExtra(PAYLOAD)) <= 0)
                {
                    Log.i("PushParseIntentService", "DB insert failed....");
                    return;
                }

                final PushMessage pm = convertJsonToPushMessage(intent.getStringExtra(PAYLOAD));

                if(pm == null)
                {
                    Log.i("PushParseIntentService", "Push message is empty");
                    return;
                }
                if(pm.img != null && pm.img.contains("www.dropbox.com"))
                {
                   pm.img=  pm.img.replace("www.dropbox.com","dl.dropboxusercontent.com");
                }
                NotificationManager nm =
                        (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
                Notification notification = getNotification(getApplicationContext(), pm, intent, getBitmap(pm.img));
                try {
                    nm.notify(notificationId, notification);

                } catch (SecurityException e) {
                    // Some phones throw an exception for unapproved vibration
                    notification.defaults = Notification.DEFAULT_LIGHTS | Notification.DEFAULT_SOUND;
                    nm.notify(notificationId, notification);
                }
            }
            else if(ACTION_CLEANUP.equals(action))
            {
                PushPojoManager.getInstance().deleteObsoleteMessages(AndroidUtils.getAppContext(),System.currentTimeMillis());
            }


        }
    }


    protected Notification getNotification(Context context, PushMessage pm, Intent intent , Bitmap largeIcon) {


        if (pm == null)
        {
            return null;
        }


        String title = pm.ptxt;
        String alert = pm.stxt;


        Random random = new Random();
        int contentIntentRequestCode = random.nextInt();
        int deleteIntentRequestCode = random.nextInt();

        // Security consideration: To protect the app from tampering, we require that intent filters
        // not be exported. To protect the app from information leaks, we restrict the packages which
        // may intercept the push intents.
        String packageName = context.getPackageName();


        Intent contentIntent = new Intent(context, MapsActivity.class);

        contentIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
        contentIntent.putExtra("openoffers",true);
        PendingIntent pContentIntent =
                PendingIntent.getActivity(
                        this,
                        121,
                        contentIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        //Action view
        Intent viewIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(pm.link));


        PendingIntent pView =   PendingIntent.getActivity(
                this,
               122,
                viewIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );



        //Action SHare

        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_TEXT, "Get Rydeit Offers, " + pm.link);
        shareIntent.setType("text/plain");


        PendingIntent pShare =  PendingIntent.getActivity(
                this,
                123,
                shareIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );



        NotificationCompat.BigTextStyle notiStyle = new NotificationCompat.BigTextStyle();
        notiStyle.setBigContentTitle(title);
        notiStyle.setSummaryText(alert);

        // The purpose of setDefaults(Notification.DEFAULT_ALL) is to inherit notification properties
        // from system defaults
        NotificationCompat.Builder parseBuilder = new NotificationCompat.Builder(context);
        parseBuilder.setContentTitle(title)
                .setTicker("Offers from RydeIT")
                .setSmallIcon(R.drawable.appicon)
                .setContentIntent(pContentIntent)
                .setContentText(alert)
                .setContentTitle(title)
                .setAutoCancel(true)
                .setStyle(notiStyle)
//                .addAction(android.R.drawable.ic_menu_slideshow, "VIEW", pView)
//                .addAction(android.R.drawable.ic_menu_share, "SHARE",pShare)
                .setPriority(Notification.PRIORITY_MAX)
                .setDefaults(Notification.DEFAULT_ALL);
//        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
//            parseBuilder.setStyle(notiStyle);
//        }

        Notification myNotification =  parseBuilder.build();
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            RemoteViews expandedView = new RemoteViews(this.getPackageName(),
                    R.layout.notification_custom_remote);
            expandedView.setImageViewBitmap(R.id.niv,largeIcon);
            expandedView.setTextViewText(R.id.title, title);
            expandedView.setTextViewText(R.id.content,alert);
            myNotification.bigContentView = expandedView;
        }
        return myNotification;
    }

    public static final int SMALL_NOTIFICATION_MAX_CHARACTER_LIMIT = 38;
    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private long handleActionParse(String pushData) {

        //Parse the message content and then store the values IN DB
        long id = -1;
        Log.i("TAG", "Push data received from server is :" + pushData);
        PushMessage pM = convertJsonToPushMessage(pushData);
        if (pM != null) {
            try {

                id = PushPojoManager.getInstance().insert(this, pM);
                Log.i("TAG", "inserted row id is :" + id);

            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return id;
    }


    public static PushMessage convertJsonToPushMessage(String pushData) {
        JSONObject pushJson = null;
        try {
            pushJson = new JSONObject(pushData);
        } catch (JSONException ex) {
            ex.printStackTrace();
            return null;
        }


        Log.i("TAG", "Printing Json object : "+ pushJson.toString());

        // primary text
        PushMessage pMessage = new PushMessage();
        try {
            if (pushJson.has("ptxt")) {
                pMessage.ptxt = pushJson.getString("ptxt");

            }
        } catch (JSONException ex) {
            ex.printStackTrace();
        }


        // type
        try {
            if (pushJson.has("type")) {
                pMessage.type = pushJson.getString("type");
            }
        } catch (JSONException ex) {
            ex.printStackTrace();
        }
        //secondary text
        try {
            if (pushJson.has("stxt")) {
                pMessage.stxt = pushJson.getString("stxt");

            }
        } catch (JSONException ex) {
            ex.printStackTrace();
        }
        //expiry in hrs
        try {
            if (pushJson.has("expiry")) {
                pMessage.expiry = pushJson.getInt("expiry");
            }
        } catch (JSONException ex) {
            ex.printStackTrace();
        }
        // url of image
        try {
            if (pushJson.has("link")) {
                pMessage.link = pushJson.getString("link");
            }
        } catch (JSONException ex) {
            ex.printStackTrace();
        }

        // url of image
        try {
            if (pushJson.has("img")) {
                pMessage.img = pushJson.getString("img");
            }
        } catch (JSONException ex) {
            ex.printStackTrace();
        }

        return pMessage;
    }

}
