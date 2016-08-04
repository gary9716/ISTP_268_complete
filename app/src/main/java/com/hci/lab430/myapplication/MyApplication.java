package com.hci.lab430.myapplication;

import android.app.Application;

import com.hci.lab430.myapplication.model.PokemonInfo;
import com.parse.Parse;
import com.parse.ParseObject;

/**
 * Created by lab430 on 16/8/4.
 */
public class MyApplication extends Application {

    //Once a application is started, onCreate would be called.
    @Override
    public void onCreate() {
        super.onCreate();
        ParseObject.registerSubclass(PokemonInfo.class);
        Parse.initialize(new Parse.Configuration.Builder(getApplicationContext())
                .applicationId("aBriKu0h4EZgnb8Sft9Uv4HyDZHOj01WZQp3jPs1")
                .clientKey("YJy27NUjuLfJaicKAFReic3gpCFxdemFsPrsQj05")
                .server("https://parseapi.back4app.com/")
                .enableLocalDataStore()
                .build());


    }

}
