package com.hci.lab430.myapplication.model;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Created by lab430 on 16/8/15.
 */
public class PGMapDataManager implements Callback {

    public final static String PGMapServerAddr = "http://140.112.30.43:5001";
    public final static String ImgServerAddr = "http://www.csie.ntu.edu.tw/~r03944003";
    public final static String MapResourcePath = "/map-data";
    public interface DataChangedListener {
        void onData(JSONObject jsonObject);
    }

    public DataChangedListener dataChangedListener = null;

    OkHttpClient okHttpClient;
    Call currentCall = null;
    Handler uiThreadHandler;
    int updateCycleInSecs;

    public PGMapDataManager(Context context, int updateCycleInSecs) {
        this.updateCycleInSecs = updateCycleInSecs;

        okHttpClient = new OkHttpClient();
        okHttpClient.setConnectTimeout(30, TimeUnit.SECONDS);
        okHttpClient.setReadTimeout(30, TimeUnit.SECONDS);

        uiThreadHandler = new Handler(context.getMainLooper());
        uiThreadHandler.post(issueHttpTask);
    }

    Runnable issueHttpTask = new Runnable() {
        @Override
        public void run() {
            try {
                if(currentCall == null) {
                    Request request = new Request.Builder()
                            .url(PGMapServerAddr + MapResourcePath)
                            .build();
                    currentCall = okHttpClient.newCall(request);
                    currentCall.enqueue(PGMapDataManager.this);
                }

                if(!currentCall.isExecuted())
                    currentCall.execute();
            } catch (IOException e) {
                e.printStackTrace();
            }

            if(updateCycleInSecs > 0)
                uiThreadHandler.postDelayed(issueHttpTask, updateCycleInSecs * 1000);
        }
    };

    private static class InvokeListenerOnUIThreadTask implements Runnable {

        DataChangedListener mListener;
        JSONObject dataObj;

        public InvokeListenerOnUIThreadTask(JSONObject jsonObject, DataChangedListener listener) {
            dataObj = jsonObject;
            mListener = listener;
        }

        @Override
        public void run() {
            if(mListener != null) {
                mListener.onData(dataObj);
            }
        }
    }

    @Override
    public void onFailure(Request request, IOException e) {
        e.printStackTrace();
        currentCall = null;
    }

    @Override
    public void onResponse(Response response) throws IOException {
        if(response.isSuccessful()) {
            try {
                JSONObject obj = new JSONObject(response.body().string());
                uiThreadHandler.post(new InvokeListenerOnUIThreadTask(obj, dataChangedListener));
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
        currentCall = null;
    }

    public void releaseAll() {
        if(currentCall != null && (currentCall.isExecuted() || !currentCall.isCanceled())) {
            currentCall.cancel();
        }

        currentCall = null;
        uiThreadHandler = null;
        dataChangedListener = null;
    }

}
