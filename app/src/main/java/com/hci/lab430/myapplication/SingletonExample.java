package com.hci.lab430.myapplication;

import android.content.Context;

/**
 * Created by lab430 on 16/7/27.
 */
public class SingletonExample {

    //the only one object existing in the whole application
    private static SingletonExample instance = null;
    private Context applicationContext;

    private SingletonExample(Context context) {
        applicationContext = context.getApplicationContext();
    }

    public static SingletonExample getInstance(Context context) {
        if(instance == null) {
            synchronized (SingletonExample.class) { //meanwhile, only one would be allowed to pass here
                if(instance == null) {
                    instance = new SingletonExample(context);
                }
            }
        }
        return instance;
    }

}
