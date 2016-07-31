package com.hci.lab430.myapplication;

import android.support.v7.app.AppCompatActivity;

/**
 * Created by lab430 on 16/7/31.
 */
public class CustomizedActivity extends AppCompatActivity {

    @Override
    public void onBackPressed() {
        if(isTaskRoot()) {
            moveTaskToBack(true);
        }
        else {
            super.onBackPressed(); //using default behaviour
        }
    }
}
