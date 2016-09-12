package com.hci.lab430.myapplication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.hci.lab430.myapplication.model.Utils;

import java.util.Calendar;

public class TestThreadAndReceiver extends AppCompatActivity {

    TextView wifiInfoText;
    TextView timeInfoText;
    Handler uiHandler;

    static final int updateTimePeriodInSecs = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_thread_and_receiver);
        wifiInfoText = (TextView)findViewById(R.id.wifiInfoText);
        timeInfoText = (TextView)findViewById(R.id.timeInfoText);
        uiHandler = new Handler(getMainLooper());
        uiHandler.post(updateTimeInfoToUITask);
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(wifiEventReceiver, intentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(wifiEventReceiver);
    }

    BroadcastReceiver wifiEventReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String statusMsg = Utils.getConnectivityStatusString(context);
            wifiInfoText.setText(statusMsg);
        }
    };

    Runnable updateTimeInfoToUITask = new Runnable() {
        @Override
        public void run() {
            //may need to format this string
            String timeInfo = Calendar.getInstance().getTime().toString();
            timeInfoText.setText(timeInfo);

            uiHandler.postDelayed(this, updateTimePeriodInSecs * 1000);
        }
    };

    Runnable downloadImgTask = new Runnable() {
        @Override
        public void run() {

        }
    };

}
