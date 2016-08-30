package com.hci.lab430.myapplication;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import com.hci.lab430.myapplication.model.Utils;

import java.lang.ref.WeakReference;

/**
 * Created by KTChou on 2016/8/30.
 */
public class LoadImgFromUrlTask extends AsyncTask<String, Void, Bitmap> {

    WeakReference<OnImageLoadedListener> mImageLoadedListenerWeakReference;

    public LoadImgFromUrlTask(OnImageLoadedListener listener) {
        mImageLoadedListenerWeakReference = new WeakReference<OnImageLoadedListener>(listener);
    }

    @Override
    protected Bitmap doInBackground(String... params) {
        String urlToLoad = params[0];
        byte[] imgBytes = Utils.urlToBytes(urlToLoad);
        return BitmapFactory.decodeByteArray(imgBytes, 0, imgBytes.length);
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        super.onPostExecute(bitmap);
        if(mImageLoadedListenerWeakReference.get() != null) {
            mImageLoadedListenerWeakReference.get().onImageLoaded(bitmap);
        }
    }

    public interface OnImageLoadedListener {
        void onImageLoaded(Bitmap img);
    }

}
