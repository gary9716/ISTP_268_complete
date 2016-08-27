package com.hci.lab430.myapplication;

import android.content.Context;
import android.os.AsyncTask;

import com.hci.lab430.myapplication.model.Utils;

import org.json.JSONObject;

import java.lang.ref.WeakReference;

/**
 * Created by KTChou on 2016/8/27.
 */
public class ReadJSONTask extends AsyncTask<Void, Void, JSONObject> {

    WeakReference<Context> contextWeakReference;
    WeakReference<OnJSONGotListener> listenerWeakReference;
    String mFileName;

    public ReadJSONTask(Context context, OnJSONGotListener listener, String fileName) {
        listenerWeakReference = new WeakReference<OnJSONGotListener>(listener);
        contextWeakReference = new WeakReference<Context>(context);
        mFileName = fileName;
    }

    //on Background thread
    @Override
    protected JSONObject doInBackground(Void... params) {

        if(contextWeakReference.get() != null) {
            return Utils.getJSONFromAssets(contextWeakReference.get(), mFileName);
        }
        else {
            return null;
        }
    }

    //this is executed on UI thread(main thread)
    @Override
    protected void onPostExecute(JSONObject jsonObject) {
        super.onPostExecute(jsonObject);
        if(listenerWeakReference.get() != null) {
            listenerWeakReference.get().onJSON(jsonObject);
        }
    }

    public interface OnJSONGotListener {
        void onJSON(JSONObject jsonObject);
    }

}
