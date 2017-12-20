package com.rydeit.view;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.os.Build;
import android.os.IBinder;
import android.view.Display;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import com.rydeit.R;

/**
 * Created by Aditya Khambampati
 */

public class DrawTopService extends Service {
    public DrawTopService() {
    }
    private WindowManager windowManager;
    private ImageView chatHead;
    private static int  lastx = 0;
    private static int lasty = 20000;
    WindowManager.LayoutParams params;

    @Override public IBinder onBind(Intent intent) {
        // Not used
        return null;
    }

    @Override public void onCreate() {
        super.onCreate();

        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);


        chatHead = new ImageView(this);
        // SALE icon is not looking good.
        chatHead.setImageResource(R.drawable.offers_red);

         params = new WindowManager.LayoutParams(
               160,
                160,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        WindowManager wm = (WindowManager) this.getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            Point size = new Point();
           display.getSize(size);
            params.x = size.x;
            params.y = (int) (size.y* 0.2);
        } else {

            params.x =  display.getWidth();
            params.y  = (int) (display.getHeight()* 0.2);
        }
        params.gravity = Gravity.TOP;
        lastx = params.x;
        lasty=params.y;
        windowManager.addView(chatHead, params);
        chatHead.setOnTouchListener(new View.OnTouchListener() {
            private int initialX;
            private int initialY;
            private float initialTouchX;
            private float initialTouchY;
            private long downtime = 0;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = params.x;
                        initialY = params.y;
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        downtime = event.getEventTime();
                        return true;
                    case MotionEvent.ACTION_UP:
                       // Toast.makeText(DrawTopService.this,(event.getEventTime() - downtime)+ "Seconds",Toast.LENGTH_SHORT).show();
                        if((event.getEventTime()- downtime) <= 180)
                        {
                            // this is click event
                            Intent  launchAbout = new Intent(DrawTopService.this, SettingsProfileActivity.class);
                            launchAbout.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NEW_TASK);
                            launchAbout.putExtra(SettingsProfileActivity.LAUNCH_TYPE,SettingsProfileActivity.LAUNCH_OFFERS);
                            startActivity(launchAbout);
                        }


                        return true;

                    case MotionEvent.ACTION_MOVE:
                        params.x = initialX + (int) (event.getRawX() - initialTouchX);
                        params.y = initialY + (int) (event.getRawY() - initialTouchY);
                        lastx= params.x;
                        lasty = params.y;
                        windowManager.updateViewLayout(chatHead, params);
                        return true;
                }
                return false;
            }
        });
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (chatHead != null) windowManager.removeView(chatHead);
    }
//    public class CustomView extends View {
//
//        private Paint paint;
//
//        public CustomView(Context context) {
//            super(context);
//
//            // create the Paint and set its color
//            paint = new Paint();
//            paint.setColor(Color.BLACK);
//
//        }
//
//        @Override
//        protected void onDraw(Canvas canvas) {
//
//            Paint yellow = new Paint();
//            yellow.setColor(Color.YELLOW);
//            canvas.drawText("Offers",100,100,yellow);
//            canvas.drawCircle(200, 200, 100, paint);
//        }
//
//    }


}
