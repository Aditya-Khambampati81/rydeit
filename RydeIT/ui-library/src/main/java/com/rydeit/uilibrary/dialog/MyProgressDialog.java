package com.rydeit.uilibrary.dialog;


import android.content.Context;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;


import com.avast.android.dialogs.fragment.ProgressDialogFragment;

public class MyProgressDialog {

    public static final String FRAG_TAG = "MyProgressDialog";

    private MyProgressDialog() {
    }

    DialogFragment f;

    public static void showProgressDialog(Context ctx,
                                          FragmentManager fragmentManager, String title, String message,
                                          boolean cancelable) {
        try {
            if (ctx != null && fragmentManager != null) {
                dismissProgressDialog(fragmentManager);
                ProgressDialogFragment
                        .createBuilder(ctx.getApplicationContext(), fragmentManager)
                        .setMessage(message).setTitle(title)
                        .setCancelable(cancelable).setTag(FRAG_TAG)
                        .setCancelableOnTouchOutside(false).show();
            }
        } catch (Exception e) {
            //Something went wrong. It will come here in very rare case.
        }
    }

    public static boolean dismissProgressDialog(FragmentManager fragmentManager) {
        if (fragmentManager != null) {
            DialogFragment df = (DialogFragment) fragmentManager
                    .findFragmentByTag(FRAG_TAG);
            if (df != null){
                df.dismissAllowingStateLoss();
                return true;
            }
        }
        return false;
    }

}
