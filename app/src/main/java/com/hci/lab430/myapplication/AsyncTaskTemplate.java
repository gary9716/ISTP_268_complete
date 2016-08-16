package com.hci.lab430.myapplication;

import android.os.AsyncTask;

import java.net.URL;

/**
 * Created by lab430 on 16/8/15.
 */
public class AsyncTaskTemplate extends AsyncTask<URL, Integer, Long> {

    //called on UI Thread before background task started
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    //called on another thread
    @Override
    protected Long doInBackground(URL... urls) {

        Integer currentProgress = Integer.valueOf(85);
        publishProgress(currentProgress);

        return null;
    }

    //called on UI Thread after background task finished.
    @Override
    protected void onPostExecute(Long aLong) {
        super.onPostExecute(aLong);
    }

    //called on UI Thread when publishProgress is called.
    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
    }
}
