package com.rydeit.view;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.rydeit.R;
import com.rydeit.util.Constants;


public class AboutFragment extends Fragment {

    public static final String ARG_SECTION_NUMBER = "section_number";
    public static AboutFragment mAboutFragment  =null;

    int mNoOfClicks=0;
    long mLastClickTime;



    public static AboutFragment getInstance(int sectionNumber) {

        if(mAboutFragment == null ) {
            mAboutFragment = new AboutFragment();
            //mAboutFragment.setRetainInstance(true);
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            mAboutFragment.setArguments(args);
        }

        return mAboutFragment;
    }


    @Override
    public void onAttach(Activity activity) {

        super.onAttach(activity);

    }

    public AboutFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View mRootView =inflater.inflate(R.layout.fragment_about, container, false);
        TextView tv = (TextView)mRootView.findViewById(R.id.rights_reserved_txt);
        tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(Constants.URL.DEVELOPER_SITE)));
            }
        });
        TextView osl = (TextView) mRootView.findViewById(R.id.oslv);
        osl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(getActivity(), WebviewActivity.class);
                intent.putExtra("REQUEST_URL_TYPE", WebviewActivityFragment.REQUEST_URL_TYPE_LICENSES);
                startActivity(intent);
            }
        });


        ImageView imageViewabout=(ImageView)mRootView.findViewById(R.id.imageView);
        imageViewabout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(System.currentTimeMillis()-mLastClickTime<200)
                    mNoOfClicks++;
                else
                    mNoOfClicks=1;

                mLastClickTime=System.currentTimeMillis();

                if(mNoOfClicks>=4) {
                    Constants.SIMULATE_BOOKING = !(Constants.SIMULATE_BOOKING);
                    String Mode=(Constants.SIMULATE_BOOKING)?"ON":"OFF";
                    Toast.makeText(AboutFragment.this.getActivity(), " SIMULATION MODE : "+Mode, Toast.LENGTH_SHORT).show();
                    mNoOfClicks=0;

                }
            }
        });

        return mRootView;
    }
}
