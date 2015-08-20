package com.bulletnoid.android.glflipupdemo.flipupview;

import com.bulletnoid.android.glflipupdemo.common.Vector3;

/**
 * Created by jonathan on 7/20/14.
 * <p/>
 * Record representing an rect image
 */
public class RectRecord {
    // updated by render
    public int mTexture;
    public float mAspect;

    // updated by scene builder
    public Vector3 mVector;
    public float mRotateAngle;
    public float mShadowPercent;

    public RectRecord(int texture, float aspect) {
        mTexture = texture;
        mAspect = aspect;

        mVector = new Vector3(0, 0, 0);
        mRotateAngle = 0;
        mShadowPercent = 0;
    }
}