package com.bulletnoid.android.glflipupdemo.flipupview;

/**
 * Adapter used by GLFlipUpView has to implement this interface
 */
public interface GLAdapterInterface {

    public abstract void loadBitmap(int position);

    public abstract BitmapWrapper getBitmapWrapper(int position);

    public abstract void removeBitmap(int spoition);

}
