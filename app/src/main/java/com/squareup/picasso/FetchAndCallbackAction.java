package com.squareup.picasso;

import android.graphics.Bitmap;

/**
 * Created by jonathan on 4/29/14.
 */
public class FetchAndCallbackAction extends Action<Void> {

    BitmapCallback mCallback;

    FetchAndCallbackAction(Picasso picasso, Request data, boolean skipCache, String key, BitmapCallback callback) {
        super(picasso, null, data, skipCache, false, 0, null, key);
        mCallback = callback;
    }

    @Override
    void complete(Bitmap result, Picasso.LoadedFrom from) {
        mCallback.onSuccess(result);
    }

    @Override
    void error() {
        mCallback.onError();
    }
}
