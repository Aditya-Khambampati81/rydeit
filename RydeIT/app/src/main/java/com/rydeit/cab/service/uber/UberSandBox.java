package com.rydeit.cab.service.uber;

import android.content.Context;

import com.rydeit.api.uber.UberAPIClient;
import com.rydeit.model.uber.sandbox.SandboxProductBody;
import com.rydeit.util.Constants;
import com.rydeit.util.Log;

/**
 * Created by Prakhyath on 12/25/15.
 */
public class UberSandBox {

    private static final String TAG = UberSandBox.class.getSimpleName();

    public static void putUberProductSandBox(final Context context, final String productId, final boolean driversAvailable, final float surgeMutiplier){

        if (Constants.SIMULATE_BOOKING && UberManager.getInstance(context).isLoggedIn()) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    SandboxProductBody sandboxProductBody=new SandboxProductBody(surgeMutiplier, driversAvailable);
                    UberAPIClient.getUberV1SandBoxAPIClient().putProducts(UberManager.getInstance(context).getUberAccessToken(), productId, sandboxProductBody);
                }
            }).start();
        }else
            Log.e(TAG, "Product Simulation is FAILED as user is not logged in");

    }
}
