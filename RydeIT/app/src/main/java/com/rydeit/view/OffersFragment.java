package com.rydeit.view;


import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.android.volley.toolbox.NetworkImageView;
import com.rydeit.R;
import com.rydeit.push.PromoAdapter;
import com.rydeit.push.database.PushMessageDatabase;
import com.rydeit.push.database.PushPojoManager;
import com.rydeit.push.model.PushMessage;
import com.rydeit.uilibrary.BaseFragment;
import com.rydeit.util.AndroidUtils;
import com.rydeit.util.Log;
import com.rydeit.volley.VolleySingleton;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by Aditya Khambampati
 */
public class OffersFragment extends BaseFragment implements PromoAdapter.PromoAdapterCallback {

   // private static OffersFragment sInstance = null;
    public static final String ARG_SECTION_NUMBER = "section_number";

    private PromoAdapter mAdapter = null;
    private TextView tv;
    private ViewFlipper vf = null;
    int MIN_FLIP_PROMOS = 8;
    //Flipkart messages
    private List<PushMessage> mMessages = new ArrayList<PushMessage>();
    //Rydeit promos
    private List<PushMessage> rydeitPromos = new ArrayList<PushMessage>();

    private final GestureDetector detector = new GestureDetector(new SwipeGestureDetector());

    private static final int SWIPE_MIN_DISTANCE = 120;
    private static final int SWIPE_THRESHOLD_VELOCITY = 200;
    private List<ImageView> ivList = new ArrayList<ImageView>();
    private static final int MSG_CLICK = 121;
    private LinearLayout ll =null;

    public static OffersFragment getInstance(int sectionNumber) {

//        if (sInstance == null) {
//            sInstance = new OffersFragment();
//            //sInstance.setRetainInstance(true);
//            Bundle args = new Bundle();
//            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
//            sInstance.setArguments(args);
//        }

        return new OffersFragment();
    }


    @Override
    public void onPause() {

      //  this.getActivity().startService(new Intent(this.getActivity(), DrawTopService.class));
        super.onPause();
    }







    private MyObserver contentObserver = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //FIXME take query part in to background thread , make sure that onCreateview things are alright before that
        loadFlipkartData();

