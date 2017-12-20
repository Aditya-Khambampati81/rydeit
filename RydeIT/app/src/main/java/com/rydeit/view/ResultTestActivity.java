package com.rydeit.view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.rydeit.R;
import com.rydeit.api.uber.UberAPIClient;
import com.rydeit.api.uber.UberCallback;
import com.rydeit.model.uber.PriceEstimateList;
import com.rydeit.model.uber.ProductList;
import com.rydeit.model.uber.Profile;
import com.rydeit.model.uber.TimeEstimateList;
import com.rydeit.model.uber.UserActivity;
import com.rydeit.util.Constants;

import java.util.ArrayList;
import java.util.List;

import retrofit.client.Response;


public class ResultTestActivity extends ActionBarActivity {

    private static final String TAG=ResultTestActivity.class.getSimpleName();

    public static void start(Context context, int position, String accessToken, String tokenType) {
        Intent intent = new Intent(context, ResultTestActivity.class);
        intent.putExtra("position", position);
        intent.putExtra("access_token", accessToken);
        Log.d(TAG,"accessToken========="+accessToken+" \n token_type="+tokenType);
        intent.putExtra("token_type", tokenType);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        int position = getIntent().getIntExtra("position", 0);
        switch (position) {
            case 1:
                UberAPIClient.getUberV1APIClient().getProducts(getAccessToken(),
                        Constants.START_LATITUDE,
                        Constants.START_LONGITUDE,
                        new UberCallback<ProductList>() {
                            @Override
                            public void success(ProductList productList, Response response) {
                                setupListAdapter("products", productList.toString());
                            }
                        });
                break;
            case 2:
                UberAPIClient.getUberV1APIClient().getTimeEstimates(getAccessToken(),
                        Constants.START_LATITUDE,
                        Constants.START_LONGITUDE,
                        new UberCallback<TimeEstimateList>() {
                            @Override
                            public void success(TimeEstimateList timeEstimateList, Response response) {
                                setupListAdapter("time", timeEstimateList.toString());
                            }
                        });
                break;
            case 3:
                UberAPIClient.getUberV1APIClient().getPriceEstimates(getAccessToken(),
                        Constants.START_LATITUDE,
                        Constants.START_LONGITUDE,
                        Constants.END_LATITUDE,
                        Constants.END_LONGITUDE,
                        new UberCallback<PriceEstimateList>() {
                            @Override
                            public void success(PriceEstimateList priceEstimateList, Response response) {
                                setupListAdapter("price", priceEstimateList.toString());
                            }
                        });
                break;
            case 4:
                UberAPIClient.getUberV1_1APIClient().getUserActivity(getAccessToken(),
                        0,
                        5,
                        new UberCallback<UserActivity>() {
                            @Override
                            public void success(UserActivity userActivity, Response response) {
                                setupListAdapter("history (v1.1)", userActivity.toString());
                            }
                        });
                break;
            case 5:
                UberAPIClient.getUberV1_2APIClient().getUserActivity(getAccessToken(),
                        0,
                        10,
                        new UberCallback<UserActivity>() {
                            @Override
                            public void success(UserActivity userActivity, Response response) {
                                setupListAdapter("history (v1.2)", userActivity.toString());
                            }
                        });
                break;
            case 6:
                UberAPIClient.getUberV1APIClient().getProfile(getAccessToken(),
                        new UberCallback<Profile>() {
                            @Override
                            public void success(Profile profile, Response response) {
                                setupListAdapter("me", profile.toString());
                            }
                        });
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupListAdapter(String endpoint, String response) {
        List<String> options = new ArrayList<String>();
        options.add(getString(R.string.endpoint_list_result_text, endpoint));
        options.add(response);

        ListView listView = (ListView) findViewById(R.id.list_view);
        listView.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1, options));
    }


    private String getAccessToken() {
        return getIntent().getStringExtra("token_type") + " " + getIntent().getStringExtra("access_token");
    }
}
