package com.rydeit.uilibrary;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;


public abstract class BaseNavigationActivity extends TearDownBaseActivityActionBar
        implements NavigationDrawerFragment.NavigationDrawerCallbacks {



    public static final int LAUNCH_LOGIN = 101;
    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    public NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in
     */
    private CharSequence mTitle;

    //private TextView tv ;
    //private TextView tv2 ;
    //private ImageView iv;
    public TextView toolbar_tile;
    private ImageView toolbar_image;
    LinearLayout ll_toolbar_title;

    public abstract List<DrawerObject> getDrawerContents();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getSupportActionBar() != null) {
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().hide();
        }
        setContentView(R.layout.my_finder_activity);
       //  Set a Toolbar to replace the ActionBar.
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        if(Build.VERSION.SDK_INT >= 16) {
            toolbar.setBackground(new ColorDrawable(getResources().getColor(R.color.toolbarTransparent)));
            toolbar.getBackground().setAlpha(0);

        }
        setSupportActionBar(toolbar);
//        tv = (TextView) findViewById(R.id.welcome);
//        tv2 = (TextView) findViewById(R.id.login);
//        iv = (ImageView) findViewById(R.id.imageButton1);
//        iv.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                onDrawerItemSelected(LAUNCH_LOGIN);
//            }
//        });
//        tv.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                onDrawerItemSelected(LAUNCH_LOGIN);
//            }
//        });


//        toolbar_tile = (TextView) findViewById(R.id.actionbar_title);
//        toolbar_image=(ImageView)findViewById(R.id.toolbar_title_image);
//        ll_toolbar_title=(LinearLayout)findViewById(R.id.ll_toolbar_title);

        mNavigationDrawerFragment = (NavigationDrawerFragment)getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();
        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.drawerll,
                (DrawerLayout) findViewById(R.id.drawer_layout));

    }


    public void updateTextStatus(String header , String message, int drawableRight, boolean loginStatus ) {

//        if (header == null) {
//            tv.setVisibility(View.GONE);
//        } else {
//            tv.setText(header);
//            tv.setVisibility(View.VISIBLE);
//            /*if (loginStatus) {
//                tv.setTextColor(getResources().getColor(R.color.white));
//            } else {
//                tv.setTextColor(getResources().getColor(android.R.color.black));
//            }*/
//        }
//
//        if (message == null) {
//            tv2.setVisibility(View.GONE);
//        } else {
//            tv2.setVisibility(View.VISIBLE);
//            tv2.setText(message);
//        }
//
//        iv.setBackgroundResource(drawableRight);
//        if (mNavigationDrawerFragment != null)
//        {
//            mNavigationDrawerFragment.invalidateDrawerLayout();
//        }
    }


    public abstract void onDrawerItemSelected(int position);


    @Override
    public void onNavigationDrawerItemSelected(int position) {
        this.onDrawerItemSelected(position);

    }


    public void onSectionAttached(int number) {
        List<DrawerObject> drawerContents= getDrawerContents();
        if (drawerContents != null) {
            mTitle = getString(drawerContents.get(number - 1).stringFragmentTitle);
         //   if (getSupportActionBar() != null) {
                setLocalTitle(mTitle);
          //  }
        }
    }

    /** This is hack for setting title for action bar colour white
     *
     * @param title
     */
    public void  setLocalTitle(CharSequence title )
    {
       // getSupportActionBar().setTitle(Html.fromHtml("<font color=\"white\">" + title + "</font>"));
        setToolBarTitle(title);

    }

    public void setToolBarTitle(CharSequence cs)
    {
        if(toolbar_tile != null)
        {
            toolbar_tile.setText(cs);
        }
    }

    public void setToolBarImage(int drawable)
    {
        if(toolbar_image != null)
        {
            toolbar_image.setVisibility(View.VISIBLE);
            toolbar_image.setImageResource(drawable);
            ll_toolbar_title.setGravity(Gravity.CENTER);
        }
    }

    public void hideToolBarImage()
    {
        if(toolbar_image != null)
        {
            toolbar_image.setVisibility(View.GONE);
        }
    }

    public void hideToolBarTitle()
    {
        if(toolbar_tile != null)
        {
            toolbar_tile.setVisibility(View.GONE);
        }
    }

    public void showToolBarTitle()
    {
        if(toolbar_tile != null)
        {
            toolbar_tile.setVisibility(View.VISIBLE);
        }
    }


    /**
     * This is static class for drawer object
     *
     */
    public static class DrawerObject
    {
        public int drawableId;
        public int stringResourceDescription;
        public int subtitle;
        public int stringFragmentTitle;
        public Intent actionIntent;

        public DrawerObject(int dId, int description, int secondaryDescription, int title, Intent aIntent)
        {
            drawableId = dId;
            stringResourceDescription= description;
            subtitle = secondaryDescription;
            stringFragmentTitle = title;
            actionIntent = aIntent;
        }
    }
}