        contentObserver = new MyObserver(mHandler);
        getActivity().getContentResolver().registerContentObserver(PushPojoManager.URI_MY_TABLE, false, contentObserver);
        new Thread() {
            @Override
            public void run() {
                loadDataFromDb(false);
            }
        }.start();
    }


    @Override
    public void onDestroy() {
        getActivity().getContentResolver().unregisterContentObserver(contentObserver);
        super.onDestroy();
    }

    /**
     * API to fetch data from DB , it supports few flags please read documentation carefully.
     *
     * @param includeflipkart  -- whether to include flipkart promotions here
     *
     */
    public void loadDataFromDb(boolean includeflipkart) {
        rydeitPromos.clear();
        Cursor c = PushPojoManager.getInstance().getAllActiveMessages(this.getActivity(), System.currentTimeMillis(),MIN_FLIP_PROMOS,includeflipkart);
        if (c != null) {
            if (c.moveToFirst()) {
                while (!c.isAfterLast()) {
                    PushMessage pm = new PushMessage(c.getString(c.getColumnIndex(PushMessageDatabase.MessageTable.TITLE)), c.getString(c.getColumnIndex(PushMessageDatabase.MessageTable.MESSAGE)),
                            c.getInt(c.getColumnIndex(PushMessageDatabase.MessageTable.EXPIRY)), c.getString(c.getColumnIndex(PushMessageDatabase.MessageTable.URL)), c.getString(c.getColumnIndex(PushMessageDatabase.MessageTable.MESSAGE_TYPE)),
                            c.getString(c.getColumnIndex(PushMessageDatabase.MessageTable.LINK)));
                    rydeitPromos.add(pm);
                    c.moveToNext();
                }
            }
            c.close();

        }
        if (!AndroidUtils.appInstalledOrNot("com.flipkart.android", getActivity().getApplicationContext())) {
            rydeitPromos.add(new PushMessage("Get Flipkart App", "", 1024, "NA", "PRM", "http://affiliate.flipkart.com/install-app?affid=reachryde"));
        }
        mHandler.sendMessage(mHandler.obtainMessage(MSG_UPDATE_UI_STATUS));

        //First time the adapter will be null , later when we move to background thread we will have to change it
        if (mAdapter != null)
            mAdapter.notifyData(rydeitPromos);
    }


    /**
     * API to fetch data from DB , it supports few flags please read documentation carefully.
     *
     */
    public void loadFlipkartData() {
        mMessages.clear();
        Cursor c = PushPojoManager.getInstance().getFlipkartMessages(this.getActivity(), System.currentTimeMillis(), MIN_FLIP_PROMOS);
        if (c != null) {
            if (c.moveToFirst()) {
                while (!c.isAfterLast()) {
                    PushMessage pm = new PushMessage(c.getString(c.getColumnIndex(PushMessageDatabase.MessageTable.TITLE)), c.getString(c.getColumnIndex(PushMessageDatabase.MessageTable.MESSAGE)),
                            c.getInt(c.getColumnIndex(PushMessageDatabase.MessageTable.EXPIRY)), c.getString(c.getColumnIndex(PushMessageDatabase.MessageTable.URL)), c.getString(c.getColumnIndex(PushMessageDatabase.MessageTable.MESSAGE_TYPE)),
                            c.getString(c.getColumnIndex(PushMessageDatabase.MessageTable.LINK)));
                    mMessages.add(pm);
                    c.moveToNext();
                }
            }
            c.close();
        }
    }
    @Override
    public void onResume() {
        super.onResume();
        this.getActivity().stopService(new Intent(this.getActivity(), DrawTopService.class));
    }

    @Override
    public void onAttach(Activity activity) {

        super.onAttach(activity);
        // This will update the title in navigation bar.

    }

    @Override
    public void onDialogTimedOut(int reqCode) {

    }
    private static final int MSG_UPDATE_UI_STATUS =1009;
    @Override
    public void processCustomMessage(Message messageWhat) {
        switch (messageWhat.what) {

            case  MSG_UPDATE_UI_STATUS:

            if (rydeitPromos.size() == 0) {
                tv.setVisibility(View.VISIBLE);
                progressList.setVisibility(View.INVISIBLE);

            } else {
                tv.setVisibility(View.INVISIBLE);
                progressList.setVisibility(View.VISIBLE);
                if(rydeitPromos!=null)
                    mAdapter.notifyData(rydeitPromos);
            }
            break;
            case MyObserver.MSG_RELOAD_UI:
                //We will have to change it later to background thread.
                Log.i("TAG", "Reloading ui from loadDatacall");
                loadFlipkartData();
                // update flipkart views
                updateFlipkartViews();
                //update our promos.
                loadDataFromDb(false);
                break;
            case MSG_CLICK:

                //view flipper click
                if (vf != null) {
                    View currentView = ((ViewFlipper) vf).getCurrentView();
                    if (currentView != null) {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse((String) currentView.getTag()));
                        if(AndroidUtils.appInstalledOrNot("com.flipkart.android", getActivity().getApplicationContext()))
                            intent.setPackage("com.flipkart.android");
                        OffersFragment.this.getActivity().startActivity(intent);
                    }
                }


                break;

            default:
                ;
        }

    }

    public OffersFragment() {
        // Required empty public constructor
    }

    private LinearLayout progressList;
    private RecyclerView mRecyclerView;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View mRootView = inflater.inflate(R.layout.fragment_offers, container, false);
        vf = (ViewFlipper) mRootView.findViewById(R.id.flipkartflipper);
        ll = (LinearLayout) mRootView.findViewById(R.id.crumbs);

       updateFlipkartViews();

