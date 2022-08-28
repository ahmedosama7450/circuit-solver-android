package com.osamatech.circuitsolver;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;

public class FakeCircuits {

    public static final int[] IMAGES_IDS = new int[]{
            R.drawable.img_1,
            R.drawable.img_2,
            R.drawable.img_3,
            R.drawable.img_4,
            R.drawable.img_5,
            R.drawable.img_6,
            R.drawable.img_7,
            R.drawable.img_8,
    };

    public static final String[] IMAGES_NAMES = new String[IMAGES_IDS.length];

    static {
        for (int i = 0; i < IMAGES_IDS.length; i++) {
            IMAGES_NAMES[i] = "Circuit " + (i + 1);
        }
    }

    public static Drawable getCircuitImage(int index, Context context) {
        return context.getResources().getDrawable(IMAGES_IDS[index]);
    }

    public static int getCircuitImageId(int index) {
        return IMAGES_IDS[index];
    }

    public static String getCircuitName(int index) {
        return IMAGES_NAMES[index];
    }

    public static int getCircuitsCount() {
        return IMAGES_IDS.length;
    }

}
