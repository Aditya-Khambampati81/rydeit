package com.rydeit.view.common;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
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
import com.rydeit.api.uber.UberAuthTokenClient;
import com.rydeit.api.uber.UberCallback;
import com.rydeit.model.uber.User;
import com.rydeit.uilibrary.BaseFragment;
import com.rydeit.util.Constants;
import com.rydeit.util.SharedPrefUtil;
import com.rydeit.view.CabAuthActivity;
import com.rydeit.view.ConfirmBookingActivity;
import com.rydeit.view.HistoryFragment;
import com.rydeit.view.MyProfileFragment;

import retrofit.RetrofitError;
import retrofit.client.Response;


public class UberAuthFragment extends BaseFragment {

    private static final String TAG = UberAuthFragment.class.getSimpleName();
    public static final String ARG_SECTION_NUMBER = "section_number";
    private static int iSectionNumber;
    private static String AccessToken;//FIXME SECURITY ISSUE-FIND ALTERNATIVE WAY

    private static String TokenType;
    private static String TokenValidity;
    private static String RefreshToken;

    private static long TokenValidityTimestamp;

    private View mRootView;

    public static UberAuthFragment getInstance(int sectionNumber) {

        iSectionNumber=sectionNumber;
        return new UberAuthFragment();
    }


    @Override
    public void onAttach(Activity activity) {

        super.onAttach(activity);
//
//        // This will update the title in navigation bar.
//        if(activity instanceof  MapsActivity) {
//            ((MapsActivity)activity).onSectionAttached(
//                    getArguments().getInt(ARG_SECTION_NUMBER));
//        }
    }

    public UberAuthFragment() {
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
        tvLoginhelp.setText(getString(R.string.help_auth, Constants.CAB_GLOBAL.UBER.toString()));

        return mRootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        handleAuth();
    }

    @Override
    public void onDialogTimedOut(int reqCode) {

    }

    @Override
    public void processCustomMessage(Message messageWhat) {

    }

    protected void handleAuth() {

        if(SharedPrefUtil.getStringPreference(UberAuthFragment.this.getActivity(), Constants.getAccessTokenKey(Constants.CAB_GLOBAL.UBER))!=null) {
            TokenValidityTimestamp = SharedPrefUtil.getLongPreference(UberAuthFragment.this.getActivity(), Constants.getTokenValidityKey(Constants.CAB_GLOBAL.UBER), 0);
           //Use this for testing , booking second uber cab would refresh token if we uncomment line below.
           // TokenValidityTimestamp = 1;
            if (System.currentTimeMillis() < TokenValidityTimestamp) {//TODO TEST LOGIC
                AccessToken = SharedPrefUtil.getStringPreference(UberAuthFragment.this.getActivity(), Constants.getAccessTokenKey(Constants.CAB_GLOBAL.UBER));
                TokenType = SharedPrefUtil.getStringPreference(UberAuthFragment.this.getActivity(), Constants.getTokenTypeKey(Constants.CAB_GLOBAL.UBER));
                openFragment();

            }
            else {
                // refresh token
//                int reqCode, Activity activity, BaseFragment fragment,
//                        FragmentManager manager, String title,
//                        String message, boolean cancelable
                showProgressDialog(102,this.getActivity(),this,this.getFragmentManager(), "Access Token","Refresh access token" ,false);
                UberAuthTokenClient.getUberAuthTokenClient().refreshAuthToken(
                        Constants.getUberClientSecret(UberAuthFragment.this.getActivity()),
                        Constants.getUberClientId(UberAuthFragment.this.getActivity()),
                        "refresh_token",
                        Constants.getUberRedirectUrl(UberAuthFragment.this.getActivity()),

                        SharedPrefUtil.getStringPreference(this.getActivity(), Constants.getRefreshTokenKey(Constants.CAB_GLOBAL.UBER)),
                        new UberCallback<User>() {
                            @Override
                            public void success(User user, Response response) {

                                dismissProgressDialog(getFragmentManager());


                                AccessToken = user.getAccessToken();
                                TokenType = user.getTokenType();
                                TokenValidity = user.getExpiresIn();
                                RefreshToken = user.getRefreshToken();

                                SharedPrefUtil.setStringPreference(UberAuthFragment.this.getActivity(), Constants.getAccessTokenKey(Constants.CAB_GLOBAL.UBER), AccessToken);
                                SharedPrefUtil.setStringPreference(UberAuthFragment.this.getActivity(), Constants.getTokenTypeKey(Constants.CAB_GLOBAL.UBER), TokenType);
                                SharedPrefUtil.setStringPreference(UberAuthFragment.this.getActivity(), Constants.getRefreshTokenKey(Constants.CAB_GLOBAL.UBER), RefreshToken);

                                long ctime = System.currentTimeMillis();
                                long validitytimestamp = System.currentTimeMillis() + Long.valueOf(TokenValidity) * 1000;
                                SharedPrefUtil.setLongPreference(UberAuthFragment.this.getActivity(), Constants.getTokenValidityKey(Constants.CAB_GLOBAL.UBER), validitytimestamp);

                                openFragment();
                            }

                            @Override
                            public void failure(RetrofitError error) {

                                dismissProgressDialog(getFragmentManager());
                                if(UberAuthFragment.this.getActivity() != null) {
                                    Toast.makeText(UberAuthFragment.this.getActivity(), "Error while refreshing token", Toast.LENGTH_SHORT).show();
                                    error.printStackTrace();
                                    UberAuthFragment.this.getActivity().finish();
                                }

                            }

                        });


            }
        }
        else {
            WebView webView = (WebView) this.getActivity().findViewById(R.id.web_view);
            webView.getSettings().setJavaScriptEnabled(true);
            webView.setWebViewClient(new UberWebViewClient());

            webView.loadUrl(buildUrl());
        }
    }