//        mRootView.findViewById(R.id.offer_btn).setSelected(true);
//
//        mRootView.findViewById(R.id.offer_btn).setBackgroundResource(R.color.white);
//        mRootView.findViewById(R.id.offer_btn_selected).setBackgroundResource(R.color.hex_gray);
//        mRootView.findViewById(R.id.map_selected).setBackgroundResource(R.color.white);
//
//        mRootView.findViewById(R.id.map_btn).setOnClickListener(
//                new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//                        getActivity().getSupportFragmentManager().beginTransaction()
//                                .show(MapCabFinder.getInstance(1))
//                                .hide(OffersFragment.getInstance(1))
//                                .commit();
//                    }
//                }
//        );


        tv = (TextView) mRootView.findViewById(R.id.no_result_text);
        progressList = (LinearLayout) mRootView.findViewById(R.id.progress_list);
        mRecyclerView = (RecyclerView) mRootView.findViewById(R.id.rvNetLocationList);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity().getApplicationContext()));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        mAdapter = new PromoAdapter(this.getActivity(), rydeitPromos, this);
        mRecyclerView.setAdapter(mAdapter);

        if (rydeitPromos != null && !rydeitPromos.isEmpty()) {
            tv.setVisibility(View.INVISIBLE);

        } else {
            mRootView.findViewById(R.id.no_result_text).setVisibility(View.VISIBLE);
        }
        if (rydeitPromos.size() == 0) {
            tv.setVisibility(View.VISIBLE);
            progressList.setVisibility(View.INVISIBLE);

        } else {
            tv.setVisibility(View.INVISIBLE);
            progressList.setVisibility(View.VISIBLE);
            mAdapter.notifyData(rydeitPromos);
        }


        return mRootView;

    }

    public static void tearDown()
    {
       // sInstance= null;
    }



    private void updateFlipkartViews()
    {
        if (mMessages.size() == 0)
        {
            vf.setVisibility(View.GONE);
            vf.setClickable(false);
            ll.setVisibility(View.GONE);
        }
        else
        {
            vf.setVisibility(View.VISIBLE);
            vf.setClickable(true);
            ll.setVisibility(View.VISIBLE);

            if (mMessages.size() < MIN_FLIP_PROMOS) {
                MIN_FLIP_PROMOS = mMessages.size();
            }
            ll.removeAllViews();
            //Add breadcrumbs view here for flipkart promos
            for (int i = 0; i < MIN_FLIP_PROMOS; i++) {
                ImageView iv = new ImageView(this.getActivity());
                if (i == 0)
                    iv.setImageResource(R.drawable.crumbwhite);
                else
                    iv.setImageResource(R.drawable.crumb);
                iv.setPadding(3, 3, 3, 3);
                ivList.add(iv);
                ll.addView(iv);
            }
            ll.invalidate();


            List<View> flipkartViews = convertFlipkartViewsToFlipperContent();

            if(flipkartViews!= null && flipkartViews.size()>0){
                int k = flipkartViews.size()-1;
                for (View v : flipkartViews) {

                    vf.addView(v);
                    vf.setDisplayedChild(k);
                    k--;
                }
            }


            vf.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(final View view, final MotionEvent event) {
                    detector.onTouchEvent(event);
                    return true;
                }
            });

        }
    }


    List<View> convertFlipkartViewsToFlipperContent( ) {
        List<View> views = new ArrayList<View>();
        for (int i = 0; i < MIN_FLIP_PROMOS; i++) {
            LayoutInflater inflater = getActivity().getLayoutInflater();
            View v = (View) inflater.inflate(R.layout.flippercontent, null, false);
            PushMessage pm = mMessages.get(i);
            // bind flipkart data here
            TextView tv1 = (TextView) v.findViewById(R.id.tv1);
            TextView tv2 = (TextView) v.findViewById(R.id.tv2);
            NetworkImageView iv = (NetworkImageView) v.findViewById(R.id.promoimage);

            iv.setImageUrl(pm.img, VolleySingleton.getInstance().getImageLoader());
            tv1.setText(pm.stxt);
            tv2.setText(pm.ptxt);
            v.setTag(pm.link);
            views.add(v);

        }


        return views;


    }

    @Override
    public void onSortFinished() {
        progressList.setVisibility(View.INVISIBLE);
        if (mMessages.size() == 0) {
            tv.setVisibility(View.VISIBLE);
        }
    }


    class SwipeGestureDetector extends GestureDetector.SimpleOnGestureListener {


        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            try {
                 // right to left swipe
                if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                    vf.setInAnimation(AnimationUtils.loadAnimation(AndroidUtils.getAppContext(), android.R.anim.fade_in));
                    vf.setOutAnimation(AnimationUtils.loadAnimation(AndroidUtils.getAppContext(), android.R.anim.fade_out));
                    vf.showNext();

                } else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                    vf.setInAnimation(AnimationUtils.loadAnimation(AndroidUtils.getAppContext(), android.R.anim.fade_in));
                    vf.setOutAnimation(AnimationUtils.loadAnimation(AndroidUtils.getAppContext(), android.R.anim.fade_out));
                    vf.showPrevious();

                }

                ll.removeAllViews();
                //Add breadcrumbs view here for flipkart promos
                for (int i = 0; i < MIN_FLIP_PROMOS; i++) {
                    ImageView iv = new ImageView(OffersFragment.this.getActivity());
                    if (i == (vf.getDisplayedChild()% MIN_FLIP_PROMOS))
                        iv.setImageResource(R.drawable.crumbwhite);
                    else
                        iv.setImageResource(R.drawable.crumb);
                    iv.setPadding(3, 3, 3, 3);
                    ivList.add(iv);
                    ll.addView(iv);
                }
                ll.invalidate();
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }

            return true;
        }

        @Override
        public boolean onSingleTapUp(MotionEvent event) {
            // Toast.makeText(OffersFragment.this.getActivity(),"Single tap UP",Toast.LENGTH_SHORT).show();

            return true;
        }

        @Override
        public boolean onDoubleTapEvent(MotionEvent event) {
            // Toast.makeText(OffersFragment.this.getActivity(),"DOule tap",Toast.LENGTH_SHORT).show();
            return true;
        }


        @Override
        public boolean onSingleTapConfirmed(MotionEvent event) {
            //   Toast.makeText(OffersFragment.this.getActivity(),"Confirmed tap",Toast.LENGTH_SHORT).show();
            mHandler.sendMessage(mHandler.obtainMessage(MSG_CLICK));
            return true;
        }
    }




}
