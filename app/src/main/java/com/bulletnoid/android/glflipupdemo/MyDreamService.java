package com.bulletnoid.android.glflipupdemo;

import android.service.dreams.DreamService;

import com.bulletnoid.android.glflipupdemo.flipupview.GLFlipUpView;

public class MyDreamService extends DreamService {

    private GLFlipUpView mGLFlipUpView;
    private GLFlipUpAdapter mAdapter;

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();

        setInteractive(false);
        setFullscreen(true);

        mGLFlipUpView = new GLFlipUpView(this);

        mAdapter = new GLFlipUpAdapter(this, getApplication());
        mGLFlipUpView.setAdapter(mAdapter);

        mGLFlipUpView.start();

        setContentView(mGLFlipUpView);
    }
}
