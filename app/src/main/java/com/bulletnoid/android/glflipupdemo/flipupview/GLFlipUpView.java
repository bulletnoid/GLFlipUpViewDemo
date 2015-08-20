package com.bulletnoid.android.glflipupdemo.flipupview;

import android.content.Context;
import android.graphics.PixelFormat;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.animation.AccelerateDecelerateInterpolator;

import com.bulletnoid.android.glflipupdemo.common.Vector3;

import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * This view manages the adapter and updates the scene model
 */
public class GLFlipUpView extends GLAdapterView {
    private static final String TAG = "GLFlipUpView";

    private static final boolean DEBUG = false;

    private static final int MAX_STEPS_PER_LOOP = 6;
    private static final int MAX_ON_SCREEN_IMAGE_SIZE = 4;

    private static final float TRANS_X = 3.0f;
    private static final float TRANS_Y = 0.0f;
    private static final float TRANS_Z = 3.0f;

    private static final Vector3 FLEFT = new Vector3(TRANS_X, TRANS_Y, TRANS_Z);
    private static final Vector3 FRIGHT = new Vector3(-TRANS_X, TRANS_Y, TRANS_Z);
    private static final Vector3 BLEFT = new Vector3(TRANS_X, TRANS_Y, -TRANS_Z);
    private static final Vector3 BRIGHT = new Vector3(-TRANS_X, TRANS_Y, -TRANS_Z);

    private static final long DURATION = 1500;
    private static final long DURATION_TRANS = 1500;
    private static final long DURATION_FLIP = 800;
    private static final long FLIP_STARTAT = 700;

    private static final long ANI_INIT_DELAY = 2;
    private static final long ANI_DELAY = 6;

    private int mStepCnt = 1;

    private int mLastDiraction;
    private int mNextDirection;

    private Vector3 mNextVector3;
    private Vector3 mTransVector;

    private AccelerateDecelerateInterpolator mADInterpolator;

    private boolean mShouldTrimImage = false;

    private ScheduledExecutorService mScheduler;
    private boolean mIsReadyToPause = false;

    private Model mModel;
    private GLFlipUpRenderer mRenderer;

    private Context mContext;

    public GLFlipUpView(Context context) {
        super(context);
        mContext = context;
        init();
    }

