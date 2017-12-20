package com.rydeit.view.common;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.rydeit.BuildConfig;
import com.rydeit.R;
import com.rydeit.database.DatabaseUtil;
import com.rydeit.model.common.MyBooking;
import com.rydeit.util.Constants;
import com.rydeit.util.JavaUtil;
import com.rydeit.view.TrackMyRideActivity;

import java.util.List;

/**
 * Created by Aditya.Khambampati on 11/12/2015.
 */

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryCardHolder> {

    private List<MyBooking> MyBookingList;

    private Context mContext;
    public static final String DATE_FORMAT = "dd/MMM 'at' hh:mm a";
    public static final String HOUR_FORMAT = "hh:mm";


    public HistoryAdapter(Context context, List<MyBooking> myBookingListlist, int type) {
        this.mContext = context;
        this.MyBookingList = myBookingListlist;
    }

    public void notifyData(List<MyBooking> myBookingListlist) {
        MyBookingList = myBookingListlist;
        if (myBookingListlist!=null && myBookingListlist.size() >0)
            notifyDataSetChanged();

    }

    @Override
    public HistoryCardHolder onCreateViewHolder(ViewGroup viewGroup, int i) {

        MyBooking myBooking=null;
        View view=null;
        if(MyBookingList!=null && MyBookingList.get(i)!=null)
            myBooking=MyBookingList.get(i);

        if(true){//myBooking.booking_status.equals(OlaAPIConstants.BOOKING_STATUS.COMPLETED)){
            view = LayoutInflater.from(mContext).inflate(R.layout.my_history_row, viewGroup, false);
        }else/* if(myBooking.status==2)*/{//TODO
            view = LayoutInflater.from(mContext).inflate(R.layout.my_history_row, viewGroup, false);
        }

        return new HistoryCardHolder(view);
    }

    @Override
    public void onBindViewHolder(HistoryCardHolder viewHolder, final int i) {

        Log.i("TAG", "Bind view called for I :" + i);
        try {
            MyBooking myBooking=null;

            if(MyBookingList!=null && MyBookingList.get(i)!=null)
                myBooking=MyBookingList.get(i);

            if(myBooking!=null){
                if(myBooking.cabCompany.equals(Constants.CAB_INDIA.OLA.toString())) {
                    viewHolder.cabcompanyimage.setImageResource(R.drawable.ola_icon);
                    viewHolder.tripRefId.setVisibility(View.VISIBLE);
                    viewHolder.tripRefId.setText("CRN "+myBooking.crn);
                }
                else if(myBooking.cabCompany.equals(Constants.CAB_GLOBAL.UBER.toString())) {
                    viewHolder.cabcompanyimage.setImageResource(R.drawable.uber_icon_36px);
                    viewHolder.tripRefId.setVisibility(View.GONE);
                }

                viewHolder.cabtype.setImageResource(R.drawable.cabicon_uberx);
                if(myBooking.booking_status!=null) {

                    /*if(myBooking.booking_status.toUpperCase().compareTo("CANCELLED") == 0 )
                    {
                        viewHolder.tripstatus.setTextColor(mContext.getResources().getColor(android.R.color.holo_blue_dark));
                        viewHolder.tripstatus.setText(myBooking.booking_status.toUpperCase());
                    }
                    else if((!(myBooking.booking_status.toUpperCase().compareTo("COMPLETED") == 0 ))&&
                            (!(myBooking.booking_status.toUpperCase().compareTo("SIMULATION") == 0 ))) {
                        viewHolder.tripstatus.setTextColor(mContext.getResources().getColor(R.color.actionbar_title_txt_color));
                        viewHolder.tripstatus.setText(myBooking.booking_status.toUpperCase());
                    }*/
                    viewHolder.tripstatus.setText(myBooking.booking_status.toUpperCase());
                    if((!(myBooking.booking_status.toUpperCase().compareTo("COMPLETED") == 0 ))
                            && (!(myBooking.booking_status.toUpperCase().compareTo("SIMULATION") == 0 ))
                            && (!(myBooking.booking_status.toUpperCase().compareTo("CANCELLED") == 0 ))) {
                        viewHolder.tripstatus.setTextColor(mContext.getResources().getColor(R.color.green_dark));
                    }
                    else {
                        viewHolder.tripstatus.setTextColor(mContext.getResources().getColor(R.color.hex_gray));
                    }

                }
                viewHolder.tripPickupAddress.setText(myBooking.pickUpAddress);

                if(myBooking.pickupTime!=null){
                    String starttime = JavaUtil.getDate(myBooking.pickupTime, DATE_FORMAT);
                    viewHolder.tripPickuptime.setText(starttime.toUpperCase());
                }


               if(BuildConfig.DEBUG ){
                    viewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View arg0) {
                            MyBooking myBookingInProgress = null;
                            if (MyBookingList != null && MyBookingList.get(i) != null)
                                myBookingInProgress = MyBookingList.get(i);
                            Toast.makeText(mContext, "Deleting booking id :" + myBookingInProgress.crn, Toast.LENGTH_SHORT).show();
                            if (myBookingInProgress != null) {
                                try {
                                    DatabaseUtil.deleteRydeInfo(mContext, myBookingInProgress.crn);
                                } catch (IllegalAccessException ex) {
                                    ex.printStackTrace();
                                }

                            }

                            //Delete row items

                            return true;
                        }


                    });
                }
                viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        MyBooking myBookingInProgress = null;
                        if (MyBookingList != null && MyBookingList.get(i) != null)
                            myBookingInProgress = MyBookingList.get(i);
                        if ((myBookingInProgress.booking_status.toUpperCase().compareTo("COMPLETED") == 0)||
                                (myBookingInProgress.booking_status.toUpperCase().compareTo("CANCELLED") == 0)) {
                            Toast.makeText(mContext, "Trip ended !! check email for more details", Toast.LENGTH_SHORT).show();
                        } else {

                            Intent intent = new Intent(mContext, TrackMyRideActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            intent.putExtra("TrackRideInProgress", myBookingInProgress);
                            mContext.startActivity(intent);
                        }
                    }
                });
            }




        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            Log.i("TAG", "Exception for  I :" + i);
        }

    }

    float roundTwoDecimals(float d) {

        int k =(int) d* 100 ;
        return k/100;
    }

    @Override
    public int getItemCount() {
        return MyBookingList == null ? 0 : MyBookingList.size();
    }


    public class HistoryCardHolder extends RecyclerView.ViewHolder {
        public ImageView cabcompanyimage;
        public ImageView cabtype;
        public TextView tripstatus;
        public TextView tripRefId;
        public TextView tripPickuptime;
        public TextView tripPickupAddress;


        public HistoryCardHolder(View itemView) {
            super(itemView);
            cabcompanyimage=(ImageView)itemView.findViewById(R.id.cabcompanyimage);
            cabtype=(ImageView)itemView.findViewById(R.id.cabtypeimage);
            tripstatus = (TextView) itemView.findViewById(R.id.tripstatus);
            tripRefId = (TextView) itemView.findViewById(R.id.refno);
            tripPickuptime = (TextView) itemView.findViewById(R.id.tripPickuptime);
            tripPickupAddress = (TextView)itemView.findViewById(R.id.pickupaddress);

        }
    }

}
