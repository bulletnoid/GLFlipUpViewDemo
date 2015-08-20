package com.bulletnoid.android.glflipupdemo.flipupview;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;

import com.bulletnoid.android.glflipupdemo.R;
import com.bulletnoid.android.glflipupdemo.common.RawResourceReader;
import com.bulletnoid.android.glflipupdemo.common.ShaderHelper;
import com.bulletnoid.android.glflipupdemo.common.Util;

import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by jonathan on 7/20/14.
 * <p/>
 * The render (try to) draw the model to the scene at 60fps
 */
public class GLFlipUpRenderer implements GLSurfaceView.Renderer {
    private static final String TAG = "FlipUpRenderer";

    private static final boolean DEBUG = false;
    private static final boolean DEBUG_FPS = false;

    private int mFrameCount = 0;
    private long mFrameCountingStart = 0;

    private static final float PERSPECTIVE_SCALE = 0.4f;
    private static final float epsilon = 0.01f;

    private Context mContext;
    private Model mModel;

    /**
     * Store the model matrix. This matrix is used to move models from object space (where each model can be thought
     * of being located at the center of the universe) to world space.
     */
    private float[] mModelMatrix = new float[16];

    /**
     * Store the view matrix. This can be thought of as our camera. This matrix transforms world space to eye space;
     * it positions things relative to our eye.
     */
    private float[] mViewMatrix = new float[16];

    /**
     * Store the projection matrix. This is used to project the scene onto a 2D viewport.
     */
    private float[] mProjectionMatrix = new float[16];

    /**
     * Allocate storage for the final combined matrix. This will be passed into the shader program.
     */
    private float[] mMVPMatrix = new float[16];

    private final FloatBuffer mRectPositionBuffer;
    private final FloatBuffer mRectColorBuffer;
    private final FloatBuffer mRectTextureCoordinateBuffer;

    // x, y, z
    private static float mRectPositionData[] = {
            -1.0f, -1.0f, 0.0f,
             1.0f, -1.0f, 0.0f,
            -1.0f,  1.0f, 0.0f,
             1.0f,  1.0f, 0.0f,
    };

    // r, g, b, a
    private final float mRectColorData[] = {
            1.0f, 1.0f, 1.0f, 1.0f,
            1.0f, 1.0f, 1.0f, 1.0f,
            1.0f, 1.0f, 1.0f, 1.0f,
            1.0f, 1.0f, 1.0f, 1.0f,
    };

    // x, y
    private static float mRectTextureCoordinateData[] = {
            0.0f, 1.0f,
            1.0f, 1.0f,
            0.0f, 0.0f,
            1.0f, 0.0f,
    };

    private int mMVPMatrixHandle;
    private int mMVMatrixHandle;
    private int mTextureUniformHandle;
    private int mPositionHandle;
    private int mColorHandle;
    private int mTextureCoordinateHandle;

    private int mColorMVPMatrixHandle;
    private int mColorPositionHandle;
    private int mColorColorHandle;

    private int mTexturedRectProgram;
    private int mColorRectProgram;

    private final int mBytesPerFloat = 4;
    private final int mPositionDataSize = 3;
    private final int mColorDataSize = 4;
    private final int mTextureCoordinateDataSize = 2;

    public GLFlipUpRenderer(Context context) {
        mContext = context;

        mRectPositionBuffer = Util.makeFloatBuffer(mRectPositionData);
        mRectColorBuffer = Util.makeFloatBuffer(mRectColorData);
        mRectTextureCoordinateBuffer = Util.makeFloatBuffer(mRectTextureCoordinateData);

    }

