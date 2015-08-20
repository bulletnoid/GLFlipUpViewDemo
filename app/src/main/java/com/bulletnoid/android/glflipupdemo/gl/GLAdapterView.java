//package com.bulletnoid.android.GLTest.gl;
//
//import android.content.Context;
//import android.database.DataSetObserver;
//import android.opengl.GLSurfaceView;
//import android.util.AttributeSet;
//import android.widget.BaseAdapter;
//
//public class GLAdapterView extends GLSurfaceView {
//
//    private static final int TEXTURE_VOLUME = 10;
//
//    protected BaseAdapter mAdapter;
//    protected AdapterDataSetObserver mObserver = new AdapterDataSetObserver();
//
//    protected int mCurTextureIdex;
//    protected int[] mTextures = new int[TEXTURE_VOLUME];
//
//    public GLAdapterView(Context context) {
//        super(context);
//    }
//
//    public GLAdapterView(Context context, AttributeSet attrs) {
//        super(context, attrs);
//    }
//
//    public BaseAdapter getAdapter() {
//        return mAdapter;
//    }
//
//    public GLAdapterInterface getAdapterImpl() {
//        return ((GLAdapterInterface) mAdapter);
//    }
//
//    public void setAdapter(BaseAdapter adapter) {
//        if (mAdapter != null) {
//            mAdapter.unregisterDataSetObserver(mObserver);
//        }
//
//        if (adapter != null) {
//            mAdapter = adapter;
//            mAdapter.notifyDataSetChanged();
//            mAdapter.registerDataSetObserver(mObserver);
//        }
//    }
//
//    private class AdapterDataSetObserver extends DataSetObserver {
//        @Override
//        public void onChanged() {
//            requestRender();
//        }
//
//        @Override
//        public void onInvalidated() {
//        }
//    }
//
//    public int getTexture(int position) {
//        return mTextures[position % TEXTURE_VOLUME];
//    }
//
//    public void putTexture(int textureAddr) {
//        mTextures[mCurTextureIdex % TEXTURE_VOLUME] = textureAddr;
//        mCurTextureIdex++;
//    }
//}