    public GLFlipUpView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init();
    }

    private void init() {
        setEGLContextClientVersion(2);

        mModel = new Model(mContext);

        mADInterpolator = new AccelerateDecelerateInterpolator();

        mTransVector = new Vector3(0, 0, 0);

        setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        getHolder().setFormat(PixelFormat.RGBA_8888);

    }

    @Override
    public void onResume() {
        super.onResume();
        resume();
    }

    @Override
    public void onPause() {
        super.onPause();
        pause();
    }

    public void resume() {
        mIsReadyToPause = false;
    }

    public void pause() {
        mIsReadyToPause = true;
    }

    public void start() {
        mModel.setAdapter(mAdapter);

        mRenderer = new GLFlipUpRenderer(mContext);
        mRenderer.setModel(mModel);
        setRenderer(mRenderer);

        mScheduler = Executors.newScheduledThreadPool(1);
        mScheduler.scheduleAtFixedRate(mAnimationRunnable, ANI_INIT_DELAY, ANI_DELAY, TimeUnit.SECONDS);

//        if (DEBUG) {
            setRenderMode(RENDERMODE_CONTINUOUSLY);
//        } else {
//            setRenderMode(RENDERMODE_WHEN_DIRTY);
//        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                return true;
            case MotionEvent.ACTION_MOVE:
                return true;
            case MotionEvent.ACTION_UP:
                return true;
        }
        return false;
    }

    @Override
    public boolean onKeyDown(int keycode, KeyEvent event) {
        switch (event.getKeyCode()) {
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                break;
            case KeyEvent.KEYCODE_DPAD_LEFT:
                break;
        }
        return true;
    }

    /**
     * Decide how the model to be updated
     */
    long startTime, elapsed;
    Vector3 vNext;
    Random rand = new Random();
    private Runnable mAnimationRunnable = new Runnable() {
        @Override
        public void run() {
            if (mIsReadyToPause) {
                return;
            }

            if (mStepCnt < MAX_STEPS_PER_LOOP - 1) {
                // move forward
                mNextDirection = rand.nextInt(2);

                mStepCnt++;
                mShouldTrimImage = false;
            } else if (mStepCnt == MAX_STEPS_PER_LOOP - 1) {
                // force a movement to last direction
                mNextDirection = mLastDiraction;

                mStepCnt++;
                mShouldTrimImage = false;
            } else {
                // move to the same direction of last but back and clean
                mNextDirection = mLastDiraction;

                mStepCnt = 1;
                mShouldTrimImage = true;
            }
            mLastDiraction = mNextDirection;

            if (mShouldTrimImage) {
                switch (mNextDirection) {
                    case 0:
                        mNextVector3 = BLEFT;
                        break;
                    case 1:
                        mNextVector3 = BRIGHT;
                        break;
                }
            } else {
                switch (mNextDirection) {
                    case 0:
                        mNextVector3 = FLEFT;
                        break;
                    case 1:
                        mNextVector3 = FRIGHT;
                        break;
                }
            }

            if (mModel.mImageEndPos == 0) {
                vNext = Vector3.add(mNextVector3, new Vector3(0, 0, 0));
            } else {
                vNext = Vector3.add(mNextVector3, mModel.getLoopedRectReccord(mModel.mImageEndPos - 1).mVector);
            }

            Vector3.copy(mModel.getLoopedRectReccord(mModel.mImageEndPos).mVector, vNext);

            mModel.mImageEndPos++;

            /**
             * [mImageStartPos, mImageEndPos)
             * the images in record
             */
            if (mModel.mImageEndPos - mModel.mImageStartPos > MAX_ON_SCREEN_IMAGE_SIZE) {
                mModel.mImageStartPos = mModel.mImageEndPos - MAX_ON_SCREEN_IMAGE_SIZE;
            }

            startTime = System.currentTimeMillis();

            while (true) {
                elapsed = System.currentTimeMillis() - startTime;
                if (elapsed >= DURATION) {
                    endAnimation();
                    break;
                } else {
                    updateModel(elapsed);
                }
//                requestRender();
            }
        }
    };

    /**
     * Looped by scheduler to update the model
     */
    float pt, ptt, pf, pff, elapsedTrans, elapsedFlip;
    float angle, percent, antPercent, dist;
    RectRecord rectRecord;
    private void updateModel(float elapsed) {
        elapsedTrans = elapsed;
        elapsedFlip = elapsed - FLIP_STARTAT;

        elapsedTrans = elapsedTrans > DURATION_TRANS ? DURATION_TRANS : elapsedTrans;

        elapsedFlip = elapsedFlip < 0 ? 0 : elapsedFlip;
        elapsedFlip = elapsedFlip > DURATION_FLIP ? DURATION_FLIP : elapsedFlip;

        // trans
        pt = elapsedTrans / DURATION_TRANS;
        ptt = mADInterpolator.getInterpolation(pt);

        if (mShouldTrimImage && ptt > 0.5) {
            mModel.mImageStartPos = mModel.mImageEndPos - 1;
        }

        mTransVector.set(ptt * mNextVector3.x, ptt * mNextVector3.y, ptt * mNextVector3.z);
        mModel.transCamera(mTransVector);

        // flip
        pf = elapsedFlip / DURATION_FLIP;
        pff = mADInterpolator.getInterpolation(pf);

        for (int i = mModel.mImageStartPos; i < mModel.mImageEndPos; i++) {
            rectRecord = mModel.getLoopedRectReccord(i);

            if (i == mModel.mImageEndPos - 1) {
                angle = (1 - pff) * (-90);
                percent = Math.abs(angle / 90);
            } else {
                angle = 0;
                dist = Math.abs(mModel.mEye.z - rectRecord.mVector.z);
                if (dist >= mModel.CAMERA_DIST_Z) {
                    percent = (dist - mModel.CAMERA_DIST_Z) / mModel.CAMERA_DIST_Z / 3f;
                } else {
                    percent = (mModel.CAMERA_DIST_Z - dist) / mModel.CAMERA_DIST_Z * 2f;
                }
            }
            percent *= 1.2;
            percent = percent > 1 ? 1 : percent;
            percent = percent < 0 ? 0 : percent;
            antPercent = 1 - percent;

            rectRecord.mRotateAngle = angle;
            rectRecord.mShadowPercent = antPercent;
        }

    }

    /**
     * Update model after animation
     */
    private void endAnimation() {
        requestRender();

        mModel.setCameraPosition(Vector3.add(mModel.mCameraBasePosition,
                new Vector3(mNextVector3.x, mNextVector3.y, mNextVector3.z)));

        mModel.mNeedLoadNext = true;

        if (mShouldTrimImage) {
            mShouldTrimImage = false;
        }

    }


}
