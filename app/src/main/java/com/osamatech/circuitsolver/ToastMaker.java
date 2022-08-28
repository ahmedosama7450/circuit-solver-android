package com.osamatech.circuitsolver;

import android.content.Context;
import android.widget.Toast;

public class ToastMaker {

    private static Toast lastToast;

    public static void showToast(Context context, String text, int duration) {
        if (lastToast != null) lastToast.cancel();
        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
        lastToast = toast;
    }

    public static void shortToast(Context context, String text) {
        showToast(context, text, Toast.LENGTH_SHORT);
    }

}
