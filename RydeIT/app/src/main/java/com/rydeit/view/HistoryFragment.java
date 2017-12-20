package com.rydeit.view;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.rydeit.R;
import com.rydeit.api.uber.UberAPIClient;
import com.rydeit.api.uber.UberCallback;
import com.rydeit.database.DatabaseUtil;
import com.rydeit.model.common.MyBooking;
import com.rydeit.model.uber.History;
import com.rydeit.model.uber.UserActivity;
import com.rydeit.push.database.PushDatabaseHelper;
import com.rydeit.push.database.PushPojoManager;
import com.rydeit.uilibrary.BaseFragment;
import com.rydeit.util.Constants;
import com.rydeit.util.Log;
import com.rydeit.view.common.HistoryAdapter;
import com.rydeit.view.common.UberAuthFragment;

import java.util.ArrayList;

import retrofit.client.Response;


public class HistoryFragment extends BaseFragment {
    private View mRootView;

    public static final String ARG_SECTION_NUMBER = "section_number";
    private static HistoryFragment mHistory;
    private MyObserver historyObserver = null;

    ArrayList<MyBooking> mCabBookings;

    public HistoryFragment() {
        // Required empty public constructor
    }


    public static HistoryFragment getInstance(int sectionNumber) {

        if(mHistory == null ) {
            mHistory = new HistoryFragment();
            mHistory.setRetainInstance(true);
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            mHistory.setArguments(args);
        }

        return mHistory;
    }

    private LinearLayout progressList;
    private RecyclerView mRecyclerView;
    TextView tv = null;
    private HistoryAdapter mAdapter = null;


    @Override
    public void onAttach(Activity activity) {

        super.onAttach(activity);
        // This will update the title in navigation bar.
        if(activity instanceof  MapsActivity) {
            ((MapsActivity)activity).onSectionAttached(
                    getArguments().getInt(ARG_SECTION_NUMBER));
        }

    }
    public static void tearDown()
    {
        mHistory = null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mRootView = inflater.inflate(R.layout.fragment_history, container, false);
        tv = (TextView) mRootView.findViewById(R.id.no_result_text);
        progressList = (LinearLayout) mRootView.findViewById(R.id.progress_list);
        mRecyclerView = (RecyclerView)mRootView.findViewById(R.id.historylist);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity().getApplicationContext()));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        queryBookingHistory();
        return mRootView;

    }

    private void bindDatatoViews(ArrayList<MyBooking> myBookings)
    {
        try {
            if(myBookings != null && myBookings.size()>0){

                if(mCabBookings==null)
                    mCabBookings=myBookings;
                else
                    mCabBookings.addAll(myBookings);

                //tv.setVisibility(View.VISIBLE);
                if(mAdapter == null)
                    mAdapter = new HistoryAdapter(this.getActivity(), mCabBookings,1);
                int i=mAdapter.getItemCount();
                mRecyclerView.setAdapter(mAdapter);

                if(mCabBookings != null && mCabBookings.size()>0){
                    tv.setVisibility(View.INVISIBLE);
                    mAdapter.notifyData(mCabBookings);
                }
                else{
                    tv.setVisibility(View.VISIBLE);
                    progressList.setVisibility(View.INVISIBLE);
                }
            }
            if(mCabBookings != null && mCabBookings.size()>0) {
                progressList.setVisibility(View.INVISIBLE);
                tv.setVisibility(View.INVISIBLE);
            }
            else {
                tv.setVisibility(View.VISIBLE);
                progressList.setVisibility(View.INVISIBLE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static final String TAG = MyProfileFragment.class.getSimpleName();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        historyObserver  = new MyObserver(mHandler);
        getActivity().getContentResolver().registerContentObserver(DatabaseUtil.URI_HISTORY, false, historyObserver);
        if(mCabBookings!=null)
            mCabBookings.clear();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    void queryBookingHistory(){
        if(mCabBookings!=null)
            mCabBookings.clear();
        progressList.setVisibility(View.VISIBLE);
        //From DB
        //FIXME : query should not be on main thread so added limit in query field.
        ArrayList<MyBooking> myBookings=DatabaseUtil.getAllRideList(this.getActivity());
        bindDatatoViews(myBookings);

        //From Uber End point APis
        //getUberBookingHistory();
    }

    void adaptUberHistory(UserActivity userActivity){
        if(userActivity != null && userActivity.getHistory()!= null && !userActivity.getHistory().isEmpty()){
            ArrayList<MyBooking> myBookings=new ArrayList<>();
            for(History history: userActivity.getHistory()){
                MyBooking myBooking = new MyBooking();
                myBooking.pickUpAddress=history.getStart_City().getCityName();//TODO gives city name, not the address
                myBooking.booking_status=history.getStatus();
                //String starttime = new SimpleDateFormat("dd/MMM HH:mm").format(history.getStart_time());
                myBooking.pickupTime=history.getStart_time();
                myBooking.crn=history.getRequest_id();
                myBooking.cabCompany= Constants.CAB_GLOBAL.UBER.toString();
                myBookings.add(myBooking);
            }

            if(myBookings!=null && myBookings.size()>0)
                bindDatatoViews(myBookings);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(historyObserver!=null)
            getActivity().getContentResolver().unregisterContentObserver(historyObserver);
        if(mCabBookings!=null)
            mCabBookings.clear();
    }

    @Override
    public void onDialogTimedOut(int reqCode) {

        //For any progress dialog if timeout happens then control will come from base class
        // you can show dialog here.

    }

    @Override
    public void processCustomMessage(Message msg) {

        // IN base activity there is  public DialogHandlerActivity mActivityHandler = null;
        // instead of creating a handler you can reuse it and message that cannot be handled in base class will come here.
        switch (msg.what) {
            case MyObserver.MSG_RELOAD_UI:
                //We will have to change it later to background thread.
                queryBookingHistory();

                break;
            default:
                break;


        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void getUberBookingHistory(){
        //progressList.setVisibility(View.VISIBLE);
        UberAPIClient.getUberV1_2APIClient().getUserActivity(getAccessToken(),
                0,
                10,
                new UberCallback<UserActivity>() {
                    @Override
                    public void success(UserActivity userActivity, Response response) {

                        adaptUberHistory(userActivity);
                    }
                });
    }

    private String getAccessToken() {
        return UberAuthFragment.getTokenType() + " " + UberAuthFragment.getAccessToken();
    }
}