    private String buildUrl() {
        Uri.Builder uriBuilder = Uri.parse(Constants.AUTHORIZE_URL).buildUpon();
        uriBuilder.appendQueryParameter("response_type", "code");
        uriBuilder.appendQueryParameter("client_id",Constants.getUberClientId(this.getActivity()));
        uriBuilder.appendQueryParameter("scope", Constants.SCOPES);
        uriBuilder.appendQueryParameter("redirect_uri", Constants.getUberRedirectUrl(this.getActivity()));
        return uriBuilder.build().toString().replace("%20", "+");
    }

    private class UberWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            return checkRedirect(view, url);
        }

        @Override
        public void onPageFinished(WebView view, String url) {

            if (UberAuthFragment.this.getActivity() != null) {

                if (UberAuthFragment.this.getActivity() instanceof CabAuthActivity) {
                    if (((CabAuthActivity) UberAuthFragment.this.getActivity()).mIsRunning)
                        ((CabAuthActivity) UberAuthFragment.this.getActivity()).hideProgressBar();
                }
            }


            super.onPageFinished(view, url);
        }

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            if (checkRedirect(view, failingUrl)) {
                return;
            }

            if (UberAuthFragment.this.getActivity() != null) {

                if (UberAuthFragment.this.getActivity() instanceof CabAuthActivity) {
                    if (((CabAuthActivity) UberAuthFragment.this.getActivity()).mIsRunning)
                        ((CabAuthActivity) UberAuthFragment.this.getActivity()).hideProgressBar();
                }
            }

            Toast.makeText(UberAuthFragment.this.getActivity(), "Oh no! " + description, Toast.LENGTH_SHORT).show();
        }

        private boolean checkRedirect(WebView view, String url) {
            if (url.startsWith(Constants.getUberRedirectUrl(UberAuthFragment.this.getActivity()))) {
                Uri uri = Uri.parse(url);
                UberAuthTokenClient.getUberAuthTokenClient().getAuthToken(
                        Constants.getUberClientSecret(UberAuthFragment.this.getActivity()),
                        Constants.getUberClientId(UberAuthFragment.this.getActivity()),
                        "authorization_code",
                        uri.getQueryParameter("code"),
                        Constants.getUberRedirectUrl(UberAuthFragment.this.getActivity()),
                        new UberCallback<User>() {
                            @Override
                            public void success(User user, Response response) {
                                AccessToken=user.getAccessToken();
                                TokenType=user.getTokenType();
                                TokenValidity=user.getExpiresIn();
                                RefreshToken=user.getRefreshToken();

                                SharedPrefUtil.setStringPreference(UberAuthFragment.this.getActivity(), Constants.getAccessTokenKey(Constants.CAB_GLOBAL.UBER), AccessToken);
                                SharedPrefUtil.setStringPreference(UberAuthFragment.this.getActivity(),Constants.getTokenTypeKey(Constants.CAB_GLOBAL.UBER),TokenType);
                                SharedPrefUtil.setStringPreference(UberAuthFragment.this.getActivity(),Constants.getRefreshTokenKey(Constants.CAB_GLOBAL.UBER),RefreshToken);

                                long ctime=System.currentTimeMillis();
                                long validitytimestamp=System.currentTimeMillis()+Long.valueOf(TokenValidity)*1000;
                                SharedPrefUtil.setLongPreference(UberAuthFragment.this.getActivity(), Constants.getTokenValidityKey(Constants.CAB_GLOBAL.UBER), validitytimestamp);

                                openFragment();
                            }

                        });
                return true;

            }else
                view.loadUrl(url);
            return false;
        }
    }

    private void openFragment()
    {
        Log.i(TAG, "Uber oAuth is sucess for Finder activity @ Position" + iSectionNumber);
        FragmentActivity act=UberAuthFragment.this.getActivity();
        switch (iSectionNumber) {
            case 1:
                //Book Cab
                Intent intent=new Intent();
                intent.putExtra("access_token", AccessToken);
                intent.putExtra("token_type", TokenType);
                this.getActivity().setResult(ConfirmBookingActivity.REQUEST_CODE_UBER, intent);
                this.getActivity().finish();
                break;
            case 2:
                //FIXME choose list view or maps view based on state saved last time.?
                //MY PROFILE
                act.getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, MyProfileFragment.getInstance(iSectionNumber))
                        .commit();

                break;
            case 3:
                //History- TODO THIS LOGIC IS NO MORE REQUIRED
                act.getSupportFragmentManager().beginTransaction()
                        .replace(R.id.finder_container, HistoryFragment.getInstance(iSectionNumber))
                        .commit();

                break;
        }
    }

    public static String getTokenType() {
        return TokenType;
    }

    public static String getAccessToken() {
        return AccessToken;
    }

}
