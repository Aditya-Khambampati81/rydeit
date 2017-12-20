package com.rydeit.view;


import android.content.Intent;
import android.os.Bundle;
import android.os.Message;


import com.rydeit.R;
import com.rydeit.uilibrary.TearDownBaseActivityActionBar;

import java.util.Arrays;

/**
 * Login activity integrated with Facebook login
 *
 */
public class LoginActivity  extends TearDownBaseActivityActionBar {
//    private  CallbackManager callbackManager = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        if(isLoggedIn())
//        {
//            LoginActivity.this.startActivity(new Intent(getBaseContext(), MapsActivity.class));
//            finish();
//            return;
//        }
//
        setContentView(R.layout.activity_login);
//        callbackManager = CallbackManager.Factory.create();
//        final LoginButton loginButton = (LoginButton) findViewById(R.id.login_button);
//        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
//            @Override
//            public void onSuccess(LoginResult loginResult) {
//                //Toast login success
//                Toast.makeText(LoginActivity.this, "Login Success for id:" + loginResult.getAccessToken().getUserId(), Toast.LENGTH_LONG).show();
//                //FIXME:Save access token here to get user information later
//                // start all rydes list activity
//                LoginActivity.this.startActivity(new Intent(getBaseContext(), MapsActivity.class));
//                LoginActivity.this.finish();
//            }
//
//            @Override
//            public void onCancel() {
//                // toast login cancel
//                Toast.makeText(LoginActivity.this, "Login cancelled", Toast.LENGTH_SHORT).show();
//            }
//
//            @Override
//            public void onError(FacebookException e) {
//                Toast.makeText(LoginActivity.this, "Error logging in", Toast.LENGTH_SHORT).show();
//            }
//        });
//          loginButton.setOnClickListener(new View.OnClickListener() {
//              @Override
//              public void onClick(View v) {
//                  LoginManager.getInstance().logInWithReadPermissions(LoginActivity.this, Arrays.asList("public_profile", "user_friends"));
//              }
//          });


    }

//    public boolean isLoggedIn() {
//        if(AccessToken.getCurrentAccessToken()!=null)
//              return true;
//        return false;
//    }

   @Override
    public void onDialogTimedOut(int reqCode) {

    }

    @Override
    public void processCustomMessage(Message msg) {

    }
    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
//        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

}