    public void setModel(Model model) {
        mModel = model;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {

        GLES20.glClearColor(0f, 0f, 0f, 0f);

        // Call CULL_FACE will cull reflection
//        GLES20.glEnable(GLES20.GL_CULL_FACE);
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);

        // load textured rect program
        final String vertexShader = RawResourceReader.readTextFileFromRawResource(mContext, R.raw.ct_vertex_shader);
        final String fragmentShader = RawResourceReader.readTextFileFromRawResource(mContext, R.raw.ct_fragment_shader);

        final int vertexShaderHandle = ShaderHelper.compileShader(GLES20.GL_VERTEX_SHADER, vertexShader);
        final int fragmentShaderHandle = ShaderHelper.compileShader(GLES20.GL_FRAGMENT_SHADER, fragmentShader);

        mTexturedRectProgram = ShaderHelper.createAndLinkProgram(vertexShaderHandle, fragmentShaderHandle,
                new String[]{"a_Position", "a_Color", "a_TexCoordinate"});

        // load color rect program
        final String colorVertexShader = RawResourceReader.readTextFileFromRawResource(mContext, R.raw.c_vertex_shader);
        final String colorFragmentShader = RawResourceReader.readTextFileFromRawResource(mContext, R.raw.c_fragment_shader);

        final int colorVertxShaderHandle = ShaderHelper.compileShader(GLES20.GL_VERTEX_SHADER, colorVertexShader);
        final int colorFragmentShaderHandle = ShaderHelper.compileShader(GLES20.GL_FRAGMENT_SHADER, colorFragmentShader);

        mColorRectProgram = ShaderHelper.createAndLinkProgram(colorVertxShaderHandle, colorFragmentShaderHandle,
                new String[]{"a_Position", "a_Color"});

        initCRectHandle();
        initCTRectHandle();

        mModel.init();

    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);

        GLES20.glClearColor(0f, 0f, 0f, 0f);

        // Create a new perspective projection matrix. The height will stay the same
        // while the width will vary as per aspect ratio.
        final float ratio = (float) width / height;
        final float left = -ratio;
        final float right = ratio;
        final float bottom = -1.0f;
        final float top = 1.0f;
        final float near = 1.0f;
        final float far = 16.0f;

