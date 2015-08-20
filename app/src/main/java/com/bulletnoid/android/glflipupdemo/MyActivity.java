package com.bulletnoid.android.glflipupdemo;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.os.Bundle;

import com.bulletnoid.android.glflipupdemo.flipupview.GLFlipUpRenderer;
import com.bulletnoid.android.glflipupdemo.flipupview.GLFlipUpView;

public class MyActivity extends Activity {

    private GLFlipUpView mGLFlipUpView;
    private GLFlipUpAdapter mAdapter;
    private GLFlipUpRenderer mRender;

//    private GLSurfaceView mGLSurfaceView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (detectOpenGLES20()) {
            mGLFlipUpView = new GLFlipUpView(this);
            setContentView(mGLFlipUpView);

            mAdapter = new GLFlipUpAdapter(this, getApplication());
            mGLFlipUpView.setAdapter(mAdapter);

            mGLFlipUpView.start();

            mGLFlipUpView.requestFocus();
        }
    }

    private boolean detectOpenGLES20() {
        ActivityManager am =
                (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        ConfigurationInfo info = am.getDeviceConfigurationInfo();
        return (info.reqGlEsVersion >= 0x20000);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mGLFlipUpView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mGLFlipUpView.onPause();
    }
//
//    @Override
//    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        return mGLFlipUpView.onKeyDown(keyCode, event);
//    }
}
