//package com.bulletnoid.android.GLTest.gl;
//
//import android.content.Context;
//import android.graphics.Bitmap;
//import android.graphics.BitmapFactory;
//import android.graphics.PixelFormat;
//import android.opengl.GLSurfaceView;
//import android.opengl.GLU;
//import android.util.Log;
//import android.view.KeyEvent;
//import android.view.MotionEvent;
//import android.view.animation.AccelerateDecelerateInterpolator;
//import android.view.animation.AnimationUtils;
//
//import javax.microedition.khronos.egl.EGLConfig;
//import javax.microedition.khronos.opengles.GL10;
//import java.nio.FloatBuffer;
//import java.util.Random;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//
//public class GLPhotoWallView extends GLAdapterView implements GLSurfaceView.Renderer {
//
//    private static final String TAG = "GLFlipUpView";
//
//    private static final int MAX_STEPS_PER_CLEAN = 6;
//    private static final int VISIBLE_TILES = 4;
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
//    private static final float DURATION_CAM_TRANS = 100f;
//    private static final float DURATION_CAM_ROTATE = 3f;
//
//    private static final long ANI_INIT_DELAY = 1;
//    private static final long ANI_DELAY = 5;
//
//    private static final int RECORD_VOLUME = 12;
//
//    private static final float WALL_SCALE_WIDTH = 30f;
//    private static final float WALL_SCALE_HEIGHT = 6f;
//    private static final float WALL_CAMERA_DIST = 8f;
//    private static final float CAMERA_DIFF = WALL_SCALE_WIDTH - WALL_CAMERA_DIST;
//    private static final float CAMERA_TRANS_DIST = 2 * CAMERA_DIFF;
//
//    private static final int NORTH = 0;
//    private static final int WEST = 1;
//    private static final int SOUTH = 2;
//    private static final int EAST = 3;
//
//    private static final int PHOTO_TYPE_L = 0;
//    private static final int PHOTO_TYPE_M1 = 1;
//    private static final int PHOTO_TYPE_M2 = 2;
//    private static final int PHOTO_TYPE_S = 3;
//
//    private static final float PHOTO_WIDTH_L = 2f;
//    private static final float PHOTO_WIDTH_M1 = 1.6f;
//    private static final float PHOTO_WIDTH_M2 = 1.2f;
//    private static final float PHOTO_WIDTH_S = 1f;
//
//    private static final float LAYOUT_WIDTH_L = 2.5f;
//    private static final float LAYOUT_WIDTH_M = 2.0f;
//
//    private static final float LAYOUT_UP = 0f;
//    private static final float LAYOUT_UP_UP = 1f;
//    private static final float LAYOUT_UP_LOW = -1f;
//
//    private static final float LAYOUT_LOW = 0f;
//    private static final float LAYOUT_LOW_UP = 0f;
//    private static final float LAYOUT_LOW_LOW = 0f;
//
//    private static final float THRESHOLD = WALL_CAMERA_DIST + 4f;
//
//    // type of different frames
//    private static final int FRAME_CNT = 10;
//
//    private static final float[] GVertices = new float[]{
//            -1.0f, -1.0f, 0.0f,
//            1.0f, -1.0f, 0.0f,
//            -1.0f, 1.0f, 0.0f,
//            1.0f, 1.0f, 0.0f,
//    };
//
//    private static final float[] GTextures = new float[]{
//            0.0f, 1.0f,
//            1.0f, 1.0f,
//            0.0f, 0.0f,
//            1.0f, 0.0f,
//    };
//
//    private FloatBuffer mVerticesBuffer;
//    private FloatBuffer mTexturesBuffer;
//
//    private int mWallPaperResId;
//    private Record mWallPaperRecord;
//
//    private int mPlaceHolderResId;
//    private Record mPlaceHolderRecord;
//
//    private Record[] mFrameRecord = new Record[FRAME_CNT];
//
//    private int mWidth;
//    private int mHeight;
//
//    private float epsilon = 0.0001f;
//
//    private PhotoRecord[] mRecord = new PhotoRecord[RECORD_VOLUME];
//    private Vector3 mCameraCurPosition;
//    private Vector3 mCameraBasePosition;
//    private Vector3 mTransVector;
//    private Vector3 mCameraLookDirect;
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
////    private int mLoadedImageEndPos;
////    private int mLoadedImageStartPos;
//
//    private int mStepCnt;
//    private int mDirectionCnt;
//    private boolean mShouldTrimImage;
//    private boolean mShouldTrimTexture;
//
//    private int mCurDirect;
//
//    private boolean gen2 = false;
//
//    /**
//     *    x   z
//     * N  0, -1
//     * E  1,  0
//     * S  0,  1
//     * W -1,  0
//     */
//    private int[][] mDirection      = {{ 0, -1}, { 1,  0}, { 0,  1}, {-1,  0}};
//    private int[][] mRotateVector   = {{ 1, -1}, { 1,  1}, {-1,  1}, {-1, -1}};
//    private int[][] mTransStart     = {{-1, -1}, { 1, -1}, { 1,  1}, {-1,  1}};
//    private int[][] mTransDirection = {{1, 0}, {0, 1}, {-1, 0}, {0, -1}};
//    private float[] mRotateDeg      = {0, -90, 180, 90};
//
//    private float mLayoutEnd;
//
//    private ExecutorService mExecutor;
//
//    public GLPhotoWallView(Context context) {
//        super(context);
//
//        setEGLConfigChooser(8, 8, 8, 8, 16, 0);
//
//        setRenderer(this);
//        setRenderMode(RENDERMODE_WHEN_DIRTY);
//
//        getHolder().setFormat(PixelFormat.TRANSLUCENT);
//
//        mADInterpolator = new AccelerateDecelerateInterpolator();
//
//        mTransVector = new Vector3(0, 0, 0);
//        mCameraLookDirect = new Vector3(0, 0, 0);
//
//        mOnScreenImageCnt = 0;
//        mOnScreenImageStartPos = 0;
//        mOnScreenImageEndPos = 0;
//
//        mStepCnt = 0;
//
//        mExecutor = Executors.newScheduledThreadPool(1);
//    }
//
//    public void setWallPaperRes(int res) {
//        mWallPaperResId = res;
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
//        gl.glDisable(GL10.GL_DEPTH_TEST);
////        gl.glDepthFunc(GL10.GL_LEQUAL);                                     // The type of depth testing to do
//        gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_NICEST);     // nice perspective view
//        gl.glShadeModel(GL10.GL_SMOOTH);                                    // Enable smooth shading of color
//        gl.glDisable(GL10.GL_DITHER);
//
//        initCamera();
//        initPlaceHolder(gl);
//        initWall(gl);
//
//        // init frames
//        for (int i = 0; i < FRAME_CNT; i ++) {
//            mFrameRecord[i] = new Record(mPlaceHolderRecord.mTexture, mPlaceHolderRecord.mAspect);
//        }
//
//        // init 5 photos
//        for (int i = 0; i < 6; i++) {
//            loadBitmap(i);
//        }
//
//        mOnScreenImageStartPos = 0;
//        mOnScreenImageEndPos = 4;
//
//        mExecutor.execute(mCameraRunnable);
//
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
//        setCameraLookat(gl, mCameraCurPosition, mCameraLookDirect);
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
////        drawImages(gl, false);
////        drawImages(gl, true);
//
//        // draw trans desk
////        drawFloor(gl);
//
////        if (mShouldTrimTexture) {
////            trimTexture(gl);
////            mShouldTrimTexture = false;
////        }
//        drawWalls(gl, NORTH);
//        drawWalls(gl, WEST);
//        drawWalls(gl, SOUTH);
//        drawWalls(gl, EAST);
//
//        drawImages(gl);
//    }
//
//    float startTime, elapsed;
//    Random rand = new Random();
//
//    float pt, ptt, pf, pff, elapsedTrans, elapsedFlip;
//
//    float trans, camDist;
//    int direct;
//
//    private Runnable mCameraRunnable = new Runnable() {
//        @Override
//        public void run() {
//
//            direct = 0;
//            mCurDirect = 0;
//
//            updateCameraLookDirect(NORTH);
//            mLayoutEnd = WALL_CAMERA_DIST;
//            startTime = AnimationUtils.currentAnimationTimeMillis();
//
//            while (true) {
//                elapsed = (AnimationUtils.currentAnimationTimeMillis() - startTime) / 1000.0f;
//                trans = CAMERA_TRANS_DIST * elapsed / DURATION_CAM_TRANS;
//
//                if (trans > CAMERA_TRANS_DIST) {
//
//                    startTime = AnimationUtils.currentAnimationTimeMillis();
//
//                    while (true) {
//                        elapsed = (AnimationUtils.currentAnimationTimeMillis() - startTime) / 1000.0f;
//                        angle = 90 * elapsed / DURATION_CAM_ROTATE;
//                        if (angle > 90) {
//                            break;
//                        }
//                        rotateCamera(direct, angle);
//                        requestRender();
//                    }
//
//                    direct += 1;
//                    direct %= 4;
//                    mCurDirect = direct;
//                    updateCameraLookDirect(direct);
//                    mLayoutEnd = WALL_CAMERA_DIST;
//                    startTime = AnimationUtils.currentAnimationTimeMillis();
//                    cruseCamera(direct, 0);
//                    requestRender();
//                } else {
//                    cruseCamera(direct, trans);
//                    requestRender();
//                    camDist = mTransDirection[direct][0] * mCameraCurPosition.x
//                            + mTransDirection[direct][1] * mCameraCurPosition.z;
//                    camDist = Math.abs(camDist);
//                    if (mLayoutEnd < camDist + THRESHOLD) {
//                        loadBitmap(++mOnScreenImageEndPos);
//                    }
//                }
//            }
//        }
//    };
//
//    private void updateCameraLookDirect(int direct) {
//        mCameraLookDirect.set(mDirection[direct][0], 0, mDirection[direct][1]);
//    }
//
//    private void rotateCamera(int direct, float angle) {
//        radius = Math.toRadians(Math.abs(angle));
//        if (mRotateVector[direct][0] != mRotateVector[direct][1]) {
//            mCameraLookDirect.set(
//                    mRotateVector[direct][0] * (float) Math.sin(radius),
//                    0,
//                    mRotateVector[direct][1] * (float) Math.cos(radius));
//        } else {
//            mCameraLookDirect.set(
//                    mRotateVector[direct][0] * (float) Math.cos(radius),
//                    0,
//                    mRotateVector[direct][1] * (float) Math.sin(radius));
//        }
//    }
//
//    private void cruseCamera(int direct, float trans) {
//        mCameraCurPosition.set(
//                mTransStart[direct][0] * CAMERA_DIFF + mTransDirection[direct][0] * trans,
//                0,
//                mTransStart[direct][1] * CAMERA_DIFF + mTransDirection[direct][1] * trans);
//
//    }
//
//    private void trimTexture(GL10 gl) {
//        Log.w("trim", String.valueOf(mOnScreenImageEndPos - MAX_STEPS_PER_CLEAN - 2) + " : " +
//                String.valueOf(mOnScreenImageEndPos - 2));
//        for (int i = mOnScreenImageEndPos - MAX_STEPS_PER_CLEAN - 2; i < mOnScreenImageEndPos - 2; i++) {
//            gl.glDeleteTextures(1, new int[]{getLoopReord(i).mFrameRecord.mTexture}, 0);
//            gl.glDeleteTextures(1, new int[]{getLoopReord(i).mPhotoRecord.mTexture}, 0);
//        }
//    }
//
//    public void initCamera() {
//        mCameraCurPosition = new Vector3(0, 0, CAMERA_Z);
//        mCameraBasePosition = new Vector3(0, 0, CAMERA_Z);
//    }
//
//    public void setCameraLookat(GL10 gl, Vector3 vEye, Vector3 vDirect) {
//        GLU.gluLookAt(gl,
//                vEye.x, vEye.y, vEye.z,
//                vEye.x + vDirect.x, vEye.y + vDirect.y, vEye.z + vDirect.z,
//                0f, 1f, 0f);
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
//    }
//
//    private void initWall(GL10 gl) {
//        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), mWallPaperResId);
//        aspect = (float) bitmap.getWidth() / bitmap.getHeight();
//        texture = Util.bitmap2Texture(gl, bitmap, Util.TEXTURE_SIZEL, Util.TEXTURE_SIZEM);
//        mWallPaperRecord = new Record(texture, aspect);
//    }
//
//    public void drawWalls(GL10 gl, int direct) {
//        gl.glPushMatrix();
//
//        gl.glVertexPointer(3, GL10.GL_FLOAT, 0, mVerticesBuffer);
//        gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, mTexturesBuffer);
//
//        gl.glEnable(GL10.GL_TEXTURE_2D);
//        gl.glBindTexture(GL10.GL_TEXTURE_2D, mWallPaperRecord.mTexture);
//
//        // TRS
//        gl.glTranslatef(
//                mDirection[direct][0] * WALL_SCALE_WIDTH,
//                0,
//                mDirection[direct][1] * WALL_SCALE_WIDTH);
//        gl.glRotatef(mRotateDeg[direct], 0, 1, 0);
//        gl.glScalef(WALL_SCALE_WIDTH, WALL_SCALE_HEIGHT, 1);
//
//        gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, 4);
//
//        gl.glPopMatrix();
//    }
//
//    private void drawImages(GL10 gl) {
//        for (int i = mOnScreenImageStartPos; i < mOnScreenImageEndPos; ++i) {
//            drawImage(gl, getRecord(gl, i).mPhotoRecord);
////            drawImage(gl, getRecord(gl, i).mFrameRecord);
//        }
//    }
//
//    private float angle, percent, dist;
//    private double radius;
//
//    private void drawImage(GL10 gl, Record record) {
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
//            // TRS
//            gl.glTranslatef(record.mVector.x, record.mVector.y, record.mVector.z);
//            gl.glRotatef(record.mAngleY, 0.0f, 1.0f, 0.0f);
//            gl.glScalef(1f , 1f / record.mAspect, 1f);
//            gl.glScalef(record.mScale, record.mScale, record.mScale);
//
//            gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, 4);
//
//            gl.glPopMatrix();
//        }
//    }
//
//    private void genLayout(GL10 gl, int pos, int direct) {
//
////        if (gen2) {
////            layoutWideLow(getRecord(gl, pos), direct);
////            Log.w("low", String.valueOf(mLayoutEnd));
////            gen2 = false;
////            mLayoutEnd += LAYOUT_WIDTH_M;
////        }
//
////        if (getRecord(gl, pos).mPhotoRecord.mAspect > 1) {
////            if (getRecord(gl, pos + 1).mPhotoRecord.mAspect > 1) {
////                if (rand.nextInt(2) == 0) {
////                    layoutWide(getRecord(gl, pos), direct);
////                } else {
////                    layoutWideUp(getRecord(gl, pos), direct);
////                }
////            } else {
////                layoutWide(getRecord(gl, pos), direct);
////            }
////        } else {
////            layoutLong(getRecord(gl, pos), direct);
////        }
//
////        if (rand.nextInt(2) % 2 == 0) {
////            layoutWide(getRecord(gl, pos), direct);
////        } else {
////            layoutWideUp(getRecord(gl, pos), direct);
////            Log.w("up", String.valueOf(mLayoutEnd));
////        }
//
//        if (pos % 2 == 0) {
//            layoutWideUp(getRecord(gl, pos), direct);
//        } else {
//            layoutWideLow(getRecord(gl, pos), direct);
//        }
//    }
//
//    private float coord_horiz, coord_vert, scale;
//    private void layoutWide(PhotoRecord record, int direct) {
//        coord_horiz = mLayoutEnd + LAYOUT_WIDTH_L / 2 - WALL_SCALE_WIDTH;
//        coord_vert = LAYOUT_UP;
//        scale = 2 / PHOTO_WIDTH_L;
//
//        putPhoto2Wall(record, coord_horiz, coord_vert, scale, direct);
//
//        mLayoutEnd += LAYOUT_WIDTH_L;
//    }
//
//    private void layoutLong(PhotoRecord record, int direct) {
//        coord_horiz = mLayoutEnd + LAYOUT_WIDTH_M / 2 - WALL_SCALE_WIDTH;
//        coord_vert = LAYOUT_UP;
//        scale = 2 / PHOTO_WIDTH_M1;
//
//        putPhoto2Wall(record, coord_horiz, coord_vert, scale, direct);
//
//        mLayoutEnd += LAYOUT_WIDTH_M;
//    }
//
//    private void layoutWideUp(PhotoRecord record, int direct) {
//        coord_horiz = mLayoutEnd + LAYOUT_WIDTH_M / 2 - WALL_SCALE_WIDTH;
//        coord_vert = LAYOUT_UP_UP;
//        scale = 2 / PHOTO_WIDTH_M2;
//
//        putPhoto2Wall(record, coord_horiz, coord_vert, scale, direct);
//
//        gen2 = true;
//    }
//
//    private void layoutWideLow(PhotoRecord record, int direct) {
//        coord_horiz = mLayoutEnd + LAYOUT_WIDTH_M / 2 - WALL_SCALE_WIDTH;
//        coord_vert = LAYOUT_UP_LOW;
//        scale = 2 / PHOTO_WIDTH_M2;
//
//        putPhoto2Wall(record, coord_horiz, coord_vert, scale, direct);
//
//        mLayoutEnd += LAYOUT_WIDTH_M;
//        Log.w("end", String.valueOf(mLayoutEnd));
//    }
//
//    private float vx, vy, vz;
//    private void putPhoto2Wall(PhotoRecord record, float horiz, float vert, float scale, int direct) {
//        switch (direct) {
//            case NORTH:
//                vx = horiz;
//                vy = vert;
//                vz = mDirection[direct][1] * (WALL_SCALE_WIDTH - epsilon);
//                break;
//            case WEST:
//                vx = mDirection[direct][0] * (WALL_SCALE_WIDTH - epsilon);
//                vy = vert;
//                vz = -horiz;
//                break;
//            case SOUTH:
//                vx = -horiz;
//                vy = vert;
//                vz = mDirection[direct][1] * (WALL_SCALE_WIDTH - epsilon);
//                break;
//            case EAST:
//                vx = mDirection[direct][0] * (WALL_SCALE_WIDTH - epsilon);
//                vy = vert;
//                vz = horiz;
//                break;
//        }
//        record.mPhotoRecord.mVector.x = vx;
//        record.mPhotoRecord.mVector.y = vy;
//        record.mPhotoRecord.mVector.z = vz;
//        record.mPhotoRecord.mAngleY = mRotateDeg[direct];
//        record.mPhotoRecord.mScale = scale;
//
//        record.mFrameRecord.mVector.x = vx;
//        record.mFrameRecord.mVector.y = vy;
//        record.mFrameRecord.mVector.z = vz;
//        record.mFrameRecord.mAngleY = mRotateDeg[direct];
//        record.mPhotoRecord.mScale = scale;
//    }
//
//    public void loadBitmap(int position) {
//        getAdapterImpl().loadBitmap(position);
//        putLoopRecord(position, new PhotoRecord(
//                new Record(mFrameRecord[position % 10].mTexture, mFrameRecord[position % 10].mAspect),
//                new Record(mPlaceHolderRecord.mTexture, mPlaceHolderRecord.mAspect)));
//    }
//
//    Bitmap bitmap;
//    private PhotoRecord record;
//
//    private void updateRecord(GL10 gl, int position) {
//        bitmap = getAdapterImpl().getBitmap(position);
//        if (bitmap != null) {
//            // update texture
//            aspect = (float) getAdapterImpl().getSize(position).width / getAdapterImpl().getSize(position).height;
//            texture = Util.textureBitmap2Texture(gl, bitmap);
//            record = getLoopReord(position);
//            record.mPhotoRecord.mTexture = texture;
//            record.mPhotoRecord.mAspect = aspect;
//            getAdapterImpl().removeBitmap(position);
//
//            // update layout
//            genLayout(gl, position, NORTH);
//        }
//    }
//
//    private float aspect;
//    private int texture;
//
//    private PhotoRecord getRecord(GL10 gl, int position) {
//        record = getLoopReord(position);
//        if (record.mPhotoRecord.mTexture == mPlaceHolderRecord.mTexture) {
//            updateRecord(gl, position);
//        }
//
//        return record;
//    }
//
//    private void putLoopRecord(int position, PhotoRecord record) {
//        mRecord[position % RECORD_VOLUME] = record;
//    }
//
//    private PhotoRecord getLoopReord(int position) {
//        return mRecord[position % RECORD_VOLUME];
//    }
//
//    public class PhotoRecord {
//        public Record mFrameRecord;
//        public Record mPhotoRecord;
//
//        public PhotoRecord(Record frameRecord, Record photoRecord) {
//            this.mFrameRecord = frameRecord;
//            this.mPhotoRecord = photoRecord;
//        }
//    }
//
//    public class Record {
//        public int mTexture;
//        public float mAspect;
//        public Vector3 mVector;
//        public float mScale;
//        public float mAngleY;
//
//        public Record() {
//            this.mTexture = 0;
//            this.mAspect = 0;
//            mVector = new Vector3(0, 0, 0);
//        }
//
//        public Record(int texture, float aspect) {
//            this.mTexture = texture;
//            this.mAspect = aspect;
//            mVector = new Vector3(0, 0, 0);
//        }
//    }
//
//}
