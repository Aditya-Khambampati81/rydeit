package com.rydeit.view.common;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import android.widget.Toast;

import com.rydeit.R;
import com.rydeit.cab.service.ola.OlaAPIConstants;
import com.rydeit.util.Constants;
import com.rydeit.util.SharedPrefUtil;
import com.rydeit.view.CabAuthActivity;
import com.rydeit.view.MapsActivity;

/**
 * Created by Prakhyath on 10/17/15.
 */

public class OlaAuthFragment extends Fragment {

    private static final String TAG = OlaAuthFragment.class.getSimpleName();
    public static final String ARG_SECTION_NUMBER = "section_number";
    public static OlaAuthFragment mOlaAuthFragment =null;

    private static int iSectionNumber;
    private static String AccessToken;//FIXME SECURITY ISSUE-FIND ALTERNATIVE WAY

    private static String TokenType;
    private static long TokenValidityTimestamp;

    private View mRootView;

    public static OlaAuthFragment getInstance() {

        //iSectionNumber=sectionNumber;
        if(mOlaAuthFragment == null ) {
            mOlaAuthFragment = new OlaAuthFragment();
            mOlaAuthFragment.setRetainInstance(true);
            Bundle args = new Bundle();
            //args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            mOlaAuthFragment.setArguments(args);
        }

        return mOlaAuthFragment;
    }


    @Override
    public void onAttach(Activity activity) {

        super.onAttach(activity);

        // This will update the title in navigation bar.
        if(activity instanceof  MapsActivity) {
            ((MapsActivity)activity).onSectionAttached(
                    getArguments().getInt(ARG_SECTION_NUMBER));
        }
    }

    public OlaAuthFragment() {
        // Required empty public ´constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mRootView = inflater.inflate(R.layout.fragment_webview_auth, container, false);
        TextView tvLoginhelp=(TextView)mRootView.findViewById(R.id.logihelptext);
        tvLoginhelp.setText(getString(R.string.help_auth, Constants.CAB_INDIA.OLA.toString()));

        return mRootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        handleAuth();
    }

    protected void handleAuth() {

        if(SharedPrefUtil.getStringPreference(OlaAuthFragment.this.getActivity(), Constants.getAccessTokenKey(Constants.CAB_INDIA.OLA))!=null) {

            TokenValidityTimestamp = SharedPrefUtil.getLongPreference(OlaAuthFragment.this.getActivity(), Constants.getTokenValidityKey(Constants.CAB_INDIA.OLA), 0);

            if (System.currentTimeMillis() < TokenValidityTimestamp) {//TODO TEST LOGIC
                AccessToken = SharedPrefUtil.getStringPreference(OlaAuthFragment.this.getActivity(), Constants.getAccessTokenKey(Constants.CAB_INDIA.OLA));
                TokenType = SharedPrefUtil.getStringPreference(OlaAuthFragment.this.getActivity(), Constants.getTokenTypeKey(Constants.CAB_INDIA.OLA));
                openFragment();

            }
        }
        else{
            WebView webView = (WebView) this.getActivity().findViewById(R.id.web_view);
            webView.getSettings().setJavaScriptEnabled(true);
            webView.setWebViewClient(new OlaWebViewClient());
            webView.loadUrl(buildUrl());
        }
    }

    private String buildUrl() {
        Uri.Builder uriBuilder = Uri.parse(OlaAPIConstants.AUTHORIZE_URL).buildUpon();
        uriBuilder.appendQueryParameter(OlaAPIConstants.OAUTH_AUTH_PARAM.RESPONSE_TYPE, OlaAPIConstants.OAUTH_AUTH_PARAM.RESPONSE_TYPE_VALUE);
        uriBuilder.appendQueryParameter(OlaAPIConstants.OAUTH_AUTH_PARAM.CLIENT_ID, OlaAPIConstants.getOlaClientId(this.getActivity()));
        uriBuilder.appendQueryParameter(OlaAPIConstants.OAUTH_AUTH_PARAM.SCOPE, OlaAPIConstants.OAUTH_AUTH_PARAM.SCOPE_VALUE);
        uriBuilder.appendQueryParameter(OlaAPIConstants.OAUTH_AUTH_PARAM.REDIRECT_URI, OlaAPIConstants.getOlaRedirectUrl(this.getActivity()));
        uriBuilder.appendQueryParameter(OlaAPIConstants.OAUTH_AUTH_PARAM.STATE, OlaAPIConstants.OAUTH_AUTH_PARAM.STATE_VALUE);
        Log.d(TAG,"URL===="+uriBuilder.toString());
        return uriBuilder.build().toString().replace("%20", "+");
    }

