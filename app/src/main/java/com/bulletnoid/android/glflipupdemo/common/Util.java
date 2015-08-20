package com.bulletnoid.android.glflipupdemo.common;

import android.opengl.GLES20;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * Created by jonathan on 4/30/14.
 */
public class Util {
    private static final String TAG = "Util";

    public static void setColorV4(float[] color, float r, float g, float b, float a) {
        color[0] = color[4] = color[8] = color[12] = r;
        color[1] = color[5] = color[9] = color[13] = g;
        color[2] = color[6] = color[10] = color[14] = b;
        color[3] = color[7] = color[11] = color[15] = a;
    }

    public static void setMaskColorV4(float[] color, float r, float g, float b, float a, float rr, float gg, float bb, float aa) {
        color[0] = color[4] = r;
        color[1] = color[5] = g;
        color[2] = color[6] = b;
        color[3] = color[7] = a;
        color[8] = color[12] = rr;
        color[9] = color[13] = gg;
        color[10] = color[14] = bb;
        color[11] = color[15] = aa;
    }

    public static FloatBuffer makeFloatBuffer(final float[] arr) {
        FloatBuffer fb = ByteBuffer.allocateDirect(arr.length * 4)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        fb.put(arr).position(0);
        return fb;
    }

    public static void putFloatBuffer(FloatBuffer floatBuffer, final float[] arr) {
        floatBuffer.put(arr).position(0);
    }

    public static int createProgram(String vertexSource, String fragmentSource) {
        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexSource);
        if (vertexShader == 0) {
            return 0;
        }

        int pixelShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentSource);
        if (pixelShader == 0) {
            return 0;
        }

        int program = GLES20.glCreateProgram();
        if (program != 0) {
            GLES20.glAttachShader(program, vertexShader);
            checkGlError("glAttachShader");
            GLES20.glAttachShader(program, pixelShader);
            checkGlError("glAttachShader");
            GLES20.glLinkProgram(program);
            int[] linkStatus = new int[1];
            GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0);
            if (linkStatus[0] != GLES20.GL_TRUE) {
                Log.e(TAG, "Could not link program: ");
                Log.e(TAG, GLES20.glGetProgramInfoLog(program));
                GLES20.glDeleteProgram(program);
                program = 0;
            }
        }
        return program;
    }

    public static int loadShader(int shaderType, String source) {
        int shader = GLES20.glCreateShader(shaderType);
        if (shader != 0) {
            GLES20.glShaderSource(shader, source);
            GLES20.glCompileShader(shader);
            int[] compiled = new int[1];
            GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0);
            if (compiled[0] == 0) {
                Log.e(TAG, "Could not compile shader " + shaderType + ":");
                Log.e(TAG, GLES20.glGetShaderInfoLog(shader));
                GLES20.glDeleteShader(shader);
                shader = 0;
            }
        }
        return shader;
    }

    public static void checkGlError(String op) {
        int error;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            Log.e(TAG, op + ": glError " + error);
            throw new RuntimeException(op + ": glError " + error);
        }
    }

}
