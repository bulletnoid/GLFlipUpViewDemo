package com.bulletnoid.android.glflipupdemo.flipupview;

import android.content.Context;
import android.database.DataSetObserver;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.widget.BaseAdapter;

/**
 * Wrapped basic adapter process
 */
public class GLAdapterView extends GLSurfaceView {
    private static final String TAG = "GLAdapterView";

    protected BaseAdapter mAdapter;
    protected AdapterDataSetObserver mObserver = new AdapterDataSetObserver();

    public GLAdapterView(Context context) {
        super(context);
    }

    public GLAdapterView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BaseAdapter getAdapter() {
        return mAdapter;
    }

    public GLAdapterInterface getAdapterImpl() {
        return ((GLAdapterInterface) mAdapter);
    }

    public void setAdapter(BaseAdapter adapter) {
        if (mAdapter != null) {
            mAdapter.unregisterDataSetObserver(mObserver);
        }

        if (adapter != null) {
            mAdapter = adapter;
            mAdapter.notifyDataSetChanged();
            mAdapter.registerDataSetObserver(mObserver);
        }
    }

    private class AdapterDataSetObserver extends DataSetObserver {
        @Override
        public void onChanged() {
            requestRender();
        }

        @Override
        public void onInvalidated() {
        }
    }

}