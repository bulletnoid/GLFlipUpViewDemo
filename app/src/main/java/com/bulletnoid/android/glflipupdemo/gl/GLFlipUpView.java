//package com.bulletnoid.android.GLTest.gl;
//
//import android.content.Context;
//import android.graphics.Bitmap;
//import android.graphics.BitmapFactory;
//import android.graphics.PixelFormat;
//import android.opengl.GLSurfaceView;
//import android.opengl.GLU;
//import android.util.AttributeSet;
//import android.util.Log;
//import android.view.KeyEvent;
//import android.view.MotionEvent;
//import android.view.animation.AccelerateDecelerateInterpolator;
//import android.view.animation.AnimationUtils;
//
//import javax.microedition.khronos.egl.EGLConfig;
//import javax.microedition.khronos.opengles.GL10;
//import javax.microedition.khronos.opengles.GL11;
//
//import java.nio.FloatBuffer;
//import java.util.Random;
//import java.util.concurrent.Executors;
//import java.util.concurrent.ScheduledExecutorService;
//import java.util.concurrent.TimeUnit;
//
//public class GLFlipUpView extends GLAdapterView implements
//        GLSurfaceView.Renderer {
//
//    private static final String TAG = "GLFlipUpView";
//
//    private static final int MAX_STEPS_PER_LOOP = 6;
//    private static final int MAX_ON_SCREEN_IMAGE_SIZE = 5;
//
//    private static final float CAMERA_Z = 3.0f;
//
//    private static final float TRANS_X = 3.0f;
//    private static final float TRANS_Y = 0.0f;
//    private static final float TRANS_Z = 3.0f;
//
//    private static final float SCALE = 1.0f;
//
//    private static final float DURATION = 1.5f;
//    private static final float DURATION_TRANS = 1.5f;
//    private static final float DURATION_FLIP = 1.0f;
//    private static final float FLIP_STARTAT = 0.5f;
//
//    private static final long ANI_INIT_DELAY = 2;
//    private static final long ANI_DELAY = 5;
//
//    private static final int RECORD_VOLUME = 10;
//
//    private static final float[] GVertices = new float[] {
//            -1.0f, -1.0f, 0.0f,
//            1.0f, -1.0f, 0.0f,
//            -1.0f, 1.0f, 0.0f,
//            1.0f, 1.0f, 0.0f,
//    };
//
//    private static final float[] GTextures = new float[] {
//            0.0f, 1.0f,
//            1.0f, 1.0f,
//            0.0f, 0.0f,
//            1.0f, 0.0f,
//    };
//
//    private FloatBuffer mVerticesBuffer;
//    private FloatBuffer mTexturesBuffer;
//
//    private int mBgResId;
//    private Record mBgRecord;
//
//    private int mPlaceHolderResId;
//    private Record mPlaceHolderRecord;
//
//    private int mWidth;
//    private int mHeight;
//
//    private Listener mListener;
//
//    private float epsilon = 0.0001f;
//
//    private Record[] mRecord;
//    private Vector3 mCameraCurPosition;
//    private Vector3 mCameraBasePosition;
//    private Vector3 mTransVector;
//
//    private int mLastDiraction;
//    private int mNextDirection;
//    private Vector3 mNextVector3;
//
//    private AccelerateDecelerateInterpolator mADInterpolator;
//
//    private float mCurAnglePercent;
//    private int mOnScreenImageCnt;
//    private int mOnScreenImageStartPos;
//    private int mOnScreenImageEndPos;
//
//    private int mStepCnt;
//    private int mDirectionCnt;
//    private boolean mShouldTrimImage;
////    private boolean mShouldTrimTexture;
//
//    private ScheduledExecutorService mScheduler;
//    private boolean mIsReadyToPause;
//
//    private GL10 mGL10;
//
//    public GLFlipUpView(Context context) {
//        super(context);
//        init();
//    }
//
//    public GLFlipUpView(Context context, AttributeSet attrs) {
//        super(context, attrs);
//        init();
//    }
//
//    private void init() {
//        setEGLConfigChooser(8, 8, 8, 8, 16, 0);
//
//        setRenderer(this);
//        setRenderMode(RENDERMODE_WHEN_DIRTY);
//
//        getHolder().setFormat(PixelFormat.TRANSLUCENT);
//
//        mRecord = new Record[RECORD_VOLUME];
//
//        mADInterpolator = new AccelerateDecelerateInterpolator();
//
//        mTransVector = new Vector3(0, 0, 0);
//
//        mOnScreenImageCnt = 0;
//        mOnScreenImageStartPos = 0;
//        mOnScreenImageEndPos = 0;
//
//        mStepCnt = 0;
//
//        mScheduler = Executors.newScheduledThreadPool(1);
//        mIsReadyToPause = false;
//    }
//
//    public void onPause() {
//        pause();
//    }
//
//    public void onDestroy() {
//        trimTexture(mGL10, mOnScreenImageEndPos - MAX_ON_SCREEN_IMAGE_SIZE, mOnScreenImageEndPos);
//    }
//
//    public void setBackgroundRes(int res) {
//        mBgResId = res;
//    }
//
//    public void setPlaceHolderRes(int res) {
//        mPlaceHolderResId = res;
//    }
//
//    @Override
//    public boolean onTouchEvent(MotionEvent event) {
//        int action = event.getAction();
//        switch (action) {
//            case MotionEvent.ACTION_DOWN:
//                return true;
//            case MotionEvent.ACTION_MOVE:
//                return true;
//            case MotionEvent.ACTION_UP:
//                return true;
//        }
//        return false;
//    }
//
//    @Override
//    public boolean onKeyDown(int keycode, KeyEvent event) {
//        switch (event.getKeyCode()) {
//            case KeyEvent.KEYCODE_DPAD_RIGHT:
//                break;
//            case KeyEvent.KEYCODE_DPAD_LEFT:
//                break;
//        }
//        return true;
//    }
//
//    @Override
//    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
//        mVerticesBuffer = Util.makeFloatBuffer(GVertices);
//        mTexturesBuffer = Util.makeFloatBuffer(GTextures);
//
//        gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);                            // Set color's clear-value to black
//        gl.glClearDepthf(1.0f);                                             // Set depth's clear-value to farthest
////        gl.glClearStencil(0);
//        gl.glEnable(GL10.GL_DEPTH_TEST);
//        gl.glDepthFunc(GL10.GL_LEQUAL);                                     // The type of depth testing to do
//        gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_NICEST);     // nice perspective view
//        gl.glShadeModel(GL10.GL_SMOOTH);                                    // Enable smooth shading of color
//        gl.glDisable(GL10.GL_DITHER);
//
//        initCamera();
//        initPlaceHolder(gl);
//
//        loadBitmap(0);
//        loadBitmap(1);
//
//        mScheduler.scheduleAtFixedRate(mAnimationRunnable, ANI_INIT_DELAY, ANI_DELAY, TimeUnit.SECONDS);
//
//        mGL10 = gl;
//    }
//
//    @Override
//    public void onSurfaceChanged(GL10 gl, int w, int h) {
//        mWidth = w;
//        mHeight = h;
//
//        if (h == 0) h = 1;                                                  // To prevent divide by zero
//        float aspect = (float) w / h;
//
//        gl.glViewport(0, 0, w, h);
//
//        gl.glMatrixMode(GL10.GL_PROJECTION);                                // Select projection matrix
//        gl.glLoadIdentity();                                                // Reset projection matrix
//
//        GLU.gluPerspective(gl, 45, aspect, 0.1f, 100.f);                    // Use perspective projection
//
//        gl.glMatrixMode(GL10.GL_MODELVIEW);                                 // Select model-view matrix
//        gl.glLoadIdentity();
//
//        gl.glOrthof(-aspect * SCALE, aspect * SCALE, -1 * SCALE, 1 * SCALE, 1, 3);
//
//    }
//
//    @Override
//    public void onDrawFrame(GL10 gl) {
//        gl.glMatrixMode(GL10.GL_MODELVIEW);
//        gl.glLoadIdentity();
//        setCameraLookat(gl, mCameraCurPosition);
//
//        gl.glClearColor(0, 0, 0, 1);
//        gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
////        gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT | GL10.GL_STENCIL_BUFFER_BIT);
//
//        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
//        gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
//
//        // draw stencil
////        gl.glColorMask(false, false, false, false);
////        gl.glDisable(GL10.GL_DEPTH_TEST);
////        gl.glEnable(GL10.GL_STENCIL_TEST);
////        gl.glStencilOp(GL10.GL_KEEP, GL10.GL_KEEP, GL10.GL_REPLACE);
////        gl.glStencilFunc(GL10.GL_ALWAYS, 1, 1);
////        drawFloor(gl);
//
//        // draw shadow to stencil
////        gl.glColorMask(true, true, true, true);
////        gl.glEnable(GL10.GL_DEPTH_TEST);
////        gl.glStencilFunc(GL10.GL_EQUAL, 1, 1);
////        gl.glStencilOp(GL10.GL_KEEP, GL10.GL_KEEP, GL10.GL_KEEP);
////        drawImages(gl, true);
////        gl.glDisable(GL10.GL_STENCIL_TEST);
//
//        // draw target
//        drawImages(gl, false);
//        drawImages(gl, true);
//
//        // draw trans desk
//        drawFloor(gl);
//
//    }
//
//    public void pause() {
//        mIsReadyToPause = true;
//    }
//
//    public void resume() {
//        mIsReadyToPause = false;
//    }
//
//    float startTime, elapsed;
//    Vector3 vNext;
//    Random rand = new Random();
//
//    private Runnable mAnimationRunnable = new Runnable() {
//        @Override
//        public void run() {
//            if (mIsReadyToPause) {
//                return;
//            }
//            mNextDirection = rand.nextInt(2);
//
//            if (mStepCnt < MAX_STEPS_PER_LOOP) {
//                // move forward
//                switch (mNextDirection) {
//                    case 0:
//                        mNextVector3 = new Vector3(TRANS_X, TRANS_Y, TRANS_Z);
//                        break;
//                    case 1:
//                        mNextVector3 = new Vector3(-TRANS_X, TRANS_Y, TRANS_Z);
//                        mDirectionCnt++;
//                        break;
//                }
//                mStepCnt++;
//                mShouldTrimImage = false;
//            } else if (mStepCnt == MAX_STEPS_PER_LOOP) {
//                // force a movemet to mojority direction
//                if (mDirectionCnt > 3) {
//                    mNextVector3 = new Vector3(-TRANS_X, TRANS_Y, TRANS_Z);
//                    mLastDiraction = 1;
//                } else {
//                    mNextVector3 = new Vector3(TRANS_X, TRANS_Y, TRANS_Z);
//                    mLastDiraction = 0;
//                }
//                mStepCnt++;
//                mShouldTrimImage = false;
//            } else {
//                // move back and clean
//                if (mNextDirection == mLastDiraction) {
//                    mNextDirection += 1;
//                    mNextDirection %= 2;
//                }
//                switch (mNextDirection) {
//                    case 0:
//                        mNextVector3 = new Vector3(-TRANS_X, TRANS_Y, -TRANS_Z);
//                        break;
//                    case 1:
//                        mNextVector3 = new Vector3(TRANS_X, TRANS_Y, -TRANS_Z);
//                        break;
//                }
//                mStepCnt = 1;
//                mDirectionCnt = 0;
//                mShouldTrimImage = true;
//            }
//
//            if (mOnScreenImageEndPos == 0) {
//                vNext = Vector3.add(mNextVector3, new Vector3(0, 0, 0));
//            } else {
//                vNext = Vector3.add(mNextVector3, getLoopReord(mOnScreenImageEndPos - 1).mVector);
//            }
//            getLoopReord(mOnScreenImageEndPos).mVector = vNext;
//
//            mOnScreenImageEndPos++;
//
//            // [mOnScreenImageStartPos, mOnScreenImageEndPos]
//            if (mOnScreenImageEndPos - mOnScreenImageStartPos >= MAX_ON_SCREEN_IMAGE_SIZE) {
//                mOnScreenImageStartPos = mOnScreenImageEndPos - MAX_ON_SCREEN_IMAGE_SIZE + 1;
////                mOnScreenImageCnt = MAX_ON_SCREEN_IMAGE_SIZE;
//            }
//
//            startTime = AnimationUtils.currentAnimationTimeMillis();
//
//            while (true) {
//                elapsed = (AnimationUtils.currentAnimationTimeMillis() - startTime) / 1000.0f;
//                if (elapsed >= DURATION) {
//                    endAnimation();
//                    break;
//                } else {
//                    updateModel(elapsed);
//                }
//                requestRender();
//            }
//        }
//    };
//
//    float pt, ptt, pf, pff, elapsedTrans, elapsedFlip;
//
//    private void updateModel(float elapsed) {
//        elapsedTrans = elapsed;
//        elapsedFlip = elapsed - FLIP_STARTAT;
//
//        if (elapsedTrans > DURATION_TRANS) {
//            elapsedTrans = DURATION_TRANS;
//        }
//
//        if (elapsedFlip < 0) {
//            elapsedFlip = 0;
//        } else if (elapsedFlip > DURATION_FLIP) {
//            elapsedFlip = DURATION_FLIP;
//        }
//
//        // trans
//        pt = elapsedTrans / DURATION_TRANS;
//        ptt = mADInterpolator.getInterpolation(pt);
//
//        if (mShouldTrimImage && pt > 0.5) {
//            mOnScreenImageStartPos = mOnScreenImageEndPos - 1;
//        }
//
//        mTransVector.set(ptt * mNextVector3.x, ptt * mNextVector3.y, ptt * mNextVector3.z);
//        setCameraCurPosition(Vector3.add(mTransVector, mCameraBasePosition));
//
//        // flip
//        pf = elapsedFlip / DURATION_FLIP;
//        pff = mADInterpolator.getInterpolation(pf);
//
//        mCurAnglePercent = pff;
//    }
//
//    private void endAnimation() {
//        requestRender();
//
//        Vector3.copy(mCameraBasePosition, Vector3.add(mCameraBasePosition,
//                new Vector3(mNextVector3.x, mNextVector3.y, mNextVector3.z)));
//
//        loadBitmap(mOnScreenImageEndPos + 1);
//
//        if (mShouldTrimImage) {
////            mShouldTrimTexture = true;
//            mOnScreenImageCnt = 1;
//            mShouldTrimImage = false;
//        }
//
//        if (mOnScreenImageEndPos > MAX_ON_SCREEN_IMAGE_SIZE) {
//            trimTexture(mGL10, mOnScreenImageEndPos - MAX_ON_SCREEN_IMAGE_SIZE - 1, mOnScreenImageEndPos - MAX_ON_SCREEN_IMAGE_SIZE - 1);
//        }
//
//    }
//
//    // [from, to]
//    private void trimTexture(GL10 gl, int from, int to) {
//        Log.w("trim", String.valueOf(from) + " - " + String.valueOf(to));
//
//        for (int i = from; i <= to; i++) {
//            gl.glDeleteTextures(1, new int[]{getLoopReord(i).mTexture}, 0);
//        }
//    }
//
//    public void initCamera() {
//        mCameraCurPosition = new Vector3(0, 0, CAMERA_Z);
//        mCameraBasePosition = new Vector3(0, 0, CAMERA_Z);
//    }
//
//    public void setCameraLookat(GL10 gl, Vector3 v3) {
//        GLU.gluLookAt(gl, v3.x, v3.y + 1f, v3.z, v3.x, v3.y + 1f, v3.z - 1f, 0f, 1f, 0f);
//    }
//
//    public void setCameraCurPosition(Vector3 v3) {
//        mCameraCurPosition.set(v3.x, v3.y, v3.z);
//    }
//
//    private void initPlaceHolder(GL10 gl) {
//        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), mPlaceHolderResId);
//        aspect = (float) bitmap.getWidth() / bitmap.getHeight();
//        texture = Util.bitmap2Texture(gl, bitmap, Util.TEXTURE_SIZEM, Util.TEXTURE_SIZEM);
//        mPlaceHolderRecord = new Record(texture, aspect);
//
//    }
//
//    private void initBg(GL10 gl) {
//        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), mBgResId);
//        aspect = (float) bitmap.getWidth() / bitmap.getHeight();
//        texture = Util.bitmap2Texture(gl, bitmap, Util.TEXTURE_SIZEM, Util.TEXTURE_SIZEM);
//        mBgRecord = new Record(texture, aspect);
//    }
//
//    public void drawFloor(GL10 gl) {
//        gl.glPushMatrix();
//
//        gl.glVertexPointer(3, GL10.GL_FLOAT, 0, mVerticesBuffer);
//
//        gl.glTranslatef(mCameraBasePosition.x, mCameraBasePosition.y, mCameraBasePosition.z);
//        gl.glRotatef(90, 1, 0, 0);
//        gl.glScalef(8, 8, 1);
//
//        gl.glDisable(GL10.GL_TEXTURE_2D);
//        gl.glEnable(GL10.GL_BLEND);
//        gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
//        gl.glColor4f(0, 0, 0, 0.7f);
//        gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, 4);
//        gl.glColor4f(1, 1, 1, 1);
//        gl.glDisable(GL10.GL_BLEND);
//
//        gl.glPopMatrix();
//    }
//
//    private void drawImages(GL10 gl, boolean mirror) {
//        for (int i = mOnScreenImageStartPos; i < mOnScreenImageEndPos; ++i) {
//            drawImage(gl, getRecord(gl, i), (mOnScreenImageEndPos - 1) == i, mirror);
//        }
//    }
//
//    private float angle, percent, dist;
//    private double radius;
//
//    private void drawImage(GL10 gl, Record record, boolean useRotate, boolean mirror) {
//        if (record != null && record.mTexture != 0) {
//
//            gl.glPushMatrix();
//
//            gl.glVertexPointer(3, GL10.GL_FLOAT, 0, mVerticesBuffer);
//            gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, mTexturesBuffer);
//
//            gl.glEnable(GL10.GL_TEXTURE_2D);
//            gl.glBindTexture(GL10.GL_TEXTURE_2D, record.mTexture);
//
//            gl.glTranslatef(record.mVector.x, record.mVector.y, record.mVector.z);
//
//            if (mirror) {
//                gl.glScalef(1, -1, 1);
//            }
//
//            // -90 to 0
//            if (useRotate) {
//                angle = (1 - mCurAnglePercent) * (-90);
//            } else {
//                angle = 0;
//            }
//
//            radius = Math.toRadians(Math.abs(angle));
//            gl.glTranslatef(0, (float) Math.cos(radius), (float) -Math.sin(radius));
//            gl.glTranslatef(0, epsilon, 0);
//
//            // shade
//            if (useRotate) {
//                gl.glRotatef(angle, 1.0f, 0.0f, 0.0f);
//                percent = Math.abs(angle / 90);
//            } else {
//                dist = Math.abs(mCameraCurPosition.z - record.mVector.z);
//                if (dist > CAMERA_Z) {
//                    percent = (dist - CAMERA_Z) / CAMERA_Z / 3f;
//                } else {
//                    percent = (CAMERA_Z - dist) / CAMERA_Z * 2f;
//                }
//            }
//
//            gl.glScalef(1f * record.mAspect, 1, 1);
//
//            gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, 4);
//
//            gl.glDisable(GL10.GL_TEXTURE_2D);
//            gl.glEnable(GL10.GL_BLEND);
//            gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
//            gl.glColor4f(0, 0, 0, percent);
//            gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, 4);
//            gl.glColor4f(1, 1, 1, 1);
//            gl.glDisable(GL10.GL_BLEND);
//
//            gl.glPopMatrix();
//        }
//    }
//
//    public void loadBitmap(int position) {
//        if (getAdapter() != null) {
//            getAdapterImpl().loadBitmap(position);
//            putLoopRecord(position, new Record(mPlaceHolderRecord.mTexture, mPlaceHolderRecord.mAspect));
//        }
//    }
//
//    Bitmap bitmap;
//    private Record record;
//
//    private void updateRecord(GL10 gl, int position) {
//        if (getAdapter() != null) {
//            bitmap = getAdapterImpl().getBitmap(position);
//            if (bitmap != null) {
////                aspect = (float) getAdapterImpl().getSize(position).width / getAdapterImpl().getSize(position).height;
//                texture = Util.textureBitmap2Texture(gl, bitmap);
//                record = getLoopReord(position);
//                record.mTexture = texture;
//                record.mAspect = aspect;
//                getAdapterImpl().removeBitmap(position);
//            }
//        }
//    }
//
//    private float aspect;
//    private int texture;
//
//    private Record getRecord(GL10 gl, int position) {
//        record = getLoopReord(position);
//        if (record.mTexture == mPlaceHolderRecord.mTexture) {
//            updateRecord(gl, position);
//        }
//
//        return record;
//    }
//
//    private void putLoopRecord(int position, Record record) {
//        mRecord[position % RECORD_VOLUME] = record;
//    }
//
//    private Record getLoopReord(int position) {
//        return mRecord[position % RECORD_VOLUME];
//    }
//
//    public class Record {
//        public int mTexture;
//        public float mAspect;
//        public Vector3 mVector;
//
//        public Record(int texture, float aspect) {
//            this.mTexture = texture;
//            this.mAspect = aspect;
//            this.mVector = new Vector3(0, 0, 0);
//        }
//    }
//
//    public static interface Listener {
//        public void tileOnTop(GLFlipUpView view, int position);
//
//        public void topTileClicked(GLFlipUpView view, int position);
//    }
//
//}
