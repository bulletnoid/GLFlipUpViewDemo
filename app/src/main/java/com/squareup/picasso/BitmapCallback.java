package com.squareup.picasso;

import android.graphics.Bitmap;

/**
 * Created by jonathan on 4/29/14.
 */
public interface BitmapCallback {
    void onSuccess(Bitmap bitmap);

    void onError();
}
