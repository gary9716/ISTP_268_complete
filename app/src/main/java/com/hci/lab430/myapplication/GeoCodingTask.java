package com.hci.lab430.myapplication;

import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.hci.lab430.myapplication.model.Utils;

import java.lang.ref.WeakReference;

/**
 * Created by lab430 on 16/8/15.
 */
public class GeoCodingTask extends AsyncTask<String, Void, double[]> {

    WeakReference<GeoCodingResponse> geoCodingResponseWeakReference;

    @Override
    protected double[] doInBackground(String... params) {
        Log.e("ThreadTest", Long.toString(Thread.currentThread().getId()));

        double[] latlng = Utils.getLatLngFromAddress(params[0]);

        if(latlng != null)
        {
            return latlng;
        }

        return null;
    }

    @Override
    protected void onPostExecute(double[] latlng) {
        super.onPostExecute(latlng);
        if(latlng!=null)
        {
            LatLng result = new LatLng(latlng[0], latlng[1]);
            if (geoCodingResponseWeakReference.get() != null)
            {
                geoCodingResponseWeakReference.get().callbackWithGeoCodingResult(result);
            }
        }
//            Log.e("PostExecute Thread ID", Long.toString(Thread.currentThread().getId()));

    }

    public GeoCodingTask(GeoCodingResponse geoCodingResponse)
    {
        geoCodingResponseWeakReference = new WeakReference<GeoCodingResponse>(geoCodingResponse);
    }

    public interface GeoCodingResponse{
        void callbackWithGeoCodingResult(LatLng latLng);
    }
}
