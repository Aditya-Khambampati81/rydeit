package com.rydeit.push;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;
import com.rydeit.R;
import com.rydeit.flurry.MyEventManager;
import com.rydeit.push.model.PushMessage;
import com.rydeit.volley.VolleySingleton;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Aditya.Khambampati on 11/2/2015.
 */

    public class PromoAdapter extends RecyclerView.Adapter<PromoAdapter.PromoCardHolder>{

        private PromoAdapterCallback callback ;
        private List<PushMessage> pushMessagesList;
        private Context mContext;


    public PromoAdapter(Context context,List<PushMessage> list,PromoAdapterCallback listener) {
            this.mContext = context;
            this.pushMessagesList = list;
            this.callback = listener;

        }

        public void notifyData(List<PushMessage> list){
            pushMessagesList = list;
            if(pushMessagesList!=null && pushMessagesList.size()>0) {
                sortedList();
            }
            else
            {
                if(callback!=null)
                    callback.onSortFinished();
            }
            notifyDataSetChanged();

        }

        @Override
        public PromoCardHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.my_list_row, viewGroup, false);
            return new PromoCardHolder(view);
        }

        @Override
        public void onBindViewHolder(PromoCardHolder viewHolder, int i) {

            final String  link = pushMessagesList.get(i).link;

            viewHolder.promoTitle.setText(pushMessagesList.get(i).ptxt);
            viewHolder.description.setText(pushMessagesList.get(i).stxt);
            if (pushMessagesList.get(i).img == null || pushMessagesList.get(i).img.compareTo("NA")==0)
            {
                viewHolder.iv.setDefaultImageResId(R.drawable.flipkart_banner);

            }
            else {
                if(pushMessagesList.get(i).img.contains("www.dropbox.com"))
                {
                    pushMessagesList.get(i).img=  pushMessagesList.get(i).img.replace("www.dropbox.com","dl.dropboxusercontent.com");
                }
                viewHolder.iv.setDefaultImageResId(R.drawable.loadingt_banner);
                viewHolder.iv.setImageUrl(pushMessagesList.get(i).img, VolleySingleton.getInstance().getImageLoader());
                //viewHolder.iv.setDefaultImageResId(R.drawable.flipkart_banner);
            }
            viewHolder.iv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                        String myLink = link;
                        if ((myLink != null && myLink.compareTo("NA") == 0) || (myLink == null)) {
                            myLink = "http://amazon.in";
                        }

                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(myLink));
                        mContext.startActivity(intent);


                        MyEventManager.getInstance().logPromoLinkEvent("pr",myLink);

                }
            });
            viewHolder.share.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (v.getId() == R.id.share) {
                        // INvoke share intent with link
                        Intent shareIntent = new Intent();
                        shareIntent.setAction(Intent.ACTION_SEND);
                        shareIntent.putExtra(Intent.EXTRA_TEXT, "Get Rydeit Offers, " + link);
                        shareIntent.setType("text/plain");
                        mContext.startActivity(Intent.createChooser(shareIntent, "RydeIT promo"));
                        MyEventManager.getInstance().logPromoLinkEvent("sr", link);
                    }
                }
            });

        }

        private class SortListTask extends AsyncTask<Void, Void, Void> {
            ArrayList tempLocationList;
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                tempLocationList = new ArrayList<PushMessage>();
                tempLocationList.addAll(pushMessagesList);
            }

            @Override
            protected Void doInBackground(Void... voids) {

                Collections.sort(tempLocationList, new Comparator<PushMessage>() {
                    @Override
                    public int compare(PushMessage message1, PushMessage message2) {
                        return (int) (message1.expiry - message2.expiry);
                    }
                });
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                if(pushMessagesList!=null) {
                    pushMessagesList.clear();
                    pushMessagesList.addAll(tempLocationList);
                    notifyDataSetChanged();

                }
                callback.onSortFinished();
            }
        }


        private void sortedList(){
            try {
                new SortListTask().execute();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        @Override
        public int getItemCount() {
            return pushMessagesList == null ? 0 : pushMessagesList.size();
        }




        public  class PromoCardHolder extends RecyclerView.ViewHolder {
            public NetworkImageView iv;
            public TextView promoTitle;
            public TextView description;
            public ImageView share ;



            public PromoCardHolder(View itemView) {
                super(itemView);
                iv = (NetworkImageView) itemView.findViewById(R.id.niv);
                promoTitle = (TextView) itemView.findViewById(R.id.stxt);
                share = (ImageView) itemView.findViewById(R.id.share);
                description = (TextView) itemView.findViewById(R.id.ptxt);
            }
        };


    public static interface PromoAdapterCallback {
        public void onSortFinished();
    }

}