    private class OlaWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            return checkRedirect(view, url);
        }

        @Override
        public void onPageFinished(WebView view, String url) {



            if (OlaAuthFragment.this.getActivity() != null) {

                if (OlaAuthFragment.this.getActivity() instanceof CabAuthActivity) {
                    if (((CabAuthActivity) OlaAuthFragment.this.getActivity()).mIsRunning)
                        ((CabAuthActivity) OlaAuthFragment.this.getActivity()).hideProgressBar();
                }
            }


            super.onPageFinished(view,url);
        }

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            if (checkRedirect(view, failingUrl)) {
                return;
            }
            if (OlaAuthFragment.this.getActivity() != null) {

                if (OlaAuthFragment.this.getActivity() instanceof CabAuthActivity) {
                    if (((CabAuthActivity) OlaAuthFragment.this.getActivity()).mIsRunning)
                        ((CabAuthActivity) OlaAuthFragment.this.getActivity()).hideProgressBar();
                }
            }

            Toast.makeText(OlaAuthFragment.this.getActivity(), "Oh no! " + description, Toast.LENGTH_SHORT).show();
        }

        private boolean checkRedirect(WebView view, String url) {
            if (url.startsWith(OlaAPIConstants.getOlaRedirectUrl(OlaAuthFragment.this.getActivity()))) {
                //url="http://bsoftlabs.in/?access_token=a532015f982a4696b4bcfb885cd09759&state=state123&scope=profile%20booking&token_type=bearer&expires_in=15551999";
                url=url.replace("#","?");//FIXME TEMP FIX TO OLA API REDIRECT
                Uri uri = Uri.parse(url);
                AccessToken=uri.getQueryParameter(OlaAPIConstants.OAUTH_REDIRECT_PARAM.ACCESS_TOKEN);
                TokenType=uri.getQueryParameter(OlaAPIConstants.OAUTH_REDIRECT_PARAM.TOKEN_TYPE);
                String TokenValidity=uri.getQueryParameter(OlaAPIConstants.OAUTH_REDIRECT_PARAM.EXPIRES_IN);
                Log.d(TAG,"uri===="+uri);
                Log.d(TAG,"AccessToken===="+AccessToken);
                Log.d(TAG,"TokenType===="+TokenType);

                SharedPrefUtil.setStringPreference(OlaAuthFragment.this.getActivity(), Constants.getAccessTokenKey(Constants.CAB_INDIA.OLA),AccessToken);
                SharedPrefUtil.setStringPreference(OlaAuthFragment.this.getActivity(),Constants.getTokenTypeKey(Constants.CAB_INDIA.OLA),TokenType);

                long validitytimestamp=System.currentTimeMillis()+Long.valueOf(TokenValidity)*1000;
                SharedPrefUtil.setLongPreference(OlaAuthFragment.this.getActivity(), Constants.getTokenValidityKey(Constants.CAB_INDIA.OLA), validitytimestamp);

                openFragment();

                return true;
            }else
                view.loadUrl(url);
            return false;
        }
    }

    private void openFragment()
    {
        Log.i(TAG, "Ola oAuth is success for Finder activity @ Position" + iSectionNumber);
        FragmentActivity act=OlaAuthFragment.this.getActivity();

        Intent i=new Intent();
        i.putExtra("access_token", AccessToken);
        i.putExtra("token_type", TokenType);
        this.getActivity().setResult(101, i);
        this.getActivity().finish();

        /*switch (iSectionNumber) {
            case 2:
                //FIXME choose list view or maps view based on state saved last time.?
                //MY PROFILE
                act.getSupportFragmentManager().beginTransaction()
                        .replace(R.id.finder_container, MyProfileFragment.getInstance(iSectionNumber))
                        .commit();

                break;
            case 3:
                //History
                act.getSupportFragmentManager().beginTransaction()
                        .replace(R.id.finder_container, HistoryFragment.getInstance(iSectionNumber))
                        .commit();

                break;
        }*/
    }

    public static String getTokenType() {
        return TokenType;
    }

    public static String getAccessToken() {
        return AccessToken;
    }

}
