package com.rydeit.view;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.rydeit.R;
import com.rydeit.cab.service.uber.UberAPIConstants;
import com.rydeit.util.Constants;
import com.rydeit.util.Log;

/**
 * Created by Aditya Khambampati
 */
public class WebviewActivityFragment extends Fragment {

    private static final String TAG = WebviewActivityFragment.class.getSimpleName();

    public static final String URL= "url";

    public static final String INTENT_STRING_REQUEST_URL_TYPE="REQUEST_URL_TYPE";
    public static final String INTENT_STRING_REQUEST_URL="REQUEST_URL";

    public static final int REQUEST_URL_TYPE_LICENSES=101;
    public static final int REQUEST_URL_TYPE_SURGE_CONFIRMATION=104;

    public WebviewActivityFragment() {
    }
    private WebView mWebView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_webview, container, false);
        mWebView = (WebView) view.findViewById(R.id.mywebview);
        /*mWebView.setInitialScale(1);
        mWebView.getSettings().setLoadWithOverviewMode(true);
        mWebView.getSettings().setUseWideViewPort(true);
        mWebView.setWebChromeClient(new WebChromeClient());
        mWebView.setBackgroundColor(getResources().getColor(R.color.white));

        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setPluginState(WebSettings.PluginState.ON_DEMAND);
        webSettings.setPluginState(WebSettings.PluginState.ON);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setUseWideViewPort(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            webSettings.setDisplayZoomControls(false);
        }*/
        String mUrl = Constants.URL.LICENSES;

        if(getActivity().getIntent().hasExtra(INTENT_STRING_REQUEST_URL_TYPE)){
            int type=getActivity().getIntent().getIntExtra(INTENT_STRING_REQUEST_URL_TYPE, 0);
            switch (type){
                case REQUEST_URL_TYPE_LICENSES:
                    mUrl= Constants.URL.LICENSES;
                    getActivity().setTitle(R.string.software_licences);
                    break;

                case REQUEST_URL_TYPE_SURGE_CONFIRMATION:
                    if(getActivity().getIntent().hasExtra(INTENT_STRING_REQUEST_URL))
                        mUrl=getActivity().getIntent().getStringExtra(INTENT_STRING_REQUEST_URL);
                        //mUrl=buildSurgeUrl(mUrl);
                    Log.d(TAG,"mUrl===="+mUrl);
                    getActivity().setTitle(R.string.surge_confirmation);

                    mWebView.getSettings().setJavaScriptEnabled(true);
                    mWebView.setWebViewClient(new CustomWebViewClient());

                    break;
            }
        }

        mWebView.loadUrl(mUrl);

    return view;
}

    private String buildSurgeUrl(String urlDomain) {
        Uri.Builder uriBuilder = Uri.parse(urlDomain).buildUpon();
        return uriBuilder.build().toString().replace("%20", "+");
    }

    private class CustomWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            return checkRedirect(view, url);
        }

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            if (checkRedirect(view, failingUrl)) {
                return;
            }
            Toast.makeText(WebviewActivityFragment.this.getActivity(), "Oh no! " + description, Toast.LENGTH_SHORT).show();
        }

        private boolean checkRedirect(WebView view, String url) {
            int type=0;
            if(getActivity()!=null && getActivity().getIntent()!=null)
                type= getActivity().getIntent().getIntExtra(INTENT_STRING_REQUEST_URL_TYPE, 0);
            if(type==REQUEST_URL_TYPE_SURGE_CONFIRMATION){
                if(url.contains(UberAPIConstants.getUberRedirectUrl(getActivity()))) {
                    Uri uri = Uri.parse(url);
                    String redirectResultValue=uri.getQueryParameter(UberAPIConstants.SURGE_REDIRECT_PARAM.RIDE_SURGE_CONFIRM_ID);
                    setRedirectResult(type, redirectResultValue);
                    return true;
                }
                else if(url.contains(Constants.BASE_UBER_SANDBOX_URL_V1))
                    view.loadUrl(url);
                else
                    view.loadUrl(url);

                return true;
            }
            return false;
        }
    }

    private void setRedirectResult(int type, String result)
    {
        if(type==REQUEST_URL_TYPE_SURGE_CONFIRMATION) {
            FragmentActivity act = WebviewActivityFragment.this.getActivity();
            Intent intent = new Intent();
            intent.putExtra(UberAPIConstants.SURGE_REDIRECT_PARAM.RIDE_SURGE_CONFIRM_ID, result);
            this.getActivity().setResult(ConfirmBookingActivity.REQUEST_URL_SURGE_CONFIRMATION, intent);
            this.getActivity().finish();
        }
    }
}
