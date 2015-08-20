package com.bulletnoid.android.glflipupdemo.flipupview;

import android.content.Context;
import android.graphics.Bitmap;
import android.widget.BaseAdapter;

import com.bulletnoid.android.glflipupdemo.R;
import com.bulletnoid.android.glflipupdemo.common.TextureHelper;
import com.bulletnoid.android.glflipupdemo.common.Util;
import com.bulletnoid.android.glflipupdemo.common.Vector3;

/**
 * Created by jonathan on 7/20/14.
 * <p/>
 * Model represent the scene
 * Updated by the GLView and drew by the Renderer
 */
public class Model {
    private static final String TAG = "Model";

    public static final float CAMERA_DIST_Z = 3.0f;
    public final int RECORD_VOLUME = 10;
    public final int PLACEHOLDER_RES_ID = R.raw.placeholder;

    private Context mContext;

    // Camera info
    public Vector3 mCameraBasePosition;
    public Vector3 mEye;
//    public Vector3 mCenter;
//    public Vector3 mUp;

    public RectRecord mPlaceHolderRecord;

    public RectRecord[] mRectRecord;

    public int mBackgroundResId;
    public int mPlaceHolderResId;

    public boolean mNeedLoadNext = false;

    /**
     * the images the record is [start, end)
     */
    public int mImageStartPos = 0;
    public int mImageEndPos = 0;

    private BaseAdapter mAdapter;

    public Model(Context context) {
        mContext = context;

        mCameraBasePosition = new Vector3(0, 1, CAMERA_DIST_Z);

        mEye = new Vector3(0, 1, CAMERA_DIST_Z);
//        mCenter = new Vector3(0, 1, 0);
//        mUp = new Vector3(0, 1, 0);

        mRectRecord = new RectRecord[RECORD_VOLUME];

    }

    public void setAdapter(BaseAdapter adapter) {
        mAdapter = adapter;
    }

    public BaseAdapter getAdapter() {
        return mAdapter;
    }

    public GLAdapterInterface getAdapterImpl() {
        return (GLAdapterInterface) mAdapter;
    }

    /**
     * Call requires GLE
     */
    public void init() {
        mPlaceHolderRecord = getRectRecordFromRes(PLACEHOLDER_RES_ID);

        for (int i = 0; i < RECORD_VOLUME; i++) {
            mRectRecord[i] = new RectRecord(mPlaceHolderRecord.mTexture, mPlaceHolderRecord.mAspect);
        }

        asyncLoadBitmap(0);

        Util.checkGlError("init model");
    }

    public void transCamera(Vector3 v3) {
        mEye = Vector3.add(mCameraBasePosition, v3);
    }

    public void setCameraPosition(Vector3 v3) {
        mEye.set(v3.x, v3.y, v3.z);
        mCameraBasePosition.set(v3.x, v3.y, v3.z);
    }

    public void putLoopedRectRecord(int i, RectRecord rectRecord) {
        mRectRecord[i % RECORD_VOLUME] = rectRecord;
    }

    public RectRecord getLoopedRectReccord(int i) {
        return mRectRecord[i % RECORD_VOLUME];
    }

    /**
     * Call requires GLE
     * Trim oldest texture and reset the record
     */
    public void trimTexture(int pos) {
        RectRecord rr = getLoopedRectReccord(pos);

        if (rr.mTexture != mPlaceHolderRecord.mTexture) {
            TextureHelper.deleteTexture(rr.mTexture);
        }

        rr.mTexture = mPlaceHolderRecord.mTexture;
        rr.mAspect = mPlaceHolderRecord.mAspect;

    }

    /**
     * Call requires GLE
     * This method does not check if bitmap size is 2^n or resize them to 2^n
     *
     * @param resId
     * @return
     */
    private RectRecord getRectRecordFromRes(int resId) {
        int result[] = TextureHelper.loadTextureFromResourseWithDimen(mContext, resId);
        float aspect = (float) result[1] / result[2];
        return new RectRecord(result[0], aspect);
    }

    /**
     * Call requires GLE
     * Launch async task to load bitmap
     * <p/>
     * It is the first load func to call to build a new image
     *
     * @param position
     */
    public void asyncLoadBitmap(int position) {
        if (getAdapter() != null) {
            getAdapterImpl().loadBitmap(position);
        } else {
            throw new NullPointerException("no adapter");
        }
    }

    /**
     * Call requires GLE
     * Launch async task to load bitmap
     * <p/>
     * It is the first load func to call to build a new image
     */
    public void autoAsyncLoadBitmap() {
        trimTexture(mImageEndPos);

        if (getAdapter() != null) {
            getAdapterImpl().loadBitmap(mImageEndPos);
        } else {
            throw new NullPointerException("no adapter");
        }
    }

    /**
     * Call requires GLE
     * Always try to load texture from adapter
     * <p/>
     * It is the second load func to call to build a new image
     */
    RectRecord rectRecord;

    protected RectRecord getAndTrytoUpdateRecord(int position) {
        rectRecord = getLoopedRectReccord(position);
        if (rectRecord.mTexture == mPlaceHolderRecord.mTexture) {
            updateRecord(position);
        }
        return rectRecord;
    }

    /**
     * Call requires GLE
     * Called when async load bitmap finished, update the record on called position
     */
    BitmapWrapper bitmapWrapper;
    Bitmap bitmap;
    RectRecord record;
    float aspect;
    int texture;

    protected void updateRecord(int position) {
        if (getAdapter() != null) {
            bitmapWrapper = getAdapterImpl().getBitmapWrapper(position);
            if (bitmapWrapper != null) {
                bitmap = bitmapWrapper.mTexturedBitmap;
                if (bitmap != null) {
                    aspect = (float) bitmapWrapper.mOriginWidth / bitmapWrapper.mOriginHeight;
                    texture = TextureHelper.loadTextureFromTexturedBitmap(bitmap, true);
                    record = getLoopedRectReccord(position);
                    record.mTexture = texture;
                    record.mAspect = aspect;
                    getAdapterImpl().removeBitmap(position);
                }
            }
        } else {
            throw new NullPointerException("no adapter");
        }
    }


}
