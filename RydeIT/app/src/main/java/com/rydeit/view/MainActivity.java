package com.rydeit.view;

import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Window;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.rydeit.R;
import com.rydeit.api.uber.UberAuthTokenClient;
import com.rydeit.api.uber.UberCallback;
import com.rydeit.model.uber.User;
import com.rydeit.util.Constants;

import retrofit.client.Response;


/**
 * Created by Aditya Khambampati
 */
public class MainActivity extends ActionBarActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().requestFeature(Window.FEATURE_PROGRESS);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        WebView webView = (WebView) findViewById(R.id.web_view);
        webView.getSettings().setJavaScriptEnabled(true);

        webView.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView view, int progress) {
                MainActivity.this.setProgress(progress * 1000);
            }
        });

        webView.setWebViewClient(new UberWebViewClient());

        webView.loadUrl(buildUrl());
    }

    private String buildUrl() {
        Uri.Builder uriBuilder = Uri.parse(Constants.AUTHORIZE_URL).buildUpon();
        uriBuilder.appendQueryParameter("response_type", "code");
        uriBuilder.appendQueryParameter("client_id", Constants.getUberClientId(this));
        uriBuilder.appendQueryParameter("scope", Constants.SCOPES);
        uriBuilder.appendQueryParameter("redirect_uri", Constants.getUberRedirectUrl(this));
        return uriBuilder.build().toString().replace("%20", "+");
    }

    private class UberWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            return checkRedirect(url);
        }

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            if (checkRedirect(failingUrl)) {
                return;
            }
            Toast.makeText(MainActivity.this, "Oh no! " + description, Toast.LENGTH_SHORT).show();
        }

        private boolean checkRedirect(String url) {
            if (url.startsWith(Constants.getUberRedirectUrl(MainActivity.this))) {
                Uri uri = Uri.parse(url);
                UberAuthTokenClient.getUberAuthTokenClient().getAuthToken(
                        Constants.getUberClientSecret(MainActivity.this),
                        Constants.getUberClientId(MainActivity.this),
                        "authorization_code",
                        uri.getQueryParameter("code"),
                        Constants.getUberRedirectUrl(MainActivity.this),
                        new UberCallback<User>() {
                            @Override
                            public void success(User user, Response response) {
                                DemoActivity.start(MainActivity.this, user.getAccessToken(), user.getTokenType());
                                finish();
                            }
                        });
                return true;
            }
            return false;
        }
    }
}
