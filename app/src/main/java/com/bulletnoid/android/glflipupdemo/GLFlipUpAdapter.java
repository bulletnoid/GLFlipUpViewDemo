package com.bulletnoid.android.glflipupdemo;

import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.bulletnoid.android.glflipupdemo.common.TextureHelper;
import com.bulletnoid.android.glflipupdemo.flipupview.BitmapWrapper;
import com.bulletnoid.android.glflipupdemo.flipupview.GLAdapterInterface;
import com.squareup.picasso.BitmapCallback;
import com.squareup.picasso.Picasso;

import java.util.concurrent.ConcurrentHashMap;

public class GLFlipUpAdapter extends BaseAdapter implements GLAdapterInterface {
    private static final String TAG = "com.bulletnoid.android.gl.GLFlipUpAdapter";

    private ConcurrentHashMap<Integer, BitmapWrapper> mBitmapWrapper;

    public String URLS[] = {
            "http://media-cache-ak0.pinimg.com/736x/92/c3/92/92c39256180c5833348bf13f484c7118.jpg",
            "http://media-cache-ak0.pinimg.com/736x/9c/ce/ef/9cceef143ae517e7841669a930afcce0.jpg",
            "http://media-cache-ak0.pinimg.com/736x/f4/0e/ea/f40eeadee9be166a7765a2fa8dc329ee.jpg",
            "http://media-cache-ak0.pinimg.com/736x/61/c2/7b/61c27ba27d8e4d1be5a4e9885e41437a.jpg",
            "http://media-cache-ec0.pinimg.com/736x/7c/66/c3/7c66c3ed5840b7dca55e57380d3d6bac.jpg",
            "http://media-cache-ak0.pinimg.com/736x/2f/0c/15/2f0c1576c034ff4979c0ebdad42c646d.jpg",
            "http://media-cache-ak0.pinimg.com/736x/fe/c4/e9/fec4e938f2e6912c97405627bb902527.jpg",
            "http://media-cache-ak0.pinimg.com/736x/d6/ce/f5/d6cef528704c4b6bdf4fac2f48805b31.jpg",
            "http://media-cache-ec0.pinimg.com/736x/c4/2f/10/c42f10f8aa4803985f0ec7bde6d1ee7c.jpg",
            "http://media-cache-ak0.pinimg.com/736x/fe/2d/ef/fe2def2d9b5383cb886e62ea12ae5e4e.jpg",
            "http://media-cache-ec0.pinimg.com/736x/2b/38/f8/2b38f835e98b8c09d7f99f7791539901.jpg",
            "http://media-cache-ec0.pinimg.com/736x/9b/98/16/9b9816242b6433c6844bbf70cddbbe1a.jpg",
            "http://media-cache-ec0.pinimg.com/736x/2d/f0/92/2df0925b13d7bc782512b41d373cd941.jpg",
            "http://media-cache-ec0.pinimg.com/736x/98/1e/98/981e98dca0a0968b36007ec2ba45d973.jpg",
            "http://media-cache-ak0.pinimg.com/736x/8f/4e/f3/8f4ef30d53b3137034f539510062cd47.jpg",
            "http://media-cache-ec0.pinimg.com/736x/3e/16/5f/3e165fce7cb73211be5b5f78a426f0f7.jpg",
            "http://media-cache-ak0.pinimg.com/736x/90/fb/ab/90fbab0ef514bd47ea9bd81b78e43252.jpg",
            "http://media-cache-ak0.pinimg.com/736x/d7/39/fa/d739fa2fe6ac409f50738767f83d1192.jpg",
            "http://media-cache-ec0.pinimg.com/736x/37/28/85/37288502b44b06902708ceb6927cde42.jpg",
            "http://media-cache-ak0.pinimg.com/736x/dd/d7/81/ddd7813ce56b431083e7ca006a759ad1.jpg"
    };

    private Context mContext;
    private Application mAppContext;

    public GLFlipUpAdapter(Context context, Application app) {
        mContext = context;
        mAppContext = app;

        mBitmapWrapper = new ConcurrentHashMap<Integer, BitmapWrapper>();
    }

    public void onDestroy() {
        for (Integer key : mBitmapWrapper.keySet()) {
            mBitmapWrapper.get(key).mTexturedBitmap.recycle();
        }

        mBitmapWrapper.clear();
    }

    public int getCount() {
        return URLS.length;
    }

    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return 0;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        return null;
    }

    @Override
    public void loadBitmap(final int position) {
        mBitmapWrapper.put(position, new BitmapWrapper());

//        final int uri = SAMPLE_IMAGES[position % SAMPLE_IMAGES.length];
        final String uri = URLS[position % URLS.length];

        // i know this is crap, but you have to do this to get bitmap size

        // first load the picture
        // mem cache is useless here, don't use it, it's just a waste of memory
        Picasso.with(mAppContext).
                load(uri).
                fetchAndCallback(new BitmapCallback() {

                    @Override
                    public void onSuccess(Bitmap bitmap) {
                        if (bitmap != null) {
                            mBitmapWrapper.get(position).mOriginHeight = bitmap.getHeight();
                            mBitmapWrapper.get(position).mOriginWidth = bitmap.getWidth();
//                            Log.d("size", position + " : " + mBitmapWrapper.get(position).mOriginHeight +
//                                    " by " + mBitmapWrapper.get(position).mOriginWidth);
                            // then resize it to texture size, i'm lazy, use picasso here
                            Picasso.with(mAppContext).
                                    load(uri).
                                    resize(TextureHelper.autoDecideValidSize(bitmap.getWidth()),
                                            TextureHelper.autoDecideValidSize(bitmap.getHeight())).
                                    skipMemoryCache().
                                    fetchAndCallback(new BitmapCallback() {

                                        @Override
                                        public void onSuccess(Bitmap bitmap) {
                                            mBitmapWrapper.get(position).mTexturedBitmap = bitmap;
                                            notifyDataSetChanged();
                                        }

                                        @Override
                                        public void onError() {
                                        }
                                    });
                            }

                        }

                    @Override
                    public void onError() {
                    }
                });
    }

    @Override
    public void removeBitmap(int position) {
        mBitmapWrapper.get(position).mTexturedBitmap.recycle();
        mBitmapWrapper.remove(position);
    }

    @Override
    public BitmapWrapper getBitmapWrapper(int position) {
        return mBitmapWrapper.get(position);
    }

}
