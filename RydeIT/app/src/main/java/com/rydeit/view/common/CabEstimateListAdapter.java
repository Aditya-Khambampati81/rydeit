package com.rydeit.view.common;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.rydeit.R;
import com.rydeit.util.Constants;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;

public class CabEstimateListAdapter extends BaseAdapter {

    public static final int TYPE_HEADER = 0;
    public static final int TYPE_ITEM = 1;
    private static final String TAG= CabEstimateListAdapter.class.getSimpleName();

    private ArrayList<Estimate> mData = new ArrayList<Estimate>();

    private LayoutInflater mInflater;

    public CabEstimateListAdapter(Context context) {
        mInflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void clearRidesItems(Constants.CABCOMPANY cabcompany){
        for (Iterator<Estimate> iterator = mData.iterator(); iterator.hasNext();) {
            Estimate estimate = iterator.next();
            if(estimate!=null && estimate.cabcompany!=null) {
                if (estimate.cabcompany.toString().equals(cabcompany.toString())) {
                    iterator.remove();
                }
            }
        }
    }

    public void clearHeaderIfNoItems(Constants.CABCOMPANY cabcompany){
        int itemCount=0;
        for (Iterator<Estimate> iterator = mData.iterator(); iterator.hasNext();) {
            Estimate estimate = iterator.next();
            if(estimate!=null && estimate.cabcompany!=null) {
                if (estimate.cabcompany.toString().equals(cabcompany.toString()) && estimate.itemType==TYPE_ITEM) {
                    itemCount++;
                }
            }
        }
        if(itemCount==0)
            clearRidesItems(cabcompany);
    }

    public void addItem(final Estimate estimate) {
        estimate.itemType=TYPE_ITEM;
        mData.add(estimate);
        Log.d(TAG,"mData=="+mData.size());
    }

    public void addSectionHeaderItem(final Estimate estimate) {
        estimate.itemType=TYPE_HEADER;
        mData.add(estimate);
    }

    @Override
    public int getItemViewType(int position) {
        if(mData!=null && mData.get(position)!=null && mData.get(position).itemType==TYPE_HEADER)
            return TYPE_HEADER;
        else
            return TYPE_ITEM;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Estimate getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        int rowType = getItemViewType(position);

        if (convertView == null) {
            holder = new ViewHolder();
            switch (rowType) {
                case TYPE_HEADER:
                    convertView = mInflater.inflate(R.layout.listview_header_item, null);
                    holder.rydename = (TextView) convertView.findViewById(R.id.text);
                    holder.cabCompanyImage=(ImageView)convertView.findViewById((R.id.cabcompanyimage));

                    break;
                case TYPE_ITEM:
                    convertView = mInflater.inflate(R.layout.listview_row_item, null);
                    holder.rydename = (TextView) convertView.findViewById(R.id.rydename);
                    holder.eta = (TextView) convertView.findViewById(R.id.eta);
                    holder.surgePrice = (TextView) convertView.findViewById(R.id.surge);
                    holder.rate = (TextView) convertView.findViewById(R.id.rate);
                    break;
            }
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        switch (rowType) {
            case TYPE_HEADER:
                Log.d(TAG,"rowType:"+rowType+" position=="+position);
                Estimate estimate=mData.get(position);
                holder.rydename.setText(estimate.display_name);
                holder.cabCompanyImage.setImageResource(estimate.cabImageResource);
                break;
            case TYPE_ITEM:
                Log.d(TAG, "rowType:" + rowType + " position==" + position);
                holder.rydename.setText(mData.get(position).display_name );
                holder.eta.setText(mData.get(position).eta+" min");
                if(mData.get(position).costPerDistance>0)
                    holder.rate.setText(mData.get(position).costPerDistance + "/km");//TODO NEEDS TO BE LOCALIZED BASED ON CURRENCY

                if(mData.get(position).surcharge > 0) {
                    holder.surgePrice.setText(" "+Float.toString(mData.get(position).surcharge)+"x");
                    holder.surgePrice.setVisibility(View.VISIBLE);
                }
                else
                    holder.surgePrice.setVisibility(View.INVISIBLE);
                break;
        }

        return convertView;
    }

    public static class ViewHolder {
        public TextView rydename;
        public ImageView cabCompanyImage;
        public TextView eta;
        public TextView surgePrice;
        public TextView rate;
    }



    public void clear()
    {
        mData.clear();
    }

    public static class Estimate implements Serializable{
        public String id;
        public String display_name;
        public int eta;
        public Constants.CABCOMPANY cabcompany;
        public float costPerDistance;
        public String address;
        public float surcharge;
        public int itemType;
        public int cabImageResource;
    }

}