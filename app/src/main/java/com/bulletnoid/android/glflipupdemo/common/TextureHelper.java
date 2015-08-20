package com.bulletnoid.android.glflipupdemo.common;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;

public class TextureHelper {
    private static final String TAG = "TextureHelper";

    public static final int TEXTURE_SIZEX = 2048;
    public static final int TEXTURE_SIZEL = 1024;
    public static final int TEXTURE_SIZEM = 512;
    public static final int TEXTURE_SIZES = 256;

    /**
     * Upload texture from resource, if bitmap size is not valid texture size, resize it
     *
     * @param context
     * @param resourceId
     * @return
     */
    public static int loadTextureFromResourse(final Context context, final int resourceId) {
        int textureId = 0;

        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;

        final Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), resourceId, options);

        textureId = loadTextureFromTexturedBitmap(bitmap, true);

        if (textureId == 0) {
            throw new RuntimeException("Error loading texture.");
        }

        return textureId;
    }

    /**
     * Upload texture from bitmap, if bitmap size is not valid texture size, resize it
     * Return texture id and original bitmap dimension
     *
     * @param context
     * @param resourceId
     * @return result[0] textureId
     * result[1] width
     * result[2] height
     */
    public static int[] loadTextureFromResourseWithDimen(final Context context, final int resourceId) {
        int textureId = 0;
        int width = 0;
        int height = 0;

        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;

        final Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), resourceId, options);
        width = bitmap.getWidth();
        height = bitmap.getHeight();

        textureId = loadTextureFromTexturedBitmap(bitmap, true);

        if (textureId == 0) {
            throw new RuntimeException("Error loading texture.");
        }

        int[] result = {textureId, width, height};

        return result;
    }

    /**
     * Upload texture from bitmap, if bitmap size is not valid texture size, resize it
     *
     * @param bitmap
     * @param recycleSource
     * @return
     */
    public static int loadTextureFromTexturedBitmap(Bitmap bitmap, boolean recycleSource) {
        final int[] textureHandle = new int[1];

        GLES20.glGenTextures(1, textureHandle, 0);

        if (textureHandle[0] != 0) {
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle[0]);

            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

            if (isValidSize(bitmap.getWidth(), bitmap.getHeight())) {
                GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
            } else {
                final Bitmap textureBitmap = createAutoSizedTexturedBitmap(bitmap);
                GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, textureBitmap, 0);
                textureBitmap.recycle();
            }

            if (recycleSource) {
                bitmap.recycle();
            }
        }

        if (textureHandle[0] == 0) {
            throw new RuntimeException("Error loading texture.");
        }

        return textureHandle[0];
    }

    /**
     * Check if bitmap size is valid (2^n)
     *
     * @param w
     * @param h
     * @return
     */
    public static boolean isValidSize(int w, int h) {
        return ((w & (w - 1)) == 0 && (h & (h - 1)) == 0);
    }

    /**
     * Return the closest power of 2 to n
     */
    public static int autoDecideValidSize(int n) {
        int next, prev, result;
        next = nextPowerOf2(n);
        prev = prevPowerOf2(n);
        if (next - n > n - prev) {
            result = prev;
        } else {
            result = next;
        }
        if (result > TEXTURE_SIZEX) {
            result = TEXTURE_SIZEX;
        }
        return result;
    }

    /**
     * Returns the next power of two.
     * Throws IllegalArgumentException if the input is <= 0 or
     * the answer overflows.
     *
     * @param n
     * @return
     */
    public static int nextPowerOf2(int n) {
        if (n <= 0 || n > (1 << 30)) throw new IllegalArgumentException("n is invalid: " + n);
        n -= 1;
        n |= n >> 16;
        n |= n >> 8;
        n |= n >> 4;
        n |= n >> 2;
        n |= n >> 1;
        return n + 1;
    }

    /**
     * Returns the previous power of two.
     * Returns the input if it is already power of 2.
     * Throws IllegalArgumentException if the input is <= 0
     * @param n
     * @return
     */
    public static int prevPowerOf2(int n) {
        if (n <= 0) throw new IllegalArgumentException();
        return Integer.highestOneBit(n);
    }

    public static void deleteTexture(int textureId) {
        GLES20.glDeleteTextures(1, new int[]{textureId}, 0);
    }

    /**
     * Auto resize the bitmap
     *
     * @param bitmap
     * @return
     */
    public static Bitmap createAutoSizedTexturedBitmap(Bitmap bitmap) {
        return Bitmap.createScaledBitmap(bitmap,
                autoDecideValidSize(bitmap.getWidth()),
                autoDecideValidSize(bitmap.getHeight()), true);
    }

    /**
     * Do not recycle param bitmap
     *
     * @param bitmap
     * @param sizeW
     * @param sizeH
     * @return
     */
    public static Bitmap createTextureBitmap(Bitmap bitmap, int sizeW, int sizeH) {
        return Bitmap.createScaledBitmap(bitmap, sizeW, sizeH, true);
    }
}
