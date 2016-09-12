package com.hci.lab430.myapplication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.widget.Toast;

import com.hci.lab430.myapplication.model.Utils;

/**
 * Created by KTChou on 2016/9/7.
 */
public class WifiEventReceiver extends BroadcastReceiver {


    @Override
    public void onReceive(Context context, Intent intent) {
        String status = Utils.getConnectivityStatusString(context);

        Toast.makeText(context, status, Toast.LENGTH_LONG).show();
        Log.d("globalWifi","wifi:" + status);
    }

}