        Matrix.frustumM(mProjectionMatrix, 0,
                left * PERSPECTIVE_SCALE, right * PERSPECTIVE_SCALE,
                bottom * PERSPECTIVE_SCALE, top * PERSPECTIVE_SCALE,
                near, far);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);

        setCameraLookat();

        drawAllTexturedRects();
        drawTransformColorRect();

        if (DEBUG_FPS) outputFps();
    }

    private void setCameraLookat() {
        Matrix.setLookAtM(mViewMatrix, 0,
                mModel.mEye.x, mModel.mEye.y, mModel.mEye.z,
                mModel.mEye.x, mModel.mEye.y, mModel.mEye.z - 1,
                0, 1, 0);
    }

    private void outputFps() {
        long now = System.nanoTime();
        if (mFrameCountingStart == 0) {
            mFrameCountingStart = now;
        } else if ((now - mFrameCountingStart) > 1000000000) {
            Log.d(TAG, "fps: " + (double) mFrameCount
                    * 1000000000 / (now - mFrameCountingStart));
            mFrameCountingStart = now;
            mFrameCount = 0;
        }
        ++mFrameCount;
    }

    private void initCTRectHandle() {
        mMVPMatrixHandle = GLES20.glGetUniformLocation(mTexturedRectProgram, "u_MVPMatrix");
        mMVMatrixHandle = GLES20.glGetUniformLocation(mTexturedRectProgram, "u_MVMatrix");
        mTextureUniformHandle = GLES20.glGetUniformLocation(mTexturedRectProgram, "u_Texture");
        mPositionHandle = GLES20.glGetAttribLocation(mTexturedRectProgram, "a_Position");
        mColorHandle = GLES20.glGetAttribLocation(mTexturedRectProgram, "a_Color");
        mTextureCoordinateHandle = GLES20.glGetAttribLocation(mTexturedRectProgram, "a_TexCoordinate");
    }

    private void initCRectHandle() {
        mColorMVPMatrixHandle = GLES20.glGetUniformLocation(mColorRectProgram, "u_MVPMatrix");
        mColorPositionHandle = GLES20.glGetAttribLocation(mColorRectProgram, "a_Position");
        mColorColorHandle = GLES20.glGetAttribLocation(mColorRectProgram, "a_Color");
    }

    private void drawAllTexturedRects() {
        GLES20.glUseProgram(mTexturedRectProgram);

        // first
        if (mModel.mNeedLoadNext) {
            mModel.autoAsyncLoadBitmap();
            mModel.mNeedLoadNext = false;
        }

        // second
        for (int i = mModel.mImageStartPos; i < mModel.mImageEndPos; i++) {
            drawTransformTexturedRect(mModel.getAndTrytoUpdateRecord(i), false);
            drawTransformTexturedRect(mModel.getAndTrytoUpdateRecord(i), true);
        }
    }

    /**
     * Perform TRS then draw the vertices
     *
     * @param rectRecord
     */
    float percent;

    public void drawTransformTexturedRect(RectRecord rectRecord, boolean mirror) {
        Matrix.setIdentityM(mModelMatrix, 0);

        Matrix.translateM(mModelMatrix, 0, rectRecord.mVector.x, rectRecord.mVector.y, rectRecord.mVector.z);

        if (mirror) {
            Matrix.scaleM(mModelMatrix, 0, 1, -1, 1);
        }

//        Matrix.translateM(mModelMatrix, 0, 0, epsilon, 0);

        Matrix.rotateM(mModelMatrix, 0, rectRecord.mRotateAngle, 1.0f, 0.0f, 0.0f);

        // use RT trick to avoid calculate tir-algebra
        Matrix.translateM(mModelMatrix, 0, 0, 1, 0);

        Matrix.scaleM(mModelMatrix, 0, 1f * rectRecord.mAspect, 1, 1);

        percent = rectRecord.mShadowPercent;
        if (mirror) {
            Util.setMaskColorV4(mRectColorData, percent, percent, percent, percent, 0, 0, 0, 1);
        } else {
            Util.setColorV4(mRectColorData, percent, percent, percent, percent);
        }
        Util.putFloatBuffer(mRectColorBuffer, mRectColorData);

        drawTexturedRect(rectRecord.mTexture);
    }

    /**
     * Draw the vertices with texture
     *
     * @param textureId
     */
    public void drawTexturedRect(int textureId) {
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
        GLES20.glUniform1i(mTextureUniformHandle, 0);

        // Prepare the triangle coordinate data
        mRectPositionBuffer.position(0);
        GLES20.glVertexAttribPointer(mPositionHandle, mPositionDataSize,
                GLES20.GL_FLOAT, false, 0, mRectPositionBuffer);
        GLES20.glEnableVertexAttribArray(mPositionHandle);

        // Pass in the color information
        mRectColorBuffer.position(0);
        GLES20.glVertexAttribPointer(mColorHandle, mColorDataSize,
                GLES20.GL_FLOAT, false, 0, mRectColorBuffer);
        GLES20.glEnableVertexAttribArray(mColorHandle);

        // Pass in the texture coordinate information
        mRectTextureCoordinateBuffer.position(0);
        GLES20.glVertexAttribPointer(mTextureCoordinateHandle, mTextureCoordinateDataSize,
                GLES20.GL_FLOAT, false, 0, mRectTextureCoordinateBuffer);
        GLES20.glEnableVertexAttribArray(mTextureCoordinateHandle);

        Matrix.multiplyMM(mMVPMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVPMatrix, 0);

        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix, 0);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, mRectPositionData.length / mPositionDataSize);

    }

    private void drawTransformColorRect() {
        Matrix.setIdentityM(mModelMatrix, 0);

        Matrix.translateM(mModelMatrix, 0,
                mModel.mCameraBasePosition.x,
                0,
                mModel.mCameraBasePosition.z);
        Matrix.scaleM(mModelMatrix, 0, 32, 1, 32);
        Matrix.rotateM(mModelMatrix, 0, -90, 1, 0, 0);

        Util.setColorV4(mRectColorData, 0, 0, 0, 0.5f);
        Util.putFloatBuffer(mRectColorBuffer, mRectColorData);

        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);

        drawColorRect();

        GLES20.glDisable(GLES20.GL_BLEND);
    }

    private void drawColorRect() {
        GLES20.glUseProgram(mColorRectProgram);

        // Prepare the triangle coordinate data
        mRectPositionBuffer.position(0);
        GLES20.glVertexAttribPointer(mPositionHandle, mPositionDataSize,
                GLES20.GL_FLOAT, false, 0, mRectPositionBuffer);
        GLES20.glEnableVertexAttribArray(mColorPositionHandle);

        // Pass in the color information
        mRectColorBuffer.position(0);
        GLES20.glVertexAttribPointer(mColorHandle, mColorDataSize,
                GLES20.GL_FLOAT, false, 0, mRectColorBuffer);
        GLES20.glEnableVertexAttribArray(mColorColorHandle);

        Matrix.multiplyMM(mMVPMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVPMatrix, 0);

        GLES20.glUniformMatrix4fv(mColorMVPMatrixHandle, 1, false, mMVPMatrix, 0);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, mRectPositionData.length / mPositionDataSize);

    }

}
